package com.project1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mvn {
    private int numTests = 0;
    private int numFailed = 0;

    public void runTests() throws IOException {
        Runtime.getRuntime().exec("apache-maven-3.8.3/bin/mvn -f app/ test");
        
        readFiles();
    }

    private void readFiles() throws FileNotFoundException, IOException {

        File root = new File("app/target/surefire-reports/");
        File[] files = root.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".txt");
            };
        });

        if(files != null) {
            Pattern pattern = Pattern.compile("Tests run: \\d+, Failures: \\d+, Errors: \\d+, Skipped: \\d+");
            for (File f : files) {
                BufferedReader r = new BufferedReader(new FileReader(f));
                String line;
                while((line = r.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    if(matcher.find()) {
                        String[] tokens = line.substring(matcher.start(), matcher.end()).split("\\s+|,");
                        numTests += Integer.parseInt(tokens[2]) + Integer.parseInt(tokens[11]);
                        numFailed += Integer.parseInt(tokens[5]) + Integer.parseInt(tokens[8]);
                    }
                }
                r.close();
            }
        }
    }

    @Override
    public String toString() {
        return (numTests - numFailed) + "/" + numTests + " test cases passed. ";
    }
}
