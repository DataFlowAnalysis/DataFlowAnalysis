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

/**
 * The DFDPartialFlowGraphFinder determines all partial flow graphs contained in a model
 */
public class DFDPartialFlowGraphFinder {

    /**
     * Finds all partial flow graphs in a dataflowdiagram model instance
     * @param dfd Data Flow Diagram model instance
     * @param dataDictionary Data Dictionary model instance
     * @return Returns a list of all partial flow graphs
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
     * Builds a list of sink vertices with previous vertices for the creation of partial flow Graphs
     * @param sink Single sink vertex without previous vertices calculated
     * @param flows All flows in the data flow diagram
     * @param inputPins Relevant input pins on the given vertex vertex
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
     * Calculate all input pins required on the previous node that will be needed to satisfy the assignments
     * to reach the present node
     * @param previousNode Previous node
     * @param flow Flow from previous into present node
     * @return List of all required pins
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
     * Clones a vertex with its predecessors to use in multiple other flow graphs
     * @param vertex Vertex that should be cloned
     * @param inputPin Input pin to the vertex from the previous vertices
     * @param flow Flow between the input pin and the copied vertex
     * @param previousNodeVertices List of previous vertices
     * @return Returns a list of cloned vertices required for usage in multiple flow graphs
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
     * Gets a list of nodes that are sinks of the given list of nodes
     * @param nodes A list of all nodes of which the sinks should be determined
     * @return List of sink nodes reachable by the given list of nodes
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
