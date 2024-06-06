package org.dataflowanalysis.converter.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.dataflowanalysis.converter.BehaviorConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class BehaviorTest {
    private BehaviorConverter converter;

    @BeforeEach
    public void init() {
        converter = new BehaviorConverter();
    }

    @ParameterizedTest
    @ValueSource(strings = {"TRUE || FALSE", "TypeA.ValueA && TypeB.ValueB", "TypeA.ValueA || TypeB.ValueB", "!TypeA.ValueA && TypeB.ValueB",
            "TypeA.ValueA || !TypeB.ValueB", "(TypeA.ValueA && TypeB.ValueB) || TypeC.ValueC", "!(TypeA.ValueA || TypeB.ValueB) && TypeC.ValueC",
            "((TypeA.ValueA && TRUE) || !TypeB.ValueB) || FALSE", "(!TypeA.ValueA && TypeB.ValueB) || (TypeC.ValueC && !TypeD.ValueD)",
            "((TypeA.ValueA || !TypeB.ValueB) && TypeC.ValueC) || (TypeD.ValueD && !(TypeE.ValueE || TypeF.ValueF))",
            "!((TypeA.ValueA && (TypeB.ValueB || !TypeC.ValueC)) || (!(TypeD.ValueD && TypeE.ValueE) && (TypeF.ValueF || TypeG.ValueG)))"})
    @DisplayName("Test Behavior Conversion")
    void testBehaviorConversion(String behavior) {
        assertEquals(behavior, converter.termToString(converter.stringToTerm(behavior)));
    }
}
