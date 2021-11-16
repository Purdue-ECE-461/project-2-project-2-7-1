package com.project1;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/* Counts line of code and computes commentRatio given path of repo */

public class Cloc {
    private static final Logger logger = LogManager.getLogger(Cloc.class);

    private File root = null;
    private int commentLines = 0;
    private int codeLines = 0;
    private int testLines = 0;
    private double commentRatio = 0;
    private double testRatio = 0;

    public Cloc(String _path) {
        root = new File(_path);
        logger.debug(root.getAbsolutePath().toString());
    }

    public int getCommentLines() {
        return commentLines;
    }

    public int getCodeLines() {
        return codeLines;
    }

    public int getTestLines() {
        return testLines;
    }

    public double getCommentRatio() {
        return commentRatio;
    }

    public double getTestRatio() {
        return testRatio;
    }

    public void count() throws IOException{

        String[] tokens = tokenizeCounts(root.getAbsolutePath());
        if(tokens != null){
            logger.debug("comment lines: " + tokens[3]);
            commentLines = Integer.parseInt(tokens[3]);
            logger.debug("code lines: " + tokens[4]);
            codeLines = Integer.parseInt(tokens[4]);
            commentRatio = (double) commentLines / (commentLines + codeLines);
        }else{
            logger.debug("could not read comment/code lines. Setting comment ratio to 0");
            commentRatio = 0;
        }


        String testPath = findTestDir(root);
        if(testPath != null) {
            tokens = tokenizeCounts(testPath);
            if(tokens!= null){
                logger.debug("test lines: " + tokens[4]);
                testLines = Integer.parseInt(tokens[4]);
                testRatio = (double) testLines / (testLines + codeLines);
            }else{
                logger.debug("could not obtain tokens for test directory. Setting test ratio to 0");
                testRatio = 0;
            }
        }
        else {
            logger.debug("could not find test directory. Setting test ratio to 0");
            testRatio = 0;
        }
    }

    private String findTestDir(File dir) {
        if (dir.getName().equals("test") || dir.getName().equals("tst") || dir.getName().equals("tests")) {
            return dir.getAbsolutePath();
        }

        File[] files = dir.listFiles();

        if(files != null) {
            for (File f : files) {
                if(f.isDirectory()) {   
                    String path = findTestDir(f);
                    if (path != null) {
                        logger.debug("findTestDir: " + path);
                        return path;
                    }
                }
            }
        }
        return null;
    }

    private String[] tokenizeCounts(String dir){
        logger.debug("calling tokenizeCounts on directory: " + dir);

        Process process;
        try {
            process = Runtime.getRuntime().exec("perl ./app/src/main/resources/cloc-1.90.pl --sum-one " + dir);
        } catch (IOException e){
            logger.debug("could not run perl script on directory " + dir);
            return null;
        }

        String countString = parseStream(process.getInputStream());
        if(countString == null || countString.isEmpty()) {
            try {
                process = Runtime.getRuntime().exec("perl ./src/main/resources/cloc-1.90.pl --sum-one " + dir);
            } catch( IOException f) {
                logger.debug("could not run perl script on directory " + dir);
                return null;
            }
            countString = parseStream(process.getInputStream());
            if(countString == null || countString.isEmpty()) {
                logger.error("could not get process input stream");
                return null;
            }
        }

        String[] tokens = countString.split("\\s+");
        return tokens;
    }

    private String parseStream(InputStream stream) {
        Scanner s = new Scanner(stream);
        String countString = s.findWithinHorizon("SUM:\\s*\\d*\\s*\\d*\\s*\\d*\\s*\\d*", 0);
        s.close();
        return countString;
    }
}
