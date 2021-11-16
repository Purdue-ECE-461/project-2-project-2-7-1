package com.project1;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import java.nio.file.Paths;
import java.io.File;

public class GitCloner {
    private static final Logger logger = LogManager.getLogger(GitCloner.class);
    private static Git result = null;
    public static boolean Clone(String repoUrl, String someName){

        try {
            logger.debug("Cloning "+repoUrl+" into ./target/"+someName);
            File file = new File("./target/"+someName);
            deleteFolder(file);
            result = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(Paths.get("./target/" + someName).toFile())
                .call();
            logger.debug("cloning successful");
        } catch (GitAPIException e) {
            logger.debug("error in cloning github repository");
            return false;
        }
        return true;
    }
    static void deleteFolder(File file){
        if(file != null && file.listFiles() != null) {
            for (File subFile : file.listFiles()) {
                if (subFile.isDirectory()) {
                    deleteFolder(subFile);
                } else {
                    subFile.delete();
                }
            }
        }
        file.delete();
    }
    public static void close(){
        if(result == null){
            return;
        }
        result.close();
    }

}
