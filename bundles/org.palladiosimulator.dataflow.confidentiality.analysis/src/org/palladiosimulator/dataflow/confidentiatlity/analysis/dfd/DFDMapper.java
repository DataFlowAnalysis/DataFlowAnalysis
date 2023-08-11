package org.palladiosimulator.dataflow.confidentiatlity.analysis.dfd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import mdpa.dfd.dataflowdiagram.Node;

public class DFDMapper {
	
	public static List<ActionSequence> findAllSequencesInDFD(DataFlowDiagram dfd, DataDictionary dataDictionary) { //TODO: hier noch auf die Pins überprüfen
		Map<Node, List<Node>> graph = new HashMap<>();
		for (var dataFlow: dfd.getFlows()) {
			Node inputNode = dataFlow.getDestinationNode();
            Node outputNode = dataFlow.getDestinationNode();
            graph.putIfAbsent(inputNode, new ArrayList<>());
            graph.get(inputNode).add(outputNode);
		}
		
		List<ActionSequence> strands = new ArrayList<ActionSequence>();
		Set<Node> visited = new HashSet<>();
		
		for (Node node : dfd.getNodes()) {
            if (!visited.contains(node)) {
                List<Node> currentStrand = new ArrayList<Node>();
                depthFirstSearch(node, visited, currentStrand, graph);
                strands.add(convertNodeStrandToDFDActionSequence(currentStrand));
            }
        }
		
		return strands;
	}
	
	private static DFDActionSequence convertNodeStrandToDFDActionSequence(List<Node> nodes) {
		List<AbstractActionSequenceElement<?>> actionSequence = new ArrayList<AbstractActionSequenceElement<?>>();
		var previousNode = nodes.get(0); //TODO
		for(var currentNode: nodes) {
			actionSequence.add(convertNodeToDFDActionSequenceElement(currentNode, previousNode));
			previousNode = currentNode;
		}
		return new DFDActionSequence(actionSequence);
	}
	
	private static void depthFirstSearch(Node node, Set<Node> visited, List<Node> currentStrand, Map<Node, List<Node>> graph) {
        visited.add(node);
        currentStrand.add(node);

        List<Node> neighbors = graph.getOrDefault(node, new ArrayList<>());
        for (Node neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                depthFirstSearch(neighbor, visited, currentStrand, graph);
            }
        }
    }
	
	private static DFDActionSequenceElement convertNodeToDFDActionSequenceElement(Node node, Node previousNode) {
		List<DataFlowVariable> dataFlowVariables = new ArrayList<DataFlowVariable>();
		
		List<CharacteristicValue> nodeCharacteristics = new ArrayList<CharacteristicValue>();
		for(var label: node.getProperties()) {
			nodeCharacteristics.add(new DFDCharacteristicValue((LabelType) label.eContainer(),label));
		}
		
		
		return new DFDActionSequenceElement(dataFlowVariables, nodeCharacteristics, node.getEntityName(), node, previousNode);
		
	}
	

}
