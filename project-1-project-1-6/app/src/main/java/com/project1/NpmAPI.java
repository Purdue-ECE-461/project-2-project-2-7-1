package com.project1;

import org.orienteer.jnpm.JNPMService;
import org.orienteer.jnpm.JNPMSettings;
import org.orienteer.jnpm.dm.PackageInfo;
import org.orienteer.jnpm.dm.RepositoryInfo;

import java.util.List;

public class NpmAPI {
    JNPMService jnpmService;
    public NpmAPI(){
        JNPMService.configure(JNPMSettings.builder().build());
        jnpmService = JNPMService.instance();
    }

    public String getGithubURL(String packageName) {
        PackageInfo packageInfo = jnpmService.getPackageInfo(packageName);
        if (packageInfo != null) {
            List<RepositoryInfo> repositories = packageInfo.getRepositories();
            for (RepositoryInfo repo : repositories) {
                if (repo.getType().equals("git")) {
                    return repo.getUrl();
                }
            }
            return ""; //COULD NOT FIND GITHUB REPOSITORY
        } else {
            //RETURN EMPTY STRING IF package name is not valid
            return "";
        }

    }
}
