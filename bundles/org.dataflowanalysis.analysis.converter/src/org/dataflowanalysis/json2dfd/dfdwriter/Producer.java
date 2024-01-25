package org.dataflowanalysis.json2dfd.dfdwriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.dataflowdiagramFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;

public class Producer {
	private dataflowdiagramFactory dfdFactory;
	private datadictionaryFactory ddFactory;
	private ResourceSet rs;		
		
	public Producer() {
		dfdFactory = dataflowdiagramFactory.eINSTANCE;
		ddFactory = datadictionaryFactory.eINSTANCE;
		rs = new ResourceSetImpl();		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Resource createAndAddResource(String outputFile, String[] fileextensions, ResourceSet rs) {
	     for (String fileext : fileextensions) {
	        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put(fileext, new XMLResourceFactoryImpl());
	     }		
	     URI uri = URI.createFileURI(outputFile);
	     Resource resource = rs.createResource(uri);
	     ((ResourceImpl)resource).setIntrinsicIDToEObjectMap(new HashMap());
	     return resource;
	  }
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void saveResource(Resource resource) {
	     Map saveOptions = ((XMLResource)resource).getDefaultSaveOptions();
	     saveOptions.put(XMLResource.OPTION_CONFIGURATION_CACHE, Boolean.TRUE);
	     saveOptions.put(XMLResource.OPTION_USE_CACHED_LOOKUP_TABLE, new ArrayList());
	     try {
	        resource.save(saveOptions);
	     } catch (IOException e) {
	        throw new RuntimeException(e);
	     }
	}
	
	public void produce(List<String> externalEntities, List<String> services) {
		Resource dfdResource = createAndAddResource("target/"+"test"+".dataflowdiagram", new String[] {"dataflowdiagram"} ,rs);
		Resource ddResource = createAndAddResource("target/"+"test"+".datadictionary", new String[] {"datadictionary"} ,rs);

		DataFlowDiagram dfd = dfdFactory.createDataFlowDiagram();
		DataDictionary dd = ddFactory.createDataDictionary();
 
		dfdResource.getContents().add(dfd);
		ddResource.getContents().add(dd);
		
		for(String ee : externalEntities) {
			var external = dfdFactory.createExternal();
			external.setEntityName(ee);
			dfd.getNodes().add(external);
		}
		
		for(String service : services) {
			var process = dfdFactory.createProcess();
			process.setEntityName(service);
			dfd.getNodes().add(process);
		}
		var x = dfdFactory.createProcess();
		dfd.getNodes().add(x);
		
		saveResource(dfdResource);
		saveResource(ddResource);
	}
}
