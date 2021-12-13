package com.project1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParseJacoco {
    public static double getCoverage() throws FileNotFoundException, IOException {
        List<List<String>> records = readCSV();
        int total = 0;
        int covered = 0; 
        for(int i = 1; i<records.size(); ++i) {
            total += Integer.parseInt(records.get(i).get(3));
            covered += Integer.parseInt(records.get(i).get(4));
        }
        total += covered;
        return (double)covered / total;
    }

    private static List<List<String>> readCSV() throws FileNotFoundException, IOException {
        List<List<String>> records = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader("app/target/site/jacoco/jacoco.csv"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] values = line.split(",");
            records.add(Arrays.asList(values));
        }
        br.close();
        return records;
    }
    
}
