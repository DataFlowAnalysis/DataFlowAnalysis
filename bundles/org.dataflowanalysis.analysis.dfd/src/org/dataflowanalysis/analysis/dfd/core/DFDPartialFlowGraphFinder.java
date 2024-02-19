package org.dataflowanalysis.analysis.dfd.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.dataflowanalysis.analysis.core.AbstractPartialFlowGraph;
import org.dataflowanalysis.dfd.datadictionary.AbstractAssignment;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;

public class DFDPartialFlowGraphFinder {

    /**
     * Finds all Partial Flow Graphs in a dataflowdiagram instance
     * @param dfd Data Flow Diagram model instance
     * @param dataDictionary Data Dictionary model instance
     * @return All Partial Flow Graphs
     */
    public static List<AbstractPartialFlowGraph> findAllPartialFlowGraphsInDFD(DataFlowDiagram dfd, DataDictionary dataDictionary) {
        List<Node> endNodes = getEndNodes(dfd.getNodes());

        List<AbstractPartialFlowGraph> sequences = new ArrayList<>();

        for (var endNode : endNodes) {
            for (var sink : buildRec(new DFDVertex(endNode.getEntityName(), endNode, new HashMap<>(), new HashMap<>()), dfd.getFlows(),
                    endNode.getBehaviour().getInPin())) {
                sink.unify(new HashSet<>());
                sequences.add(new DFDPartialFlowGraph(sink));
            }
        }
        return sequences;
    }

    /**
     * build List of sink vertices with previous Vertices for creation of Partial Flow Graphs
     * @param sink single sink vertex without previous vertices calculated
     * @param flows All Flows in the DFD
     * @param inputPins Relevant input pins on vertex
     * @return List of sinks created from the initial sink with previous vertices calculated
     */
    private static List<DFDVertex> buildRec(DFDVertex sink, List<Flow> flows, List<Pin> inputPins) {
        List<DFDVertex> vertices = new ArrayList<>();
        vertices.add(sink);
        for (var inputPin : inputPins) {
            List<DFDVertex> newVertices = new ArrayList<>();
            for (var flow : flows) {
                if (flow.getDestinationPin().equals(inputPin)) {
                    for (var vertex : vertices) {
                        Node previousNode = flow.getSourceNode();
                        List<Pin> previousNodeInputPins = getAllPreviousNodeInputPins(previousNode, flow);
                        List<DFDVertex> previousNodeVertices = buildRec(
                                new DFDVertex(previousNode.getEntityName(), previousNode, new HashMap<>(), new HashMap<>()), flows,
                                previousNodeInputPins);
                        newVertices.addAll(cloneVertexForMultipleFlowGraphs(vertex, inputPin, flow, previousNodeVertices));
                    }
                }
            }
            vertices = newVertices;
        }
        return vertices;
    }
    
    /**
     * Calculated all input pins required on the Previous Node that will be needed to satisfy the assignments reaching the present node
     * @param previousNode previous Node
     * @param flow Flow from previous into present Node
     * @return List of all required Pins
     */
    private static List<Pin> getAllPreviousNodeInputPins(Node previousNode, Flow flow) {
    	List<Pin> previousNodeInputPins = new ArrayList<>();
        for (var assignment : previousNode.getBehaviour().getAssignment()) {
            if (assignment.getOutputPin().equals(flow.getSourcePin())) {
                previousNodeInputPins.addAll(assignment.getInputPins());
            }
        }
        return previousNodeInputPins;
    }
    
    /**
     * 
     * @param vertex
     * @param inputPin
     * @param flow
     * @param previousNodeVertices
     * @return
     */
    private static List<DFDVertex> cloneVertexForMultipleFlowGraphs(DFDVertex vertex, Pin inputPin, Flow flow, List<DFDVertex> previousNodeVertices) {
    	List<DFDVertex> newVertices = new ArrayList<>();
    	for (var previousVertex : previousNodeVertices) {
            DFDVertex newVertex = vertex.clone();
            newVertex.getMapPinToPreviousVertex().put(inputPin, previousVertex);
            newVertex.getMapPinToInputFlow().put(inputPin, flow);
            newVertices.add(newVertex);
        }
    	return newVertices;
    }

    /**
     * Get List of sink nodes
     * @param nodes All nodes
     * @return List of sink nodes
     */
    private static List<Node> getEndNodes(List<Node> nodes) {
        List<Node> endNodes = new ArrayList<>(nodes);
        for (Node node : nodes) {
            if (node.getBehaviour().getInPin().isEmpty())
                endNodes.remove(node);
            for (Pin inputPin : node.getBehaviour().getInPin()) {
                for (AbstractAssignment assignment : node.getBehaviour().getAssignment()) {
                    if (assignment.getInputPins().contains(inputPin)) {
                        endNodes.remove(node);
                        break;
                    }
                }
            }
        }
        return endNodes;
    }
}
