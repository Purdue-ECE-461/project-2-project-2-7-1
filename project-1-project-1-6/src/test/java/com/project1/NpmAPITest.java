package com.project1;

import org.junit.Test;
import org.junit.Ignore;

import static org.junit.Assert.assertTrue;

public class NpmAPITest {
    @Ignore
    @Test
    public void validPackage() {
        NpmAPI npmAPI = new NpmAPI();
        assertTrue(npmAPI.getGithubURL("react").equals("git+https://github.com/facebook/react.git"));
    }

    @Ignore
    @Test
    public void invalidPackage(){
        NpmAPI npmAPI = new NpmAPI();
        assertTrue(npmAPI.getGithubURL("invalidPackage").isEmpty());
    }
}
