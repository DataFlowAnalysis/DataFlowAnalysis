package org.dataflowanalysis.analysis.dfd.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.core.AbstractActionSequenceElement;
import org.dataflowanalysis.analysis.core.ActionSequence;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.External;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;

public class DFDActionSequenceFinder {

	/**
	 * Finds all Action Sequences in a dataflowdiagram instance
	 * @param dfd Data Flow Diagram model instance
	 * @param dataDictionary Data Dictionary model instance
	 * @return All Action Sequences
	 */
	public static List<ActionSequence> findAllSequencesInDFD(DataFlowDiagram dfd, DataDictionary dataDictionary) { 
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
			sequences.add(convertNodeStrandToDFDActionSequence(strand, flows));
		}

		return sequences;
	}
	
	/**
	 * Create Map with all outgoing edges for each node from list of all flows
	 * @param flows All flows in the DFD
	 * @return All outgoing edges
	 */
	private static Map<Node, ArrayList<Node>> getMapOfOutgoingEdges(List<Flow> flows) {
		 Map<Node, ArrayList<Node>> outgoingEdges = new HashMap<>();
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
	
	/**
	 * Get List of all Nodes without incoming flows, or External nodes 
	 * @param flows All flows
	 * @return List of External Nodes and Nodes with no incoming flows
	 */
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
			if(startNodes.contains(destinationNode) && !(destinationNode instanceof External)) {
				startNodes.remove(destinationNode);
			}
		}
		return startNodes;
	}

	/**
	 * Get List of all individual information flows (strands) from start to finish recursively
	 * @param currentStrand Strand in building right now
	 * @param start New start node	
	 * @param mapOfOutgoingEdges Map of all outgoing edges
	 * @return List of all strands
	 */
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
	
	/**
	 * Convert single node strand into an Action Sequence element
	 * @param nodes Strand	
	 * @param flows List of all flows
	 * @return Converted Node strand
	 */
	private static DFDActionSequence convertNodeStrandToDFDActionSequence(List<Node> nodes, List<Flow> flows) {
		List<AbstractActionSequenceElement<?>> actionSequence = new ArrayList<AbstractActionSequenceElement<?>>();
		
		Node previousNode = null;
		for (Node node : nodes) {
			actionSequence.add(convertNodeToDFDActionSequenceElement(node, previousNode, flows));
			previousNode = node;
		}
		
		return new DFDActionSequence(actionSequence);
	}

	/**
	 * Convert single node into DFDActionSequenceElement
	 * @param node Node
	 * @param previousNode Node previous in the flow
	 * @param flows All flows
	 * @return Converted node
	 */
	private static DFDActionSequenceElement convertNodeToDFDActionSequenceElement(Node node, Node previousNode, List<Flow> flows) {
		List<DataFlowVariable> dataFlowVariables = new ArrayList<DataFlowVariable>();
		List<CharacteristicValue> nodeCharacteristics = new ArrayList<CharacteristicValue>();
				
		Flow flow = null;
		for (Flow f : flows) {
			if (f.getSourceNode().equals(previousNode) && f.getDestinationNode().equals(node)) {
				flow = f;
				break;
			}
		}

		return new DFDActionSequenceElement(dataFlowVariables, nodeCharacteristics, node.getEntityName(), node,
				previousNode, flow);

	}

}