package org.dataflowanalysis.analysis.resource.dfd;

import java.io.File;

import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.dataflowdiagramPackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public final class DFDLoader {
	
	public static DataFlowDiagram loadDFDModel(String pathToModel) {		
		Resource resource = loadModel(pathToModel);
		return (DataFlowDiagram) resource.getContents().get(0);
	}
	
	public static DataDictionary loadDataDictionaryModel(String pathToModel) {
		Resource resource = loadModel(pathToModel);
		return (DataDictionary) resource.getContents().get(0);
	}
	
	private static Resource loadModel(String pathToModel) {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(dataflowdiagramPackage.eNS_URI,
				dataflowdiagramPackage.eINSTANCE);

		File file = new File(pathToModel);
		URI uri = file.isFile() ? URI.createFileURI(file.getAbsolutePath())
				: URI.createURI(pathToModel);
		
		return resourceSet.getResource(uri, true);
	}

}
