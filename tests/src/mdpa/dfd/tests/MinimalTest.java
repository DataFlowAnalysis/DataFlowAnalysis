package mdpa.dfd.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Collections;

import mdpa.dfd.diagram.dataflowdiagrammodel.DataFlowDiagram;
import mdpa.dfd.diagram.dataflowdiagrammodel.DataFlowDiagramModelPackage;
import mdpa.dfd.diagram.dataflowdiagrammodel.impl.DataFlowDiagramModelFactoryImpl;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.Test;

class MinimalTest {

	private static final String RELATIVE_PATH_TO_MINIMAL_EXAMPLE = "resources/MinimalModel.dataflowdiagrammodel";
	private DataFlowDiagram dfd;

	@Test
	void loadAndSave() {
		dfd = loadInstance();
		dfd = changeInstance(dfd);
		saveChangedInstance(dfd);
	}

	private DataFlowDiagram loadInstance() {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(DataFlowDiagramModelPackage.eNS_URI,
				DataFlowDiagramModelPackage.eINSTANCE);

		File file = new File(RELATIVE_PATH_TO_MINIMAL_EXAMPLE);
		URI uri = file.isFile() ? URI.createFileURI(file.getAbsolutePath())
				: URI.createURI(RELATIVE_PATH_TO_MINIMAL_EXAMPLE);
		
		Resource resource = resourceSet.getResource(uri, true);
		return (DataFlowDiagram) resource.getContents().get(0);

	}

	private DataFlowDiagram changeInstance(DataFlowDiagram dfd) {
		dfd.getFlows().get(0).setEntityName("ExcitingFlow");
		return dfd;
	}

	private void saveChangedInstance(DataFlowDiagram dfd) {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
		.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(DataFlowDiagramModelPackage.eNS_URI,
		DataFlowDiagramModelPackage.eINSTANCE);

		Resource resource = resourceSet.createResource(URI.createFileURI("output/changedModel.dataflowdiagrammodel"));

		resource.getContents().add(dfd);
		assertDoesNotThrow(() -> resource.save(Collections.EMPTY_MAP));
	}

}
