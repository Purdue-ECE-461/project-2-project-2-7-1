package com.project1;

import org.apache.logging.log4j.core.config.Configurator;
import org.junit.Test;

import static com.project1.App.decode;
import static org.junit.Assert.assertEquals;

public class LicenseTest {
    String githubToken = System.getenv("GITHUB_TOKEN");
    String logLevel = System.getenv("LOG_LEVEL");
    String logFile = System.getenv("LOG_FILE");

    @Test
    public void testCompatibleReadme(){
//        System.setProperty("logFilename", logFile); //creating system property to be used inside log4j2.xml configuration file
//        Configurator.setRootLevel(decode(logLevel));

        License license = new License();
        String test = license.getFileString("./src/test/java/com/project1/Licenses/CompatibleReadme.md");
        assertEquals(license.getLicenseCompatibility(test), true);
    }

    @Test
    public void testCompatibleReadme2(){
//        System.setProperty("logFilename", logFile); //creating system property to be used inside log4j2.xml configuration file
//        Configurator.setRootLevel(decode(logLevel));

        License license = new License();
        String test = license.getFileString("./src/test/java/com/project1/Licenses/CompatibleReadme2.md");
        assertEquals(license.getLicenseCompatibility(test), true);
    }

    @Test
    public void testCompatibleReadme3(){
//        System.setProperty("logFilename", logFile); //creating system property to be used inside log4j2.xml configuration file
//        Configurator.setRootLevel(decode(logLevel));

        License license = new License();
        String test = license.getFileString("./src/test/java/com/project1/Licenses/CompatibleReadme3.md");
        assertEquals(license.getLicenseCompatibility(test), true);
    }

    @Test
    public void testCompatibleReadme4(){
//        System.setProperty("logFilename", logFile); //creating system property to be used inside log4j2.xml configuration file
//        Configurator.setRootLevel(decode(logLevel));

        License license = new License();
        String test = license.getFileString("./src/test/java/com/project1/Licenses/CompatibleReadme4.md");
        assertEquals(license.getLicenseCompatibility(test), true);
    }

    @Test
    public void testIncompatibleReadme(){
//        System.setProperty("logFilename", logFile); //creating system property to be used inside log4j2.xml configuration file
//        Configurator.setRootLevel(decode(logLevel));

        License license = new License();
        String test = license.getFileString("./src/test/java/com/project1/Licenses/IncompatibleReadme.md");
        assertEquals(license.getLicenseCompatibility(test), true);
    }

    @Test
    public void testIncompatibleReadme2(){
//        System.setProperty("logFilename", logFile); //creating system property to be used inside log4j2.xml configuration file
//        Configurator.setRootLevel(decode(logLevel));

        License license = new License();
        String test = license.getFileString("./src/test/java/com/project1/Licenses/IncompatibleReadme2.md");
        assertEquals(license.getLicenseCompatibility(test), true);
    }

    @Test
    public void testIncompatibleReadme3(){
//        System.setProperty("logFilename", logFile); //creating system property to be used inside log4j2.xml configuration file
//        Configurator.setRootLevel(decode(logLevel));

        License license = new License();
        String test = license.getFileString("./src/test/java/com/project1/Licenses/IncompatibleReadme3.md");
        assertEquals(license.getLicenseCompatibility(test), false);
    }
}
