package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.dfd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd.DFDActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd.DFDActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.ActionSequence;

import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.AbstractAssignment;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;

public class DFDActionSequenceFinder {
	public static List<ActionSequence> findAllSequencesInDFD(DataFlowDiagram dfd, DataDictionary dataDictionary) { 
		List<ActionSequence> sequences = new ArrayList<>();
		
		List<Flow> flows = dfd.getFlows();
		List<Node> nodes = dfd.getNodes();
		Set<Node> startNodes = getStartNodes(nodes);		
		Map<Node, List<Flow>> mapOfOutgoingEdges = getMapOfOutgoingEdges(flows);
		
		List<Map<Node, Pin>> nodeSequences = new ArrayList<>();
		
		for(Node node : startNodes) {
			nodeSequences.addAll(buildSequencesRec(node, null, mapOfOutgoingEdges));
		}
		
		for (Map<Node, Pin> nodeSequence : nodeSequences) {
			sequences.add(convertNodeStrandToDFDActionSequence(nodeSequence, flows));
		}
			
		
		return sequences;
	}
	
	
	
	
	/**
	 * Finds all Action Sequences in a dataflowdiagram instance
	 * @param dfd Data Flow Diagram model instance
	 * @param dataDictionary Data Dictionary model instance
	 * @return All Action Sequences
	 */
	
	/**
	 * Create Map with all outgoing edges for each node from list of all flows
	 * @param flows All flows in the DFD
	 * @return All outgoing edges
	 */
	private static Map<Node, List<Flow>> getMapOfOutgoingEdges(List<Flow> flows) {
		 Map<Node, List<Flow>> outgoingFlows = new HashMap<>();
		 for (Flow flow : flows) {
	            Node sourceNode = flow.getSourceNode();

	            if (outgoingFlows.containsKey(sourceNode)) {
	            	outgoingFlows.get(sourceNode).add(flow);
	            } else {
	                List<Flow> outFlows = new ArrayList<>();
	                outFlows.add(flow);
	                outgoingFlows.put(sourceNode, outFlows);
	            }
	        }
		 
		 return outgoingFlows;
	}
	
	private static List<Map<Node, Pin>> buildSequencesRec(Node start, Pin entry, Map<Node, List<Flow>> mapOfOutgoingEdges) {
		List<Map<Node, Pin>> sequences = new ArrayList<>();
		List<Pin> checkedOutPins = new ArrayList<>(); //necessary since there can be multiple assignments per output pin
		for (AbstractAssignment assignment : start.getBehaviour().getAssignment()) {
			if (checkedOutPins.contains(assignment.getOutputPin())) continue;
			if (assignment.getInputPins().size() > 1) throw new IllegalArgumentException("Assignments with multiple input flows are not supported");	
			if (entry == null && assignment.getInputPins().size() > 0) continue;
			if (assignment.getInputPins().size() == 0 || assignment.getInputPins().get(0).equals(entry)) {
				for (Flow flow : mapOfOutgoingEdges.get(start)) {
					if (flow.getSourcePin().equals(assignment.getOutputPin())) {
						for (Map<Node, Pin> nextSequence : buildSequencesRec(flow.getDestinationNode(), flow.getDestinationPin(), mapOfOutgoingEdges)) {
							LinkedHashMap<Node, Pin> newMap = (LinkedHashMap<Node, Pin>) ((LinkedHashMap<Node, Pin>) nextSequence).clone();
							nextSequence.clear();
							nextSequence.put(start, entry);
							nextSequence.putAll(newMap);
							sequences.add(nextSequence);
						}						
					}
				}
			}
			checkedOutPins.add(assignment.getOutputPin());
		}
		if (sequences.isEmpty()) {
			Map<Node, Pin> sequence = new LinkedHashMap<>();
			sequence.put(start, entry);
			sequences.add(sequence);
		}
		return sequences;
	}
	/**
	 * Get List of all Nodes without incoming flows, or External nodes 
	 * @param flows All flows
	 * @return List of External Nodes and Nodes with no incoming flows
	 */
	private static Set<Node> getStartNodes(List<Node> nodes) {
		Set<Node> startNodes = new HashSet<>();
		for (Node node : nodes) {
			for (AbstractAssignment assignment : node.getBehaviour().getAssignment()) {
				if (assignment.getInputPins().size() == 0) {
					startNodes.add(node);
				}
			}
		}
		return startNodes;
	}

	
	/**
	 * Convert single node strand into an Action Sequence element
	 * @param nodes Strand	
	 * @param flows List of all flows
	 * @return Converted Node strand
	 */
	
	private static DFDActionSequence convertNodeStrandToDFDActionSequence(Map<Node, Pin> nodesAndEntryPins, List<Flow> flows) {
		List<AbstractActionSequenceElement<?>> actionSequence = new ArrayList<AbstractActionSequenceElement<?>>();		
		Node prevNode = null;
		for (Node node : nodesAndEntryPins.keySet()) {
			actionSequence.add(convertNodeToDFDActionSequenceElement(node, prevNode, nodesAndEntryPins.get(node), flows));
			prevNode = node;
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
	
	private static DFDActionSequenceElement convertNodeToDFDActionSequenceElement(Node node, Node previousNode, Pin entry, List<Flow> flows) {
		List<DataFlowVariable> dataFlowVariables = new ArrayList<DataFlowVariable>();
		List<CharacteristicValue> nodeCharacteristics = new ArrayList<CharacteristicValue>();
		
		if (entry == null) {
			return new DFDActionSequenceElement(dataFlowVariables, nodeCharacteristics, node.getEntityName(), node,
					null, null);
		}
				
		Flow flow = null;
		for (Flow f : flows) {
			if (f.getDestinationPin().equals(entry) && f.getDestinationNode().equals(node)) {
				flow = f;
				break;
			}
		}

		return new DFDActionSequenceElement(dataFlowVariables, nodeCharacteristics, node.getEntityName(), node,
				previousNode, flow);

	}

}
