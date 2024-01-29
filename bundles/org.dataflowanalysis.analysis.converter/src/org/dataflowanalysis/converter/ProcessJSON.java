package org.dataflowanalysis.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.converter.microsecend.ExternalEntity;
import org.dataflowanalysis.converter.microsecend.InformationFlow;
import org.dataflowanalysis.converter.microsecend.MicroSecEnd;
import org.dataflowanalysis.converter.microsecend.Service;
import org.dataflowanalysis.converter.webdfd.Child;
import org.dataflowanalysis.converter.webdfd.DFD;
import org.dataflowanalysis.converter.webdfd.Port;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.External;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.dataflowanalysis.dfd.dataflowdiagram.Process;
import org.dataflowanalysis.dfd.dataflowdiagram.Store;
import org.dataflowanalysis.dfd.dataflowdiagram.dataflowdiagramFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;

public class ProcessJSON {
	private dataflowdiagramFactory dfdFactory;
	private datadictionaryFactory ddFactory;
	private ResourceSet rs;	
			
	public ProcessJSON() {
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
	     } 
	     catch (IOException e) {
	        throw new RuntimeException(e);
	     }
	}
	
	public void processMicro(String name, MicroSecEnd micro) {
		List<String> externalEntities = new ArrayList<>();
        List<String> services = new ArrayList<>();
        List<SimpleFlow> flows = new ArrayList<>();
        
		List<InformationFlow> iflows = micro.informationFlows();
        for(InformationFlow flow : iflows) {
        	flows.add(new SimpleFlow(flow.sender(),flow.receiver()));
        }

        for(ExternalEntity ee : micro.externalEntities()) {
        	externalEntities.add(ee.name());
        }
        for (Service service:micro.services()) {
        	services.add(service.name());
        }
		
		Map<String, Node> nodesMap = new HashMap<String, Node>();
		
		Resource dfdResource = createAndAddResource(name+".dataflowdiagram", new String[] {"dataflowdiagram"} ,rs);
		Resource ddResource = createAndAddResource(name+".datadictionary", new String[] {"datadictionary"} ,rs);

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

		for(SimpleFlow flowName: flows) {
			var source = nodesMap.get(flowName.from());
			var dest = nodesMap.get(flowName.to());
			
			var flow = dfdFactory.createFlow();
			flow.setSourceNode(source);
			flow.setDestinationNode(dest);
			flow.setEntityName(flowName.from()+"->"+flowName.to());
			
			
			var inPin = ddFactory.createPin();
			var outPin = ddFactory.createPin();
			source.getBehaviour().getOutPin().add(outPin);
			dest.getBehaviour().getInPin().add(inPin);
			
			flow.setDestinationPin(inPin);
			flow.setSourcePin(outPin);
			dfd.getFlows().add(flow);
		}
		
		saveResource(dfdResource);
		saveResource(ddResource);
	}
	
	public void processWeb(String file, DFD webdfd) {
		Map<String, Node> nodesMap = new HashMap<String, Node>();
		Map<String, Node> pinToNodeMap = new HashMap<String, Node>();
		Map<String, Pin> pinMap = new HashMap<String, Pin>();
		
		
		Resource dfdResource = createAndAddResource(file+".dataflowdiagram", new String[] {"dataflowdiagram"} ,rs);
		Resource ddResource = createAndAddResource(file+".datadictionary", new String[] {"datadictionary"} ,rs);

		DataFlowDiagram dfd = dfdFactory.createDataFlowDiagram();
		DataDictionary dd = ddFactory.createDataDictionary();
 
		dfdResource.getContents().add(dfd);
		ddResource.getContents().add(dd);
		
		for(Child child : webdfd.model().children()) {
			String[] type = child.type().split(":");
			String name=child.text();
			String id=child.id();

			if (type[0].equals("node")){
				Node node;
				if (type[1].equals("function")) {
					node = dfdFactory.createProcess();
				} 
				else if (type[1].equals("storage")) {
					node = dfdFactory.createStore();
				} 
				else if (type[1].equals("input-output")) {
					node = dfdFactory.createExternal();
				}
				else {
					node=null;
				}
				node.setEntityName(name);
				node.setId(id);
				
				var behaviour = ddFactory.createBehaviour();
				node.setBehaviour(behaviour);
				dd.getBehaviour().add(behaviour);
				
				for(Port port :child.ports()) {
					if (port.type().equals("port:dfd-input")) {
						var inPin = ddFactory.createPin();
						node.getBehaviour().getInPin().add(inPin);
						pinMap.put(port.id(), inPin);
					}
					else if (port.type().equals("port:dfd-output")) {
						var outPin = ddFactory.createPin();
						node.getBehaviour().getOutPin().add(outPin);
						pinMap.put(port.id(), outPin);
					}
					pinToNodeMap.put(port.id(),node);
				}
				
				dfd.getNodes().add(node);
				nodesMap.put(id, node);
				System.out.println(nodesMap);
				System.out.println(pinToNodeMap);
			}
			else if(type[0].equals("edge")){
				System.out.println(child.sourceId());
				System.out.println(child.targetId());
				var source = nodesMap.get(child.sourceId());
				var dest = nodesMap.get(child.targetId());
				
				var flow = dfdFactory.createFlow();
				flow.setSourceNode(source);
				flow.setDestinationNode(dest);
				flow.setEntityName(child.text());
				
				flow.setDestinationPin(pinMap.get(child.targetId()));
				flow.setSourcePin(pinMap.get(child.sourceId()));
				dfd.getFlows().add(flow);
			}
		}
				
		saveResource(dfdResource);
		saveResource(ddResource);
	}
}
