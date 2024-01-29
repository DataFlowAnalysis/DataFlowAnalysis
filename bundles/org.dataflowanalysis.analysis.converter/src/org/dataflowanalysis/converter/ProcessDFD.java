package org.dataflowanalysis.converter;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dataflowanalysis.converter.webdfd.*;
import org.dataflowanalysis.dfd.datadictionary.*;
import org.dataflowanalysis.dfd.dataflowdiagram.*;
import org.dataflowanalysis.dfd.dataflowdiagram.Process;

public class ProcessDFD {
	
	Map<Pin, String> mapInputPinToFlowName = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public void parse(String dfdFile, String ddFile, String outFile) {	
		//Init and get resources for dfd, dd model instances
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		rs.getPackageRegistry().put(dataflowdiagramPackage.eNS_URI, dataflowdiagramPackage.eINSTANCE);
		
		
		Resource dfdResource = rs.getResource(URI.createFileURI(dfdFile), true);
		Resource ddResource = rs.getResource(URI.createFileURI(ddFile), true);		
		
		DataFlowDiagram dfd = (DataFlowDiagram) dfdResource.getContents().get(0);
		DataDictionary dd = (DataDictionary) ddResource.getContents().get(0);
				
		List<Child> children = new ArrayList<>();
		
		for (Node node : dfd.getNodes()) {
			String text = node.getEntityName();
			String id = node.getId();
			String type;
			if (node instanceof Process) {
				type="node:function";
			} else if (node instanceof Store) {
				type="node:storage";
			} else if (node instanceof External) {
				type="node:input-output";
			}
			else {
				type="error";
			}
			List<Port> ports = new ArrayList<>();
			for (Pin pin : node.getBehaviour().getInPin()) {
				ports.add(new Port(null,pin.getId(),"port:dfd-input",new ArrayList<>()));
			}
			//behavior is stored in outpin
			for (Pin pin : node.getBehaviour().getOutPin()) {
				String behaviour="Replace";
				ports.add(new Port(behaviour,pin.getId(),"port:dfd-output",new ArrayList<>()));
			}
			children.add(new Child(text, new ArrayList<>(), ports,id,type,null,null,new ArrayList<>()));
		}
		
		for(Flow flow: dfd.getFlows()) {
			String id = flow.getId();
			String type = "edge:arrow";
			String sourceId= flow.getSourcePin().getId();
			String targetId = flow.getDestinationPin().getId();
			String text=flow.getEntityName();
			children.add(new Child(text,null,null,id,type,sourceId,targetId,new ArrayList<>()));
			
		}
		
		DFD output = new DFD (new Model("graph","root",children),new ArrayList<>());
		
		ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try {
            // Serialize object to JSON and write to a file
            objectMapper.writeValue(new File(outFile), output);
        } catch (IOException e) {
            e.printStackTrace();
        }
	
		
		/*
		//Init Flow map
		initFlowMap(dfd.getFlows());
		
		JSONObject everything = new JSONObject();
		
		//Create Label type and values entries
		JSONArray labelTypesArray = new JSONArray();
		
		for (LabelType labelType: dd.getLabelTypes()) {
			JSONObject labelTypeObject = new JSONObject();
			labelTypeObject.put("id", labelType.getId());
			labelTypeObject.put("name", labelType.getEntityName());
			JSONArray labelArray = new JSONArray();
			for (Label label : labelType.getLabel())  {				
				JSONObject labelObject = new JSONObject();
				labelObject.put("id", label.getId());
				labelObject.put("text", label.getEntityName());
				labelArray.add(labelObject);
			}
			labelTypeObject.put("values", labelArray);
			labelTypesArray.add(labelTypeObject);
		}
		
		//Create model entry
		JSONObject model = new JSONObject();
		model.put("type", "graph");
		model.put("id", "root");
		
		//Create model children (nodes and flows)
		JSONArray children = new JSONArray();
		
		for (Node node : dfd.getNodes()) {
			JSONObject child = new JSONObject();
			child.put("text", node.getEntityName());
			child.put("id", node.getId());
			child.put("features", new JSONObject()); 
			child.put("children", new JSONArray());
			child.put("ports", createPortsAndBehaviours(node));
			
			//Add labels
			JSONArray labelArray = new JSONArray();			
			for (Label label : node.getProperties()) {
				JSONObject labelObject = new JSONObject();
				labelObject.put("labelTypeId", ((LabelType)label.eContainer()).getId());
				labelObject.put("labelTypeValueId", label.getId());
				labelArray.add(labelObject);
			}
			
			child.put("labels", labelArray);
			
			//Infer and add type
			if (node instanceof Process) {
				child.put("type", "node:function");
			} else if (node instanceof Store) {
				child.put("type", "node:storage");
			} else if (node instanceof External) {
				child.put("type", "node:input-output");
			}
			
			children.add(child);
		}
		
		//Create Flows
		for (Flow flow : dfd.getFlows()) {
			JSONObject child = new JSONObject();
			child.put("text", flow.getEntityName());
			child.put("id", flow.getId());
			child.put("type", "edge:arrow");
			child.put("features", new JSONObject());
			child.put("children", new JSONArray());
			child.put("sourceId", flow.getSourcePin().getId());
			child.put("targetId", flow.getDestinationPin().getId());
			children.add(child);
		}
		
		model.put("children", children);
		everything.put("labelTypes", labelTypesArray);
		everything.put("model", model);
		
		//Write created JsonObject to file
		try {
	         FileWriter file = new FileWriter(outFile + ".json");
	         file.write(everything.toJSONString());
	         file.close();
	    } catch (IOException e) {
	         e.printStackTrace();
	    }
	}
	
		
	@SuppressWarnings("unchecked")
	private JSONArray createPortsAndBehaviours(Node node) {
		JSONArray ports = new JSONArray();
		
		Behaviour behaviour = node.getBehaviour();
		if (behaviour == null) return ports;
		
		Map<Pin, List<AbstractAssignment>> mapPinToAssignments = mapPinToAssignments(node);
		
		for (Pin pin : behaviour.getIn()) {
			JSONObject inPort = new JSONObject();
			inPort.put("id", pin.getId());
			inPort.put("type", "port:dfd-input");
			inPort.put("features", new JSONObject());
			inPort.put("children", new JSONArray());
			ports.add(inPort);
		}
		
		for (Pin pin : behaviour.getOut()) {
			JSONObject outPort = new JSONObject();
			outPort.put("id", pin.getId());
			outPort.put("type", "port:dfd-output");
			outPort.put("features", new JSONObject());
			outPort.put("children", new JSONArray());
			outPort.put("behavior", createBehaviourString(mapPinToAssignments.get(pin)));
			ports.add(outPort);
		}
		
		return ports;
	}
	
	private Map<Pin, List<AbstractAssignment>> mapPinToAssignments(Node node) {
		Map<Pin, List<AbstractAssignment>> mapPinToAssignments = new HashMap<>();
		
		for (AbstractAssignment assignment : node.getBehaviour().getAssignment()) {
			if (mapPinToAssignments.containsKey(assignment.getOutputPin())) {
				mapPinToAssignments.get(assignment.getOutputPin()).add(assignment);
			} else {
				List<AbstractAssignment> list = new ArrayList<>();
				list.add(assignment);
				mapPinToAssignments.put(assignment.getOutputPin(), list);
			}

		}
		
		return mapPinToAssignments;
	}
	
	private void initFlowMap(List<Flow> flows) {
		for (Flow flow : flows) {
			mapInputPinToFlowName.put(flow.getDestinationPin(), flow.getEntityName());
		}
	}
	
	private String createBehaviourString (List<AbstractAssignment> abstractAssignments) {
		StringBuilder builder = new StringBuilder();
		
		for (AbstractAssignment abstractAssignment : abstractAssignments) {
			if (abstractAssignment instanceof ForwardingAssignment) {
				for (Pin inPin : abstractAssignment.getInputPins()) {
					builder.append("forward ").append(mapInputPinToFlowName.get(inPin)).append("\n");
				}
			} else {			
				Assignment assignment = (Assignment) abstractAssignment;
				String value = getTermValue(assignment.getTerm()) ? "TRUE" : "FALSE";
				
				for (Label label : assignment.getOutputLabels()) {
					try {
					builder.append("set ").append(((LabelType)label.eContainer()).getEntityName()).append(".").append(label.getEntityName()).append(" = ").append(value).append("\n");
					} catch (IllegalArgumentException ex) {
						System.out.println("Caution!! WebEditor cant handle complex Assignments yet. Only TRUE or NOT(TRUE) supported. Everything else ignored");
					}	
				}
			}
		}
		return builder.toString();
	}
	
	//Currently only supports True or False
	private boolean getTermValue(Term term) throws IllegalArgumentException{
		if (term instanceof TRUE) return true;
		if (term instanceof NOT) {
			return !getTermValue(((NOT)term).getNegatedTerm());
		}
		throw new IllegalArgumentException();
	}*/
}
}
