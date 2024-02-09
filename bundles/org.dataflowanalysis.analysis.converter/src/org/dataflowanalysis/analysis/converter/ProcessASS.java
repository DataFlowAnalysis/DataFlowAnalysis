package org.dataflowanalysis.analysis.converter;

import java.util.HashMap;
import java.util.List;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.dataflowanalysis.analysis.core.*;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMActionSequenceElement;

import org.dataflowanalysis.analysis.pcm.core.user.*;
import org.palladiosimulator.pcm.core.entity.Entity;

import org.dataflowanalysis.dfd.datadictionary.*;
import org.dataflowanalysis.dfd.dataflowdiagram.*;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

@SuppressWarnings({"unchecked","rawtypes"})
public class ProcessASS {
	
	private Map<Entity, Node> dfdNodeMap = new HashMap<>();
	private DataDictionary dd = datadictionaryFactory.eINSTANCE.createDataDictionary();
	private DataFlowDiagram dfd = dataflowdiagramFactory.eINSTANCE.createDataFlowDiagram();
			
	public DataDictionary getDictionary() {
		return this.dd;
	}
	
	public DataFlowDiagram getDataFlowDiagram() {
		return this.dfd;
	}
	
	public void transform(List<ActionSequence> ass) {
		for (ActionSequence actionSequence : ass) {
			Node previousNode = null;
			for (AbstractActionSequenceElement<?> ASE : actionSequence.getElements()) {
				if(ASE instanceof AbstractPCMActionSequenceElement) {
					previousNode = processActionSequenceElement((AbstractPCMActionSequenceElement)ASE, previousNode);
				}
			}
		}
	}
	
	private Node processActionSequenceElement(AbstractPCMActionSequenceElement pcmASE, Node previousDFDNode) {
		Node dfdNode = getOrCreateDFDNode(pcmASE);
		
		// Add a flow between previous node and current node
		createFlows(previousDFDNode, dfdNode, pcmASE);
		
		return dfdNode;
	}
	
	
	private void createFlows(Node source, Node dest, AbstractPCMActionSequenceElement pcmASE) {
		if(source == null || dest == null) {
			return;
		}
		List<DataFlowVariable> FlowVariables = pcmASE.getAllDataFlowVariables();
		for(DataFlowVariable flowVariable : FlowVariables) {
			String flowName = flowVariable.variableName();
			
			Optional<Flow> optFlow = dfd.getFlows().stream().filter(f -> (
					f.getSourceNode().equals(source) && 
					f.getDestinationNode().equals(dest) &&
					f.getEntityName().equals(flowName))).findFirst();
			
			if(optFlow.isPresent()) {
				return; //possibly modify behavior later on
			}
			
			Flow newFlow = dataflowdiagramFactory.eINSTANCE.createFlow();
			newFlow.setSourceNode(source);
			newFlow.setDestinationNode(dest);
			newFlow.setEntityName(flowName);
			
			//Assumption in Palladio: Each flows between two nodes with the same paramenters/that are called the same use the same pin
			Pin sourceOutPin = findOrCreateOutputPin(source, flowName);
			Pin destInPin = findOrCreateInputPin(dest, flowName);
			newFlow.setSourcePin(sourceOutPin);
			newFlow.setDestinationPin(destInPin);
			
			// modify behavior
			ForwardingAssignment forwarding = datadictionaryFactory.eINSTANCE.createForwardingAssignment();
			forwarding.setOutputPin(sourceOutPin);
			source.getBehaviour().getAssignment().add(forwarding);
			
			this.dfd.getFlows().add(newFlow);
		}		
	}
	
	// A pin is equivalent if the same parameters are passed
	private Pin findOrCreateOutputPin(Node source, String parameters) {
		Optional<Pin> optPin = source.getBehaviour().getOutPin().stream().filter(p -> p.getEntityName().equals(parameters)).findAny();
		if(optPin.isPresent()) {
			return optPin.get();
		}
		Pin pin = datadictionaryFactory.eINSTANCE.createPin();
		pin.setEntityName(parameters);
		source.getBehaviour().getOutPin().add(pin);
		return pin;
	}
	
