package org.dataflowanalysis.analysis.tests.converter;

import java.io.File;

public class ConverterTest {
    protected static final String packagePath = "../org.dataflowanalysis.analysis.testmodels/models/ConverterTest/";

    protected void cleanup(String path) {
        (new File(path)).delete();
    }
}
