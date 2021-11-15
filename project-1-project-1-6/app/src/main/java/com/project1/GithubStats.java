package com.project1;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.kohsuke.github.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.FileReader;

public class GithubStats {
    private GitHub github;
    private static final Logger logger = LogManager.getLogger(GithubStats.class);
    private GHRepository repository = null;
    private boolean licenseCompatibility = false;
    private double issuesRatio = 0;
    private List<Integer> participation = null;
    private double contributorsNum = 0;
    private double repositoryHealth = 0;
    private double commentRatio = 0;
    private double testRatio = 0;
    private double dependencyRatio = 0;
    private static HttpURLConnection httpConn = null;
    private static HttpURLConnection httpConn2 = null;

    public GithubStats(){}

    public void populateStatistics(String owner, String repo, String token){

        //ESTABLISH CONNECTION AND GET REPOSITORY OBJECT
        try {
            github = GitHub.connectUsingOAuth(token);
        } catch (IOException e) {
            logger.debug("Error in establishing connection to Github. Returning to main program");
            return;
        }

        try {
            repository = github.getRepository(owner+"/"+repo);
        } catch (IOException e) {
            logger.debug("Repository" + owner + "/" + repo + " not found. Returning to main program.");
            return;
        }

        if(repository == null){
            logger.debug("Could not get GHRepository object");
            return;
        }
        String repositoryName = repository.getFullName();

    //-------------------------------------GET GITHUB REPOSITORY STATISTICS --------------------------------------------
        GHRepositoryStatistics statistics = repository.getStatistics();
        if(statistics == null){
            logger.debug("Could not get GHRepositoryStatistics object");
        }else{
        //-------LIST INDEXED BY WEEK CONTAINING NUMBER OF ADDITIONS/DELETIONS FROM SOURCE CODE FROM THE PAST YEAR ------------------
            GHRepositoryStatistics.Participation participationStats = null;
            try {
                participationStats = statistics.getParticipation(); //last index is most recent week
            } catch (IOException e) {
                logger.debug("Error retrieving participation from repository: " + repositoryName);
            }
            if(participationStats != null) {
                participation = participationStats.getAllCommits();
            }else{
                logger.debug("Could not get GHRepositoryStatistics.Participation object");
            }

        //-----------------------------ITERABLE CONTAINING INFORMATION OF COLLABORATORS ------------------------------------
            try {
                PagedIterable<GHRepositoryStatistics.ContributorStats> contributorStats = statistics.getContributorStats(true);
                contributorsNum = contributorStats == null ? 0 : contributorStats.toList().size();
            } catch (IOException e) {
                logger.debug("Error retrieving contributor statistics from repository: " + repositoryName);
            } catch (InterruptedException e) {
                logger.debug("Error retrieving contributor statistics from repository: " + repositoryName + ". Thread was interrupted");
            }
        }

    //------REPOSITORY HEALTH IS A NUMERICAL VALUE CORRESPONDING TO THE DEGREE OF DOCUMENTATION THE REPOSITORY HAS -----
        repositoryHealth = calculateRepositoryHealth(owner, repo, token);
    //---------------ISSUE RATIO IS THE RATIO OF OPEN ISSUES TO ALL ISSUES UPDATED IN THE PAST YEAR---------------------
        issuesRatio = calculateIssuesRatio(owner, repo, token);
    //----------------------------GET DEPENDENCY RATIO------------------------------------------------------
        dependencyRatio = calculateDependencyRatio(repo);
    }