	// I know it is basically a code clone from above but i do not care
	private Pin findOrCreateInputPin(Node dest, String parameters) {
		Optional<Pin> optPin = dest.getBehaviour().getInPin().stream().filter(p -> p.getEntityName().equals(parameters)).findAny();
		if(optPin.isPresent()) {
			return optPin.get();
		}
		Pin pin = datadictionaryFactory.eINSTANCE.createPin();
		pin.setEntityName(parameters);
		dest.getBehaviour().getInPin().add(pin);
		return pin;
	}
		
	private Node getOrCreateDFDNode(AbstractPCMActionSequenceElement pcmASE) {
		Node dfdNode = dfdNodeMap.get(pcmASE.getElement());
		// check if a corresponding node has already been created
		if(dfdNode == null) {
			// if not, create a node
			dfdNode = createCorrespondingDFDNode(pcmASE);
			dfdNodeMap.put(pcmASE.getElement(), dfdNode);
		}
		
		// add all node characteristics to the current node (if not already present)
		addNodeCharacteristicsToNode(dfdNode, pcmASE.getAllNodeCharacteristics());
		
		return dfdNode;
	}
	
	private Node createCorrespondingDFDNode(AbstractPCMActionSequenceElement pcmASE) {
		Node node;
		
		if (pcmASE instanceof UserActionSequenceElement) {
			node = dataflowdiagramFactory.eINSTANCE.createExternal();
		} else { //if (pcmASE instanceof SEFFActionSequenceElement)
			node = dataflowdiagramFactory.eINSTANCE.createProcess();
		}
		
		Behaviour behaviour = datadictionaryFactory.eINSTANCE.createBehaviour();
		node.setEntityName(pcmASE.getElement().getEntityName());
		node.setId(pcmASE.getElement().getId());
		node.setBehaviour(behaviour);
		dd.getBehaviour().add(behaviour);
		dfd.getNodes().add(node);
		return node;
	}
	
	private void addNodeCharacteristicsToNode(Node node, List<CharacteristicValue> charValues) {
		for(CharacteristicValue charValue : charValues) {
			Label label = getOrCreateDFDLabel(charValue);
			if(!node.getProperties().contains(label)) {
				node.getProperties().add(label);
			}
		}
	}
	
	//inefficient as hell
	private Label getOrCreateDFDLabel(CharacteristicValue charValue) {
		LabelType type = null;
		for(LabelType existingType : dd.getLabelTypes()) {
			if(existingType.getEntityName().equals(charValue.getTypeName())) {
				type = existingType;
				break;
			}
		}
		if(type == null) {
			type = datadictionaryFactory.eINSTANCE.createLabelType();
			type.setEntityName(charValue.getTypeName());
			this.dd.getLabelTypes().add(type);
		}
		
		Label label = null;
		for(Label existingLabel : type.getLabel()) {
			if(existingLabel.getEntityName().equals(charValue.getValueName())) {
				label = existingLabel;
				break;
			}
		}
		if(label == null) {
			label = datadictionaryFactory.eINSTANCE.createLabel();
			label.setEntityName(charValue.getValueName());
			type.getLabel().add(label);
		}
		
		return label;
	}
	
	public void saveModel(String path, String ending, EObject model) {

        Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        Map<String, Object> m = reg.getExtensionToFactoryMap();
        m.put(ending, new XMIResourceFactoryImpl());

        // Obtain a new resource set
        ResourceSet resSet = new ResourceSetImpl();

        // create a resource
        Resource resource = resSet.createResource(URI
                .createURI(path));
        // Get the first model element and cast it to the right type, in my
        // example everything is hierarchical included in this first node
        resource.getContents().add(model);

        // now save the content.
        try {
            resource.save(Collections.EMPTY_MAP);
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
	}
}
