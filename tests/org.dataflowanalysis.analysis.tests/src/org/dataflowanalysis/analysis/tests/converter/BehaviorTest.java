package org.dataflowanalysis.analysis.tests.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.dataflowanalysis.analysis.converter.BehaviorConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BehaviorTest {
    private BehaviorConverter converter;
    
    @BeforeEach
    public void init() {
        converter=new BehaviorConverter();
    }
    
    @Test
    @DisplayName("Test Behavior Conversion")
    public void testBehavior(){
        String behavior = "(A.A && TRUE) || !B.B";
        assertEquals(behavior,converter.termToString(converter.stringToTerm(behavior)));
    }
}
