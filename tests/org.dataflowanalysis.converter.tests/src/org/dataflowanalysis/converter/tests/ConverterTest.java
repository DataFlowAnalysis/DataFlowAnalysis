package org.dataflowanalysis.converter.tests;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.utils.LoggerManager;

public class ConverterTest {
    private static final Path current = Paths.get(System.getProperty("user.dir"));
    protected static final Path TEST_JSONS = current.getParent()
            .getParent()
            .resolve("bundles")
            .resolve("org.dataflowanalysis.examplemodels")
            .resolve("models");
    protected static final String TEST_MODELS = "org.dataflowanalysis.examplemodels";
    private final Logger logger = LoggerManager.getLogger(ConverterTest.class);

    protected void cleanup(String path) {
        (new File(path)).delete();
        logger.info("Removed: " + path);
    }
}
