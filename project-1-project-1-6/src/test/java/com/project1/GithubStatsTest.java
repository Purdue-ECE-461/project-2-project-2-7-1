package com.project1;

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.Test;

public class GithubStatsTest {
    private String [][] invalidRepositories = {{"nullive", "browserify", "cloudinar", "lodash", "expressj"},{"nodist", "browserif", "cloudinary_npm", "lodas", "express"}};
    String githubToken = System.getenv("GITHUB_TOKEN");
    String logLevel = System.getenv("LOG_LEVEL");
    String logFile = System.getenv("LOG_FILE");

    @Test
    public void invalidRepoTest(){
//        System.setProperty("logFilename", logFile); //creating system property to be used inside log4j2.xml configuration file
//        Configurator.setRootLevel(App.decode(logLevel));

        for(int i = 0; i < invalidRepositories[0].length; i++){
            GithubStats stats = new GithubStats();
            stats.populateStatistics(invalidRepositories[0][i], invalidRepositories[1][i], githubToken);

            assertTrue(stats.getRepository() == null);
            assertTrue(stats.getLicense() == false);
            assertTrue(stats.getIssuesRatio() == 0);
            assertTrue(stats.getParticipation() == null);
            assertTrue(stats.getContributorsNum() == 0);
            assertTrue(stats.getRepositoryHealth() == 0);
            //assertTrue(stats.getDependencyRatio() == 0);
        }
    }
    @Test
    public void validRepository(){
//        System.setProperty("logFilename", logFile); //creating system property to be used inside log4j2.xml configuration file
//        Configurator.setRootLevel(App.decode(logLevel));

        GithubStats stats = new GithubStats();
        stats.populateStatistics("nullivex", "nodist", githubToken);

        assertTrue(stats.getRepository() != null);
        assertTrue(stats.getIssuesRatio() != 0);
        //assertTrue(stats.getDependencyRatio() != 0);
        assertTrue(stats.getParticipation() != null);
        assertTrue(stats.getContributorsNum() != 0);
        assertTrue(stats.getRepositoryHealth() != 0);
    }
}
