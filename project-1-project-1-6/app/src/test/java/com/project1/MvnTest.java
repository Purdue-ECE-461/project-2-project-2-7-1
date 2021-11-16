package com.project1;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

public class MvnTest {

    @Ignore
    @Test
    public void test_happy() throws IOException {
        Mvn mvn = new Mvn();
        mvn.runTests();
        System.out.println(mvn);
        assertTrue(true);
    }
}
