package org.palladiosimulator.dataflow.confidentiatlity.analysis.dfd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

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
	
	public static List<ActionSequence> findAllSequencesInDFD(DataFlowDiagram dfd, DataDictionary dataDictionary) { //TODO: hier noch auf die Pins überprüfen
        Set<Node> visited = new HashSet<>();
        List<ActionSequence> strands = new ArrayList<>();
        
        var flows = dfd.getFlows();
		for (var dataFlow: flows) {
			Node inputNode = dataFlow.getSourceNode();

            if (!visited.contains(inputNode)) {
                List<Node> currentStrand = depthFirstSearch(flows, inputNode, visited);
                strands.add(convertNodeStrandToDFDActionSequence(currentStrand));
            };
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
	
	   private static List<Node> depthFirstSearch(List<Flow> flows, Node start, Set<Node> visited) {
	        Stack<Node> stack = new Stack<>();
	        List<Node> visitedNodesInStrand = new ArrayList<Node>();
	        List<Node> currentStrand = new ArrayList<Node>();
	        
	        stack.push(start);
	        visited.add(start);
	        visitedNodesInStrand.add(start);
	        currentStrand.add(start);
	        
	        while (!stack.isEmpty()) {
	            Node currentNode = stack.pop();
	            
	            for (Flow flow : flows) {
	                if (flow.getSourceNode() == currentNode) {
	                    Node neighbour = flow.getDestinationNode();
	                    
	                    if (!visited.contains(neighbour)) {
	                        stack.push(neighbour);
	                        visited.add(neighbour);
	                        visitedNodesInStrand.add(neighbour);
	                        currentStrand.add(neighbour);
	                    }
	                }
	            }
	        }
	        
	        return currentStrand;
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
