package mdpa.dfd.tests;

import static org.junit.jupiter.api.Assertions.*;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import org.junit.jupiter.api.Test;

class MinimalTest {
	

	private static final String RELATIVE_PATH_TO_MINIMAL_EXAMPLE= "?";

	@Test
	void test() {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new dataflowdaigrammodelResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(dataflowdiagrammodelPackage.eNS_URI,
				dataflowdiagrammodelPackage.eINSTANCE);

		File file = new File(RELATIVE_PATH_TO_MINIMAL_EXAMPLE);
		URI uri = file.isFile() ? URI.createFileURI(file.getAbsolutePath())
				: URI.createURI(RELATIVE_PATH_TO_MINIMAL_EXAMPLE);

		Resource resource = resourceSet.getResource(uri, true);
		return (DataFlowDiagram ) resource.getContents().get(0);

}
