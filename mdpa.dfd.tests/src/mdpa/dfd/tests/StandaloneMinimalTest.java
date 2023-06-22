package mdpa.dfd.tests;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Paths;

public class StandaloneMinimalTest {
	
	private static final String PLUGIN_PATH = "mdpa.dfd.tests";
	private static final String DFD_MODEL_PATH = "resources/MinimalModel.dataflowdiagrammodel";


	@BeforeAll
	static void setUpBeforeAll() throws Exception {
		initStandalone();
	}

	@BeforeEach
	void setUpBeforeEach() throws Exception {
	}

	@Test
	void test() {
		URI uri = createPluginURI(DFD_MODEL_PATH);
		Resource resource = // ??
	
    private URI createPluginURI(String relativePath) {
        String path = Paths.get(PLUGIN_PATH, "models", relativePath)
            .toString();
        return URI.createPlatformPluginURI(path, false);
    }
    
    private static void initStandalone() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("dataflowdiagrammodel", new XMIResourceFactoryImpl());
		
    }

}
