package org.dataflowanalysis.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.converter.microsecend.*;
import org.dataflowanalysis.converter.webdfd.*;
import org.dataflowanalysis.dfd.datadictionary.*;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.dataflowdiagram.*;
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
	
	private Map<String, Node> nodesMap;
	private Map<Node,List<String>> nodeToLabelNames;
	private Map<String,LabelType> labelTypeMap;
	private Map<String, Label> labelMap;
			
	public ProcessJSON() {
		dfdFactory = dataflowdiagramFactory.eINSTANCE;
		ddFactory = datadictionaryFactory.eINSTANCE;
		rs = new ResourceSetImpl();
		
		nodesMap = new HashMap<>();
		nodeToLabelNames = new HashMap<>();
		labelTypeMap = new HashMap<>();
		labelMap = new HashMap<>();
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
		
		
		
		Resource dfdResource = createAndAddResource(name+".dataflowdiagram", new String[] {"dataflowdiagram"} ,rs);
		Resource ddResource = createAndAddResource(name+".datadictionary", new String[] {"datadictionary"} ,rs);

		DataFlowDiagram dfd = dfdFactory.createDataFlowDiagram();
		DataDictionary dd = ddFactory.createDataDictionary();
 
		dfdResource.getContents().add(dfd);
		ddResource.getContents().add(dd);
		
		for(ExternalEntity ee : micro.externalEntities()) {
			var external = dfdFactory.createExternal();
			external.setEntityName(ee.name());
			
			dfd.getNodes().add(external);
			nodesMap.put(ee.name(), external);
			nodeToLabelNames.put(external, ee.stereotypes());
		}
		
		for(Service service : micro.services()) {
			var process = dfdFactory.createProcess();
			process.setEntityName(service.name());
			
			dfd.getNodes().add(process);
			nodesMap.put(service.name(), process);
			nodeToLabelNames.put(process, service.stereotypes());
		}
		
		LabelType annotation = ddFactory.createLabelType();
			annotation.setEntityName("stereotype");
			labelTypeMap.put("stereotype", annotation);
			dd.getLabelTypes().add(annotation);
		
		for (Node node : nodesMap.values()) {
			var behaviour = ddFactory.createBehaviour();
			node.setBehaviour(behaviour);
			
			var assignment = ddFactory.createAssignment();
			
			assignment.getOutputLabels().addAll(createLabels(nodeToLabelNames.get(node),dd,annotation));

			behaviour.getAssignment().add(assignment);
			
			dd.getBehaviour().add(behaviour);
		}

		for(InformationFlow iflow : micro.informationFlows()) {
			var source = nodesMap.get(iflow.sender());
			var dest = nodesMap.get(iflow.receiver());
			
			var flow = dfdFactory.createFlow();
			flow.setSourceNode(source);
			flow.setDestinationNode(dest);
			flow.setEntityName(iflow.sender()+"->"+iflow.receiver());
			
			
			var inPin = ddFactory.createPin();
			var outPin = ddFactory.createPin();
			source.getBehaviour().getOutPin().add(outPin);
			dest.getBehaviour().getInPin().add(inPin);
			
			flow.setDestinationPin(inPin);
			flow.setSourcePin(outPin);
			dfd.getFlows().add(flow);
		}
		
		for(Node node : nodesMap.values()) {
			var behaviour = node.getBehaviour();
			//NodeAssigment
			Assignment template = (Assignment) behaviour.getAssignment().get(0);
			if (behaviour.getOutPin().size() == 0) {}
			else {
				for (Pin outPin : behaviour.getOutPin()) {
					Assignment assignment = ddFactory.createAssignment();
					
					assignment.getInputPins().addAll(behaviour.getInPin());
					assignment.setOutputPin(outPin);
					
					assignment.getOutputLabels().addAll(template.getOutputLabels());
					assignment.setTerm(ddFactory.createTRUE());
					
					behaviour.getAssignment().add(assignment);			
				}
								
				behaviour.getAssignment().remove(template);
			}
			
			
			//ForwardAssignment
			for (Pin pin : behaviour.getOutPin()) {
				for (Label label : labelMap.values()) {
					Assignment assignment = ddFactory.createAssignment();
					assignment.setOutputPin(pin);
					assignment.getInputPins().addAll(behaviour.getInPin());
					assignment.getOutputLabels().add(label);
					
					LabelReference labelReference = ddFactory.createLabelReference();
					labelReference.setLabel(label);
					
					assignment.setTerm(labelReference);
					behaviour.getAssignment().add(assignment);
				}
			}
		}
		
		saveResource(dfdResource);
		saveResource(ddResource);
	}
	
	/*public List<Label> createLabels(List<String> labelNames, DataDictionary dd) {
		List<Label> labels = new ArrayList<>();
		for (String labelName : labelNames) {
			LabelType labelType;
			if (labelTypeMap.containsKey(labelName)) labelType = labelTypeMap.get(labelName);
			else {
				labelType = ddFactory.createLabelType();
				labelType.setEntityName(labelName);
				labelTypeMap.put(labelName, labelType);
				dd.getLabelTypes().add(labelType);
			}
			if (labelMap.containsKey(labelName)) {
				labels.add(labelMap.get(labelName));
			}
			else {
				Label label = ddFactory.createLabel();
				label.setEntityName(labelName);
				labelType.getLabel().add(label);
				labels.add(label);
				labelMap.put(labelName, label);
			}		
		}	
		return labels;
	}*/
	
	public List<Label> createLabels(List<String> labelNames, DataDictionary dd, LabelType annotation) {
		List<Label> labels = new ArrayList<>();
		for (String labelName : labelNames) {
			if (labelMap.containsKey(labelName)) {
				labels.add(labelMap.get(labelName));
			}
			else {
				Label label = ddFactory.createLabel();
				label.setEntityName(labelName);
				annotation.getLabel().add(label);
				labels.add(label);
				labelMap.put(labelName, label);
			}		
		}	
		return labels;
	}
	
	/*private void annotateForwardingAssignments(Behaviour behaviour) {
		for (Pin pin : behaviour.getOutPin()) {
			for (Label label : labelMap.values()) {
				Assignment assignment = ddFactory.createAssignment();
				assignment.setOutputPin(pin);
				assignment.getInputPins().addAll(behaviour.getIn());
				assignment.getOutputLabels().add(label);
				
				LabelReference labelReference = ddFactory.createLabelReference();
				labelReference.setLabel(label);
				
				assignment.setTerm(labelReference);
				behaviour.getAssignment().add(assignment);
			}
		}
	}*/
		
	public void processWeb(String file, DFD webdfd) {
		nodesMap = new HashMap<String, Node>();
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
				node.setId(child.id());
				
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
				nodesMap.put(child.id(), node);
			}
			else if(type[0].equals("edge")){
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
