package com.project1;

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;

public class InputReader {
    private static final Logger logger = LogManager.getLogger(InputReader.class);
    private static ArrayList<String> urls = null;
    private static ArrayList<String> owners = new ArrayList<>();
    private static ArrayList<String> names = new ArrayList<>();
    private static NpmAPI npmapi = new NpmAPI();

    public static void read(String urlIn){
        urls = new ArrayList<>();
        if (urlIn != null) {
            urls.add(urlIn);
        } else {
            logger.info("INPUT READER URL WAS NULL");
            return;
        }

        for(int i = 0; i < urls.size(); i++)
        {
            String url = urls.get(i);
            String[] tokens = url.split("/");

            if(tokens.length == 5){ //CORRECTLY FORMATTED URL
                String gitHubUrl = "";
                if(url.contains("npmjs.com")){ //CORRECTLY FORMATTED URL FROM NPMJS

                    gitHubUrl = npmapi.getGithubURL(tokens[4]);

                    if(!gitHubUrl.isEmpty()){ //VALID NPM PACKAGE NAME WITH GITHUB URL
                        tokens = gitHubUrl.split("/");
                        owners.add(tokens[3]);
                        names.add(FileNameUtils.getBaseName(tokens[4]));
                    }else{ //INVALID NPM PACKAGE NAME
                        owners.add("");
                        names.add("");
                    }

                }else{ //CORRECTLY FORMATTED URL FROM GITHUB
                    owners.add(tokens[3]);
                    names.add(tokens[4]);
                }
            }else{ //INCORRECTLY FORMATTED URL
                owners.add("");
                names.add("");
            }
        }
    }

    public static List<String> getOwners(){return owners;}
    public static List<String> getNames(){return names;}
    public static List<String> getUrls(){return urls;}

}