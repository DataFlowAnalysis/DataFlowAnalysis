package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.dfd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd.DFDFlowGraph;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd.DFDVertex;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.FlowGraph;

import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;

public class DFDFlowGraphFinder {

	/**
	 * Finds all Action Sequences in a dataflowdiagram instance
	 * @param dfd Data Flow Diagram model instance
	 * @param dataDictionary Data Dictionary model instance
	 * @return All Action Sequences
	 */	
	public static List<FlowGraph> findAllFlowGraphsInDFD(DataFlowDiagram dfd, DataDictionary dataDictionary) { 
		Map<Node, Map<Pin, List<Flow>>> mapOfIngoingEdges = getMapOfIngoingEdges(dfd.getFlows());
		Map<Node, List<DFDVertex>> mapNodeToElements = new HashMap<>();
		Map<Node, List<Node>> mapOfOutgoingEdges = getMapOfOutgoingEdges(dfd.getFlows());
		for (Node startNode : getStartNodes(dfd.getFlows())) {
			fillMapNodeToElements(startNode, mapNodeToElements, mapOfIngoingEdges, mapOfOutgoingEdges);
		}
		List<FlowGraph> sequences = new ArrayList<>();
		
		for (var endNode : getEndNodes(dfd.getFlows())) {
			for (var endElement : mapNodeToElements.get(endNode)) {
				sequences.add(DFDFlowGraph.createFromEndElement(endElement));
			}
		}
		return sequences;
	}
	
	
	/**
	 * Fills the MapNodeToElements by recursively calling for all future elements
	 * @param node
	 * @param mapNodeToElements
	 * @param mapOfIngoingEdges
	 * @param mapOfOutgoingEdges
	 */
	private static void fillMapNodeToElements(Node node, Map<Node, List<DFDVertex>> mapNodeToElements, Map<Node, Map<Pin, List<Flow>>> mapOfIngoingEdges, Map<Node, List<Node>> mapOfOutgoingEdges) {
		mapOfIngoingEdges.putIfAbsent(node, new HashMap<>());
		mapOfOutgoingEdges.putIfAbsent(node, new ArrayList<>());
		for (Pin pin : mapOfIngoingEdges.get(node).keySet()) {
			for (Flow inFlow : mapOfIngoingEdges.get(node).get(pin)) {
				if (!mapNodeToElements.containsKey(inFlow.getSourceNode())) return; //Interrupt if not all previous elements have been created. Will be called again by the previous element
			}
		}
		mapNodeToElements.put(node, convertNodeToASE(node, mapOfIngoingEdges.get(node), mapNodeToElements));
		for (Node nextNode : mapOfOutgoingEdges.get(node)) {
			fillMapNodeToElements(nextNode, mapNodeToElements, mapOfIngoingEdges, mapOfOutgoingEdges);
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
	 * Converts DFD Node into a List of DFD ASE
	 * @param node Node to be converted	
	 * @param mapOfIngoingEdges Map of ingoing Flows per Pin
	 * @param mapNodeToElements Map of nodes and all ASE that were created from said node
	 * @return all ASE created from the node
	 */
	private static List<DFDVertex> convertNodeToASE (Node node, Map<Pin, List<Flow>> mapOfIngoingEdges, Map<Node, List<DFDVertex>> mapNodeToElements) {		
		List<DFDVertex> elements = new ArrayList<>();	
		elements.add(new DFDVertex(node.getEntityName(), node, new HashMap<>(), new HashMap<>()));
		for (Pin key : mapOfIngoingEdges.keySet()) {				
			List<DFDVertex> newElements = new ArrayList<>();
			for (Flow inFlow : mapOfIngoingEdges.get(key)) {				
				for (DFDVertex element : elements) {
					for (DFDVertex prevElement : mapNodeToElements.get(inFlow.getSourceNode())) {
						DFDVertex newElement = element.clone();
						newElement.getMapPinToPreviousElement().put(key, prevElement);
						newElement.getMapPinToInputFlow().put(key, inFlow);
						newElements.add(newElement);
					}					
				}
				
			}
			elements = newElements;
		}
		
		return elements;
	}

}
