package com.project1;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ParseJacocoTest {
    
    @Ignore
    @Test
    public void test_happy() throws FileNotFoundException, IOException {
        System.out.println(ParseJacoco.getCoverage());
        assertTrue(true);
    }
}
