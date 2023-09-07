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

import mdpa.dfd.datadictionary.DataDictionary;
import mdpa.dfd.datadictionary.LabelType;
import mdpa.dfd.dataflowdiagram.DataFlowDiagram;
import mdpa.dfd.dataflowdiagram.Flow;
import mdpa.dfd.dataflowdiagram.Node;

public class DFDMapper {

	public static List<ActionSequence> findAllSequencesInDFD(DataFlowDiagram dfd, DataDictionary dataDictionary) { // TODO:
																													// hier
																													// noch
																													// auf
																													// die
																													// Pins
																													// überprüfen
		List<List<Node>> strands = new ArrayList<>();
		List<ActionSequence> sequences = new ArrayList<>();
		var flows = dfd.getFlows();
		Map<Node, ArrayList<Node>> mapOfOutgoingEdges = getMapOfOutgoingEdges(flows);
		var startNodesOfNonConnectedGraphs = getStartNodes(flows);

		for (var startNode : startNodesOfNonConnectedGraphs) {
			List<Node> currentStrand = new ArrayList<Node>();
			strands.addAll(findStrand(currentStrand, startNode, mapOfOutgoingEdges));
		}
		
		for (var strand : strands) {
			sequences.add(convertNodeStrandToDFDActionSequence(strand));
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
				previousNode);

	}

}
