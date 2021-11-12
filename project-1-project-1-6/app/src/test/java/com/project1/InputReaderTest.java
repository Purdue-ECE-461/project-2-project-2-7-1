package com.project1;

import org.apache.logging.log4j.core.config.Configurator;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class InputReaderTest {
    String logLevel = System.getenv("LOG_LEVEL");
    String logFile = System.getenv("LOG_FILE");

    @Test
    public void testValidInput(){
//        System.setProperty("logFilename", logFile); //creating system property to be used inside log4j2.xml configuration file
//        Configurator.setRootLevel(App.decode(logLevel));


        String path = "some/invalid/invalidFilePath";
        InputReader.read(path);

        assertTrue(InputReader.getNames() == null);
        assertTrue(InputReader.getOwners() == null);
    }

    @Test
    public void testInvalidInput(){
//        System.setProperty("logFilename", logFile); //creating system property to be used inside log4j2.xml configuration file
//        Configurator.setRootLevel(App.decode(logLevel));


        String path = "./src/test/java/com/project1/URLFiles/validPath.txt";
        InputReader.read(path);

        assertTrue(InputReader.getNames() != null);
        assertTrue(InputReader.getOwners() != null);

    }
}
