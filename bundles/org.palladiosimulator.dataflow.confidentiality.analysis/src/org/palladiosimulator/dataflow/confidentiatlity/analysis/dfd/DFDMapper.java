package org.palladiosimulator.dataflow.confidentiatlity.analysis.dfd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import org.eclipse.osgi.framework.util.ArrayMap;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DFDCharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd.DFDActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd.DFDActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.ActionSequence;

import mdpa.dfd.datadictionary.*;
import mdpa.dfd.datadictionary.Behaviour;
import mdpa.dfd.datadictionary.DataDictionary;
import mdpa.dfd.datadictionary.LabelType;
import mdpa.dfd.dataflowdiagram.DataFlowDiagram;
import mdpa.dfd.dataflowdiagram.Flow;
import mdpa.dfd.dataflowdiagram.Node;

public class DFDMapper {

	
	public static List<ActionSequence> findAllSequencesInDFD(DataFlowDiagram dfd, DataDictionary dataDictionary) { // TODO:
		 List<ActionSequence> sequences = new ArrayList<>();
		for (AbstractAssignment assigment : getAllAssignments(dataDictionary)) {
			List<Flow> inputFlows = new ArrayList<>();
			for (Flow flow: dfd.getFlows()) {
				if (assigment.getInputPins().stream().map(p -> p.getId()).toList().contains(flow.getDestinationPin().getId())) {
					inputFlows.add(flow);
				}
			}
			List<Node> previousNodes = new ArrayList<>();
			for (Flow flow : inputFlows) {
				previousNodes.add(flow.getSourceNode());
			}
			List<CharacteristicValue> nodeCharacteristics = new ArrayList<>();			
			Node node = findNodeFromAssignment(assigment, inputFlows, dfd);
			nodeCharacteristics.addAll(node.getProperties().stream().map(label -> new DFDCharacteristicValue((LabelType) label.eContainer(), label)).toList());
			DFDActionSequenceElement element = new DFDActionSequenceElement(new ArrayList<DataFlowVariable>(), nodeCharacteristics, node.getEntityName(), node, previousNodes, assigment, inputFlows);
			List<AbstractActionSequenceElement<?>> actionSequence = new ArrayList<AbstractActionSequenceElement<?>>();
			actionSequence.add(element);
			sequences.add(new DFDActionSequence(actionSequence));
		}
		
		return sequences;
		}
	
	private static Map<Node, ArrayList<Node>> getMapOfOutgoingEdges(List<Flow> flows) {
		 Map<Node, ArrayList<Node>> outgoingEdges = new HashMap();
		 for (Flow flow : flows) {
	            Node sourceNode = flow.getSourceNode();
	            Node destinationNode = flow.getDestinationNode();

	            if (outgoingEdges.containsKey(sourceNode)) {
	                outgoingEdges.get(sourceNode).add(destinationNode);
	            } else {
	                ArrayList<Node> destinations = new ArrayList<>();
	                destinations.add(destinationNode);
	                outgoingEdges.put(sourceNode, destinations);
	            }
	        }
		 
		 return outgoingEdges;
	}
	
	private static Node findNodeFromAssignment(AbstractAssignment assignemnt, List<Flow> flows, DataFlowDiagram dfd) {
		if (flows.size() > 0) {
			return flows.get(0).getDestinationNode();
		}
		for (Node node : dfd.getNodes()) {
			if (node.getBehaviour().getAssignment().stream().map(n -> n.getId()).toList().contains(assignemnt.getId())) return node;
		}
		return null;
	}
	
	private static List<AbstractAssignment> getAllAssignments(DataDictionary dataDictionary) {
		List<AbstractAssignment> assignments = new ArrayList<>();
		for (Behaviour behaviour : dataDictionary.getBehaviour()) {
			assignments.addAll(behaviour.getAssignment());
		}		
		return assignments;
	}
	
	private static List<Node> getStartNodes(List<Flow> flows) {
		List<Node> startNodes = new ArrayList<Node>();
		for (var flow: flows) {
			var sourceNode = flow.getSourceNode();
			if(!startNodes.contains(sourceNode)) {
				startNodes.add(sourceNode);
			}
		}
		
		
		for (var flow:flows) {
			var destinationNode = flow.getDestinationNode();
			if(startNodes.contains(destinationNode)) {
				startNodes.remove(destinationNode);
			}
		}
		return startNodes;
	}

	private static List<List<Node>> findStrand(List<Node> currentStrand, Node start, Map<Node, ArrayList<Node>> mapOfOutgoingEdges) {
		List<List<Node>> strands = new ArrayList<List<Node>>();
		var nodesWithInGoingEdgeFromStart = mapOfOutgoingEdges.get(start);
		
		List<Node> currentStrandCopy = new ArrayList<>(currentStrand);
		currentStrandCopy.add(start);
		
		if(nodesWithInGoingEdgeFromStart == null) {
			strands.add(currentStrandCopy);
			return strands;
		}
		else {
			for (var nodeWithInGoingEdgeFromStrart : nodesWithInGoingEdgeFromStart) {
				strands.addAll(findStrand(currentStrandCopy, nodeWithInGoingEdgeFromStrart, mapOfOutgoingEdges));
			}
		}
		
		return strands;
		
	}

	private static DFDActionSequence convertNodeStrandToDFDActionSequence(List<Node> nodes) {
		List<AbstractActionSequenceElement<?>> actionSequence = new ArrayList<AbstractActionSequenceElement<?>>();
		var previousNode = nodes.get(0);
		for (var currentNode : nodes) {
			actionSequence.add(convertNodeToDFDActionSequenceElement(currentNode, previousNode));
			previousNode = currentNode;
		}
		return new DFDActionSequence(actionSequence);
	}

	private static DFDActionSequenceElement convertNodeToDFDActionSequenceElement(Node node, Node previousNode) {
		List<DataFlowVariable> dataFlowVariables = new ArrayList<DataFlowVariable>();

		List<CharacteristicValue> nodeCharacteristics = new ArrayList<CharacteristicValue>();
		for (var label : node.getProperties()) {
			nodeCharacteristics.add(new DFDCharacteristicValue((LabelType) label.eContainer(), label));
		}

		return new DFDActionSequenceElement(dataFlowVariables, nodeCharacteristics, node.getEntityName(), node,
				null, null, null);

	}

}