    private double calculateRepositoryHealth(String owner, String repo, String token) {
        String urlString = String.format("https://api.github.com/repos/%s/%s/community/profile", owner, repo);

        //ESTABLISH CONNECTION
        logger.debug("attempting manual connection to " + urlString + "... for calculating repository health");
        try {
            URL url = new URL(urlString);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setDoInput(true);
            httpConn.setRequestProperty("Authorization", "token " + token);
            httpConn.setRequestProperty("Accept", "application/vnd.github+json");

        } catch (MalformedURLException e) {
            logger.info("Malformed URL - check format of URL string");
            return 0;
        } catch (IOException e) {
            logger.info("Could not establish connection to " + urlString);
            return 0;
        }
        logger.info("connection established successfully");


        //SENDING REQUEST
        logger.info("Sending request to " + urlString);
        int status = 0;
        try {
            status = httpConn.getResponseCode();
        } catch (IOException e) {
            logger.debug("Error sending request to " + urlString);
            return 0;
        }
        logger.info("Connection status: " + status);

        if (status == 200){
            String line;
            StringBuffer responseContent = new StringBuffer();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            } catch (IOException e) {
                logger.debug("Error in getting http connection input stream");
                return 0;
            }

            try {
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
            } catch (IOException e) {
                logger.debug("Error in reading lines from http connection input stream");
                return 0;
            }

            JSONObject object = new JSONObject(responseContent.toString());
            return Double.parseDouble(object.get("health_percentage").toString());

        }
        else{
            logger.debug("Received response code other than 200. Returning 0 for repository health.");
            return 0;
        }

    }

    private boolean isValidDependency(String dependency){
        boolean isValid = !(dependency.contains("~") || dependency.contains("=") || dependency.contains("<") || dependency.contains(">") || dependency.contains("*"));
        return isValid;
    }

    private double calculateDependencyRatio(String name){

        double validDependencies = 0;
        double totalDependencies = 0;
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("target/" + name + "/package.json")) {
            JSONObject packageJson = new JSONObject(jsonParser.parse(reader).toString());
            if(packageJson.has("dependencies")){
                JSONObject dependencies = (JSONObject) packageJson.get("dependencies");
                Iterator<String> keys = dependencies.keys();
                while(keys.hasNext()) {
                    String key = keys.next();
                    totalDependencies += 1;
                    if (isValidDependency(dependencies.get(key).toString())) {
                        validDependencies += 1;
                    }
                }
            } else{
                logger.info("Repository provided does not have package.json file");
                return 1;
            }
        } catch (FileNotFoundException e) {
            logger.info("Could not find package.json file to evaluate dependencies score");
            return 1;
        } catch (IOException e) {
            logger.info("IO exception package.json evaluating dependencies score");
            return 1;
        } catch (ParseException e) {
            logger.info("Parse exception package.json evaluating dependencies score");
            return 1;
        }
        if(totalDependencies > 0){
            return validDependencies/totalDependencies;
        } else{
            return 1;
        }
    }

    private double calculateIssuesRatio(String owner, String repo, String token){
        String [] states = {"all", "closed"}; //query parameters for "state"

        //Getting date one year since ago in ISO8601 format
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        String sinceParameter = toISO8601UTC(calendar.getTime()); //query parameter for "since"

        double [] issues = new double[2];

        for(int i = 0; i < states.length; i++){
            String urlString = String.format("https://api.github.com/repos/%s/%s/issues?state=%s&per_page=100&since=%s", owner, repo, states[i], sinceParameter);

            //ESTABLISH CONNECTION
            logger.debug("attempting manual connection to " + urlString + "... for calculating issue ratio");
            try {
                URL url = new URL(urlString);
                httpConn2 = (HttpURLConnection) url.openConnection();
                httpConn2.setRequestMethod("GET");
                httpConn2.setDoInput(true);
                httpConn2.setRequestProperty("Authorization", "token " + token);
                httpConn2.setRequestProperty("Accept", "application/vnd.github+json");
            } catch (MalformedURLException e) {
                logger.info("Malformed URL - check format of URL string");
                return 0;
            } catch (IOException e) {
                logger.info("Could not establish connection to " + urlString);
                return 0;
            }
            logger.info("connection established successfully");

            //SENDING REQUEST
            logger.info("Sending request to " + urlString);
            int status;
            try {
                status = httpConn2.getResponseCode();
            } catch (IOException e) {
                logger.debug("Error sending request to " + urlString);
                return 0;
            }
            logger.info("Connection status: " + status);

            if (status == 200) {
                String line;
                StringBuffer responseContent = new StringBuffer();
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(httpConn2.getInputStream()));
                } catch (IOException e) {
                    logger.debug("Error in getting http connection input stream");
                    return 0;
                }

                try {
                    while ((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }
                } catch (IOException e) {
                    logger.debug("Error in reading lines from http connection input stream");
                    return 0;
                }

                JSONArray array = new JSONArray(responseContent.toString());
                issues[i] = array.length();

            }
            else{
                logger.debug("Received response code other than 200. Returning 0 for issues ratio");
                return 0;
            }

        }
        return issues[0] == 0 ? 0 : issues[1]/issues[0];
    }

    //METHOD TO CONVERT DEFAULT DATE OBJECT INTO ISO8601 FORMAT STRING
    public static String toISO8601UTC(Date date) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        return df.format(date);
    }

    public double getIssuesRatio(){return issuesRatio;}
    public double getContributorsNum(){return contributorsNum;}
    public double getRepositoryHealth(){return repositoryHealth;}
    public List<Integer> getParticipation() {return participation;}
    public boolean getLicense(){return licenseCompatibility;}
    public GHRepository getRepository(){return repository;}
    public double getCommentRatio(){return commentRatio;}
    public double getTestRatio(){return testRatio;}
    public double getDependencyRatio(){return dependencyRatio;}

    public void setLicenseCompatibility(boolean compatibility) {
        licenseCompatibility = compatibility;
    }

    public void setCommentRatio(double _ratio){
        commentRatio = _ratio;
    }

    public void setTestRatio(double _ratio) {testRatio = _ratio;}
}
