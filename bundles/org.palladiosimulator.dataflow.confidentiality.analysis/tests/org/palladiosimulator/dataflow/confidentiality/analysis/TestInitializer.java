package org.palladiosimulator.dataflow.confidentiality.analysis;

import org.eclipse.emf.common.util.URI;

import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;
import tools.mdsd.library.standalone.initialization.log4j.Log4jInitilizationTask;

public class TestInitializer {

    private final static String PATH = "org.palladiosimulator.dataflow.confidentiality.analysis";

    private TestInitializer() {
        assert false;
    }

    public static void init() throws StandaloneInitializationException {
        StandaloneInitializerBuilder.builder()
            .registerProjectURI(Activator.class, PATH)
            .addCustomTask(new Log4jInitilizationTask())
            .build()
            .init();
    }

    public static URI getModelURI(final String relativeModelPath) {
        return getRelativePluginURI(relativeModelPath);
    }

    private static URI getRelativePluginURI(final String relativePath) {
        return URI.createPlatformPluginURI("/" + PATH + "/" + relativePath, false);
    }

}