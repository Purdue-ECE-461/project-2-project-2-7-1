package com.project1;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.commons.io.FileDeleteStrategy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class App
{

    public static void main(String[] args) throws IOException {

        //LOADING ENVIRONMENT VARIABLES
        String githubToken = System.getenv("GITHUB_TOKEN");
        String logLevel = System.getenv("LOG_LEVEL");
        String logFile = System.getenv("LOG_FILE");

        if(githubToken == null){
            System.out.println("Missing necessary github token. Exiting application...");
            System.exit(1);
        }else if(logFile == null){
            System.out.println("Missing necessary log file. Exiting application...");
            System.exit(1);
        }else if(logLevel == null){
            logLevel = "0";
        }

        //CONFIGURE LOGGER
        System.setProperty("logFilename", logFile); //creating system property to be used inside log4j2.xml configuration file
        Configurator.setRootLevel(decode(logLevel)); //configuring verbosity level based on environment variable
        final Logger logger = LogManager.getLogger(App.class);

        //MAIN LOGIC
        if(args[0].equals("test")){
            Mvn mvn = new Mvn();
            mvn.runTests();
            try {
                System.out.println(mvn + String.format("%.0f", ParseJacoco.getCoverage()*100) +"% line coverage achieved");
            } catch (Exception e) {
                System.out.println("Test runs failed");
                System.exit(1);
            }
            System.exit(0);
        }else{
            //READ INPUT
            InputReader.read(args[0]);
            List<String> owners =  InputReader.getOwners();
            List<String> names = InputReader.getNames();
            List<String> urls = InputReader.getUrls();

            //URL FILE COULD NOT BE READ
            if(owners == null || names == null){
                System.out.println("Could not read URL file");
                System.exit(1);
            }

            //CLONE, REQUEST, CALCULATE
            logger.debug("CLONING, REQUESTING AND CALCULATING...");
            List<Module> modules = new ArrayList<>();
            Module module;

            //ITERATE THROUGH LIST OF REPOSITORIES
            for(int i = 0; i < urls.size(); i++){
                String owner = owners.get(i);
                String name = names.get(i);
                String url = urls.get(i);


                //WRONGLY FORMATTED URL - COULD NOT RETRIEVE OWNER/REPOSITORY PAIR - CONTINUE TO NEXT ITERATION
                if(owner.isEmpty() || name.isEmpty()){
                    module = new Module(null);
                    module.setUrl(url);
                    modules.add(module);
                    continue;
                }

                //CLONING REPOSITORY LOCALLY
                String cloneURL = "https://github.com/" + owner + "/" + name;
                boolean cloningSuccess = GitCloner.Clone(cloneURL, name);

                //POPULATING STATISTICS FROM GITHUB API
                GithubStats stats = new GithubStats();
                stats.populateStatistics(owner, name, githubToken);

                //POPULATING STATISTICS FROM CLONED REPO.
                if(cloningSuccess){
                    //CHECKING LICENSE COMPATIBILITY FROM LOCALLY CLONED REPO
                    License license = new License();
                    String readmePath = license.findReadme(new File("target/" + name));
                    String readmeContent = license.getFileString(readmePath);
                    boolean compatibility = license.getLicenseCompatibility(readmeContent);
                    stats.setLicenseCompatibility(compatibility);

                    //COUNTING LINES OF CODE IN ROOT REPOSITORY AND TEST REPOSITORY
                    Cloc c = new Cloc("target/"+name);
                    c.count();

                    stats.setCommentRatio(c.getCommentRatio());
                    stats.setTestRatio(c.getTestRatio());
                }

                //CALCULATE FINAL METRICS
                module = new Module(stats);
                module.setUrl(url);
                module.calculateMetrics();

                modules.add(module);

                GitCloner.close();
                FileDeleteStrategy.FORCE.delete(new File("./target/"+names.get(i)));
            }

            Collections.sort(modules, Collections.reverseOrder());
            System.out.println("URL NET_SCORE RAMP_UP_SCORE CORRECTNESS_SCORE BUS_FACTOR_SCORE RESPONSIVE_MAINTAINER_SCORE LICENSE_SCORE");
            for(Module m : modules){
                System.out.println(m);
            }

            System.exit(0);
        }
    }

    static Level decode(String level){

        switch (level){
            case "1":
                return Level.INFO;
            case "2":
                return Level.DEBUG;
            default:
                return Level.OFF;
        }
    }
}