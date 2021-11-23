package com.project1;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class License {
    private static final Logger logger = LogManager.getLogger(License.class);

    public License(){}

    public String getFileString(String path){
        String content = "";
        if(path.isEmpty()){
            logger.debug("no README file path");
            return content;
        }
        logger.debug("GETTING FILE CONTENT FROM " + path);

        File readMe = new File(path);
        try {
            Scanner reader = new Scanner(readMe);
            while(reader.hasNextLine()){
                content += reader.nextLine();
            }
            reader.close();
        } catch (FileNotFoundException e) {
            logger.debug("cannot not access README file for extracting content");
            e.printStackTrace();
            return "";
        }
        return content;
    }

    public boolean getLicenseCompatibility(String fileContent){
        if(fileContent.isEmpty()){
            logger.debug("no README file content");
            return false;
        }

        Map<String, String> nameToPatterns = new HashMap<>();
        nameToPatterns.put("LGPL", "[\\s\\W]+LGPL(v\\d)*[\\s\\W]*");
        nameToPatterns.put("GPL", "[\\s\\W]*GPL(v\\d)*[\\s\\W]*");
        nameToPatterns.put("MIT", "[\\s\\W]*MIT[\\s\\W]*");
        nameToPatterns.put("BSD","[\\s\\W]*BSD(v\\d)*[\\s\\W]*");

        for(Map.Entry<String, String> entry : nameToPatterns.entrySet()){
            logger.debug("Attempting to match file content to pattern: "+ entry.getValue());
            Pattern pattern = Pattern.compile(entry.getValue(), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(fileContent);
            if(matcher.find()){
                logger.debug("MATCH FOUND");
                return true;
            }
        }
        logger.debug("did not match README content to any pattern");
        return false;

    }

    public String findReadme(File repoRoot) {
        Pattern pattern = Pattern.compile("(readme\\.[\\w]+$)", Pattern.CASE_INSENSITIVE);
        File[] files = repoRoot.listFiles();
        if(files != null) {
            for (File f : files) {
                if(f.isFile()) {
                    Matcher matcher = pattern.matcher(f.getName());
                    if (matcher.find()) {
                        String path = repoRoot.getAbsolutePath() + "/" + matcher.group(1);
                        logger.debug("Readme found at: " + path);
                        return path;
                    }
                }
            }
        }
        logger.debug("Readme not found");
        return "";
    }
}