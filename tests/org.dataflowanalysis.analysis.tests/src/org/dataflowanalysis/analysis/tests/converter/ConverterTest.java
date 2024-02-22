package org.dataflowanalysis.analysis.tests.converter;

import java.io.File;
import java.nio.file.Paths;

public class ConverterTest {
    protected static final String packagePath = Paths.get("..", "org.dataflowanalysis.analysis.testmodels", "models", "ConverterTest").toString();

    protected void cleanup(String path) {
        (new File(path)).delete();
    }
}
