package org.palladiosimulator.dataflow.confidentiatlity.analysis.dfd;

import java.io.File;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import mdpa.dfd.dataflowdiagram.DataFlowDiagram;
import mdpa.dfd.dataflowdiagram.dataflowdiagramPackage;

;

public final class DFDLoader {
	
	public static DataFlowDiagram loadDFDModel(String pathToModel) {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(dataflowdiagramPackage.eNS_URI,
				dataflowdiagramPackage.eINSTANCE);

		File file = new File(pathToModel);
		URI uri = file.isFile() ? URI.createFileURI(file.getAbsolutePath())
				: URI.createURI(pathToModel);
		
		Resource resource = resourceSet.getResource(uri, true);
		return (DataFlowDiagram) resource.getContents().get(0);
	}

}
