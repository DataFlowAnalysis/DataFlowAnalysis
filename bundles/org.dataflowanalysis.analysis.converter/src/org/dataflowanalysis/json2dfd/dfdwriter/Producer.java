package org.dataflowanalysis.json2dfd.dfdwriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.dataflowanalysis.dfd.dataflowdiagram.dataflowdiagramFactory;
import org.dataflowanalysis.json2dfd.Flow;
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
	
	private Map<String, Node> nodesMap;
		
	public Producer() {
		dfdFactory = dataflowdiagramFactory.eINSTANCE;
		ddFactory = datadictionaryFactory.eINSTANCE;
		rs = new ResourceSetImpl();	
		
		nodesMap = new HashMap<String, Node>();
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
	     } 
	     catch (IOException e) {
	        throw new RuntimeException(e);
	     }
	}
	
	public void produce(String name, List<String> externalEntities, List<String> services, List<Flow> flows) {
		Resource dfdResource = createAndAddResource("target/"+name+".dataflowdiagram", new String[] {"dataflowdiagram"} ,rs);
		Resource ddResource = createAndAddResource("target/"+name+".datadictionary", new String[] {"datadictionary"} ,rs);

		DataFlowDiagram dfd = dfdFactory.createDataFlowDiagram();
		DataDictionary dd = ddFactory.createDataDictionary();
 
		dfdResource.getContents().add(dfd);
		ddResource.getContents().add(dd);
		
		for(String entityName : externalEntities) {
			var external = dfdFactory.createExternal();
			external.setEntityName(entityName);
			
			var behaviour = ddFactory.createBehaviour();
			external.setBehaviour(behaviour);
			dd.getBehaviour().add(behaviour);
			
			dfd.getNodes().add(external);
			nodesMap.put(entityName, external);
		}
		
		for(String serviceName : services) {
			var process = dfdFactory.createProcess();
			process.setEntityName(serviceName);
			
			var behaviour = ddFactory.createBehaviour();
			process.setBehaviour(behaviour);
			dd.getBehaviour().add(behaviour);
			
			dfd.getNodes().add(process);
			nodesMap.put(serviceName, process);
		}

		for(Flow flowName: flows) {
			var source = nodesMap.get(flowName.from());
			var dest = nodesMap.get(flowName.to());
			
			var flow = dfdFactory.createFlow();
			flow.setSourceNode(source);
			flow.setDestinationNode(dest);
			dfd.getFlows().add(flow);
			
			var inPin = ddFactory.createPin();
			var outPin = ddFactory.createPin();
			source.getBehaviour().getOutPin().add(outPin);
			dest.getBehaviour().getInPin().add(inPin);
		}
		
		saveResource(dfdResource);
		saveResource(ddResource);
	}
}
