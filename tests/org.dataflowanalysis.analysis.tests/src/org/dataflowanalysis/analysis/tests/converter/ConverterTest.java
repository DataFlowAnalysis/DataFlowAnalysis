package org.dataflowanalysis.analysis.tests.converter;

import java.io.File;
import java.nio.file.Paths;

import org.apache.log4j.Logger;

public class ConverterTest {
    protected static final String packagePath = Paths.get("..", "org.dataflowanalysis.analysis.testmodels", "models", "ConverterTest").toString();
    private final Logger logger = Logger.getLogger(ConverterTest.class);

    protected void cleanup(String path) {
        (new File(path)).delete();
        logger.info("Removed: " + path);
    }
}
