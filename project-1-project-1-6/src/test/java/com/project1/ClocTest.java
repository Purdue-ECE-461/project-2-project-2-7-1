package com.project1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.Test;

public class ClocTest {
    String githubToken = System.getenv("GITHUB_TOKEN");
    String logLevel = System.getenv("LOG_LEVEL");
    String logFile = System.getenv("LOG_FILE");

//    @Test
//    public void test_happy() {
////        System.setProperty("logFilename", logFile); //creating system property to be used inside log4j2.xml configuration file
////        Configurator.setRootLevel(decode(logLevel));
//
//        Cloc c = new Cloc("./src/test/java/com/project1/CLOCTest");
//        try {
//            c.count();
//            assertEquals(4, c.getCommentLines());
//            assertEquals(3, c.getCodeLines());
//            assertEquals(1, c.getTestLines());
//            assertEquals(0.5714285714285714, c.getCommentRatio(), 0);
//            assertEquals(0.25, c.getTestRatio(), 0);
//        } catch (IOException e){
//            e.printStackTrace();
//            assertTrue(false);
//        }
//    }

    @Test
    public void test_invalid_path_unhappy() throws IOException {
//        System.setProperty("logFilename", logFile); //creating system property to be used inside log4j2.xml configuration file
//        Configurator.setRootLevel(decode(logLevel));
//
        Cloc c = new Cloc("/nonexistent/directory/");
        c.count();
        assertEquals(0, c.getCommentLines());
        assertEquals(0, c.getCodeLines());
        assertEquals(0, c.getTestLines());
        assertEquals(0, c.getCommentRatio(), 0);
        assertEquals(0, c.getTestRatio(), 0);
    }

    @Test
    public void test_empty_dir_unhappy() throws IOException {
//        System.setProperty("logFilename", logFile); //creating system property to be used inside log4j2.xml configuration file
//        Configurator.setRootLevel(decode(logLevel));

        Cloc c = new Cloc("./target/generated-sources/");
        c.count();
        assertEquals(0, c.getCommentLines());
        assertEquals(0, c.getCodeLines());
        assertEquals(0, c.getTestLines());
        assertEquals(0, c.getCommentRatio(), 0);
        assertEquals(0, c.getTestRatio(), 0);
    }
    
    static Level decode(String level){

        switch (level){
            case "0":
                return Level.OFF;
            case "1":
                return Level.INFO;
            case "2":
                return Level.DEBUG;
            default:
                return Level.OFF;
        }
    }
}
