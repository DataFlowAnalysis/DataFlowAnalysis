package org.dataflowanalysis.analysis.dfd.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.flowgraph.AbstractPartialFlowGraph;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;

public class DFDPartialFlowGraphFinder {

	/**
	 * Finds all Action Sequences in a dataflowdiagram instance
	 * @param dfd Data Flow Diagram model instance
	 * @param dataDictionary Data Dictionary model instance
	 * @return All Action Sequences
	 */	
	public static List<AbstractPartialFlowGraph> findAllPartialFlowGraphsInDFD(DataFlowDiagram dfd, DataDictionary dataDictionary) { 
		Map<Node, Map<Pin, List<Flow>>> mapOfIngoingEdges = getMapOfIngoingEdges(dfd.getFlows());
		Map<Node, List<DFDVertex>> mapNodeToVertices = new HashMap<>();
		Map<Node, List<Node>> mapOfOutgoingEdges = getMapOfOutgoingEdges(dfd.getFlows());
		for (Node startNode : getStartNodes(dfd.getFlows())) {
			fillMapNodeToVertices(startNode, mapNodeToVertices, mapOfIngoingEdges, mapOfOutgoingEdges);
		}
		List<AbstractPartialFlowGraph> sequences = new ArrayList<>();
		
		for (var endNode : getEndNodes(dfd.getFlows())) {
			for (var endElement : mapNodeToVertices.get(endNode)) {
				sequences.add(DFDPartialFlowGraph.createFromEndVertex(endElement));
			}
		}
		return sequences;
	}
	
	
	/**
	 * Fills the MapNodeToVertices by recursively calling for all future Vertices
	 * @param node
	 * @param mapNodeToVertices
	 * @param mapOfIngoingEdges
	 * @param mapOfOutgoingEdges
	 */
	private static void fillMapNodeToVertices(Node node, Map<Node, List<DFDVertex>> mapNodeToVertices, Map<Node, Map<Pin, List<Flow>>> mapOfIngoingEdges, Map<Node, List<Node>> mapOfOutgoingEdges) {
		mapOfIngoingEdges.putIfAbsent(node, new HashMap<>());
		mapOfOutgoingEdges.putIfAbsent(node, new ArrayList<>());
		for (Pin pin : mapOfIngoingEdges.get(node).keySet()) {
			for (Flow inFlow : mapOfIngoingEdges.get(node).get(pin)) {
				if (!mapNodeToVertices.containsKey(inFlow.getSourceNode())) return; //Interrupt if not all previous elements have been created. Will be called again by the previous element
			}
		}
		mapNodeToVertices.put(node, convertNodeToVertex(node, mapOfIngoingEdges.get(node), mapNodeToVertices));
		for (Node nextNode : mapOfOutgoingEdges.get(node)) {
			fillMapNodeToVertices(nextNode, mapNodeToVertices, mapOfIngoingEdges, mapOfOutgoingEdges);
		}
	}
	
	/**
	 * Create Map with all outgoing edges for each node from list of all flows
	 * @param flows All flows in the DFD
	 * @return All outgoing edges
	 */
	private static Map<Node, List<Node>> getMapOfOutgoingEdges(List<Flow> flows) {
		 Map<Node, List<Node>> outgoingEdges = new HashMap<>();
		 for (Flow flow : flows) {
	            Node sourceNode = flow.getSourceNode();
	            Node destinationNode = flow.getDestinationNode();

	            if (outgoingEdges.containsKey(sourceNode)) {
	                outgoingEdges.get(sourceNode).add(destinationNode);
	            } else {
	                List<Node> destinations = new ArrayList<>();
	                destinations.add(destinationNode);
	                outgoingEdges.put(sourceNode, destinations);
	            }
	        }
		 
		 return outgoingEdges;
	}
	
	/**
	 * Get Map that saves all input flows for each pin for each node
	 * @param flows All flows
	 * @return map of all input flows for each pin for each node
	 */
	private static Map<Node, Map<Pin, List<Flow>>> getMapOfIngoingEdges(List<Flow> flows) {
		 Map<Node, Map<Pin, List<Flow>>> ingoingEdges = new HashMap<>();
		 for (Flow flow : flows) {
	            Node destinationNode = flow.getDestinationNode();
	            Pin destinationPin = flow.getDestinationPin();
	            ingoingEdges.putIfAbsent(destinationNode, new HashMap<>());
	            Map<Pin, List<Flow>> mapPinToSourceNodes = ingoingEdges.get(destinationNode);
	            mapPinToSourceNodes.putIfAbsent(destinationPin, new ArrayList<>());
	            mapPinToSourceNodes.get(destinationPin).add(flow);
	        }
		 
		 return ingoingEdges;
	}
	
	/**
	 * Get List of all Nodes without incoming flows
	 * @param flows All flows
	 * @return List of  Nodes with no incoming flows
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
			if(startNodes.contains(destinationNode)) {
				startNodes.remove(destinationNode);
			}
		}
		return startNodes;
	}
	
	/**
	 * Get List of all Nodes without outgoing flows
	 * @param flows All flows
	 * @return List of  Nodes with no outgoing flows
	 */
	private static List<Node> getEndNodes(List<Flow> flows) {
		List<Node> endNodes = new ArrayList<Node>();
		for (var flow: flows) {
			var destinationNode = flow.getDestinationNode();
			if(!endNodes.contains(destinationNode)) {
				endNodes.add(destinationNode);
			}
		}
		
		
		for (var flow:flows) {
			var sourceNode = flow.getSourceNode();
			if(endNodes.contains(sourceNode)) {
				endNodes.remove(sourceNode);
			}
		}
		return endNodes;
	}
	
	/**
	 * Converts DFD Node into a List of DFD Vertices
	 * @param node Node to be converted	
	 * @param mapOfIngoingEdges Map of ingoing Flows per Pin
	 * @param mapNodeToVertices Map of nodes and all Vertices that were created from said node
	 * @return all Vertices created from the node
	 */
	private static List<DFDVertex> convertNodeToVertex (Node node, Map<Pin, List<Flow>> mapOfIngoingEdges, Map<Node, List<DFDVertex>> mapNodeToElements) {		
		List<DFDVertex> vertices = new ArrayList<>();	
		vertices.add(new DFDVertex(node.getEntityName(), node, new HashMap<>(), new HashMap<>()));
		for (Pin key : mapOfIngoingEdges.keySet()) {				
			List<DFDVertex> newVertices = new ArrayList<>();
			for (Flow inFlow : mapOfIngoingEdges.get(key)) {				
				for (DFDVertex vertex : vertices) {
					for (DFDVertex prevVertex : mapNodeToElements.get(inFlow.getSourceNode())) {
						DFDVertex newVertex = vertex.clone();
						newVertex.getMapPinToPreviousVertex().put(key, prevVertex);
						newVertex.getMapPinToInputFlow().put(key, inFlow);
						newVertices.add(newVertex);
					}					
				}
				
			}
			vertices = newVertices;
		}
		
		return vertices;
	}

}