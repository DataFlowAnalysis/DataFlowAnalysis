package org.dataflowanalysis.analysis.tests.converter;

import org.junit.jupiter.api.*;

import java.io.File;

import org.dataflowanalysis.analysis.converter.*;


public class ConverterTest {
    protected static final String packagePath = "../org.dataflowanalysis.analysis.testmodels/models/ConverterTest/";
    protected Converter converter;

    @BeforeEach
    public void setup() {
        converter = new Converter();
    }

    protected void cleanup(String path) {
        (new File(path)).delete();
    }
}
