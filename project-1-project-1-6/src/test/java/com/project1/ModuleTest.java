package com.project1;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ModuleTest {
    @Test
    public void emptyStats(){
        Module module = new Module(null);

        assertTrue(module.getLicense() == 0);
        assertTrue(module.getResponsiveMaintainer() == 0);
        assertTrue(module.getRampUp() == 0);
        assertTrue(module.getBusFactor() == 0);
        assertTrue(module.getCorrectness() == 0);
        assertTrue(module.getTotal() == 0);

    }

}
