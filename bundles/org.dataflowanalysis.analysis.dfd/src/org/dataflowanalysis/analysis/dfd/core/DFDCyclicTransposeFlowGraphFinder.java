package org.dataflowanalysis.analysis.dfd.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.TransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.resource.DFDResourceProvider;
import org.dataflowanalysis.dfd.datadictionary.AbstractAssignment;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;

/**
 * The DFDTransposeFlowGraphFinder determines all transpose flow graphs contained in a model
 */
public class DFDCyclicTransposeFlowGraphFinder implements TransposeFlowGraphFinder {
    private final DataDictionary dataDictionary;
    private final DataFlowDiagram dataFlowDiagram;


    public DFDCyclicTransposeFlowGraphFinder(DFDResourceProvider resourceProvider) {
        this.dataDictionary = resourceProvider.getDataDictionary();
        this.dataFlowDiagram = resourceProvider.getDataFlowDiagram();
    }

    public DFDCyclicTransposeFlowGraphFinder(DataDictionary dataDictionary, DataFlowDiagram dataFlowDiagram) {
        this.dataDictionary = dataDictionary;
        this.dataFlowDiagram = dataFlowDiagram;
    }

    /**
     * Finds all transpose flow graphs in a dataflowdiagram model instance
     * @return Returns a list of all transpose flow graphs
     */
    @Override
    public List<AbstractTransposeFlowGraph> findTransposeFlowGraphs() {
        List<Node> endNodes = getEndNodes(dataFlowDiagram.getNodes());
        List<AbstractTransposeFlowGraph> sequences = new ArrayList<>();
        if(endNodes.isEmpty()) {
           //what do we do?
        }
        for (var endNode : endNodes) {
            for (var sink : determineSinks(new DFDVertex(endNode, new HashMap<>(), new HashMap<>()), dataFlowDiagram
                    .getFlows(),
                    endNode.getBehaviour()
                            .getInPin())) {
                sink.unify(new HashSet<>());
                sequences.add(new DFDTransposeFlowGraph(sink));
            }
        }
        return sequences;
    }

    /**
     * Builds a list of sink vertices with previous vertices for the creation of transpose flow graphs.
     * <p/>
     * This method preforms the determination of sinks recursively
     * @param sink Single sink vertex without previous vertices calculated
     * @param flows All flows in the data flow diagram
     * @param inputPins Relevant input pins on the given vertex vertex
     * @return List of sinks created from the initial sink with previous vertices calculated
     */
    private List<DFDVertex> determineSinks(DFDVertex sink, List<Flow> flows, List<Pin> inputPins) {
        List<DFDVertex> vertices = new ArrayList<>();
        vertices.add(sink);
        
        for (var inputPin : inputPins) {
            List<DFDVertex> newVertices = new ArrayList<>();
            var flow = getFlow( inputPin, flows);
            for (var vertex : vertices) {      
                Node previousNode = flow.getSourceNode();
                List<Node> previousNodesInTransposeFlow = new ArrayList<>();
                previousNodesInTransposeFlow.add(flow.getDestinationNode());
                previousNodesInTransposeFlow.add(previousNode);
                List<Pin> previousNodeInputPins = getAllPreviousNodeInputPins(previousNode, flow);
                List<DFDVertex> previousNodeVertices = determineSinks(new DFDVertex(previousNode, new HashMap<>(), new HashMap<>()), flows,
                        previousNodeInputPins,previousNodesInTransposeFlow);
                newVertices.addAll(cloneVertexForMultipleFlowGraphs(vertex, inputPin, flow, previousNodeVertices));
            }
            
            vertices = newVertices;
        }
        return vertices;
    }
    
    private List<DFDVertex> determineSinks(DFDVertex sink, List<Flow> flows, List<Pin> inputPins, List<Node> previousNodesInTransposeFlow) {
        List<DFDVertex> vertices = new ArrayList<>();
        vertices.add(sink);
        for (var inputPin : inputPins) {
            List<DFDVertex> newVertices = new ArrayList<>();
            var flow =  getFlow(inputPin, flows);
            for (var vertex : vertices) {      
                Node previousNode = flow.getSourceNode();
                if(!LoopCheck(previousNodesInTransposeFlow, previousNode)) {
                    return vertices;
                }
                
                var CopyPreviousNodesInTransposeFlow = new ArrayList<>(previousNodesInTransposeFlow);
                CopyPreviousNodesInTransposeFlow.add(previousNode);
                List<Pin> previousNodeInputPins = getAllPreviousNodeInputPins(previousNode, flow);
                List<DFDVertex> previousNodeVertices = determineSinks(new DFDVertex(previousNode, new HashMap<>(), new HashMap<>()), flows,
                        previousNodeInputPins, CopyPreviousNodesInTransposeFlow);
                newVertices.addAll(cloneVertexForMultipleFlowGraphs(vertex, inputPin, flow, previousNodeVertices));
            }
            vertices = newVertices;
        }
        return vertices;
    }
    
   private Flow getFlow(Pin inputPin, List<Flow> flows) {
       return flows.stream()
               .filter(flow -> flow.getDestinationPin().equals(inputPin)).findFirst()
               .orElse(null);
   }
    
    private Boolean LoopCheck(List<Node> list, Node element) {
        long count = list.stream()
                .filter(item -> item.equals(element))
                .count();
        return count <= 2;
    }
    /**
     * Calculate all input pins required on the previous node that will be needed to satisfy the assignments to reach the
     * present node
     * @param previousNode Previous node
     * @param flow Flow from previous into present node
     * @return List of all required pins
     */
    private List<Pin> getAllPreviousNodeInputPins(Node previousNode, Flow flow) {
        List<Pin> previousNodeInputPins = new ArrayList<>();
        for (var assignment : previousNode.getBehaviour()
                .getAssignment()) {
            if (assignment.getOutputPin()
                    .equals(flow.getSourcePin())) {
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
    private List<DFDVertex> cloneVertexForMultipleFlowGraphs(DFDVertex vertex, Pin inputPin, Flow flow, List<DFDVertex> previousNodeVertices) {
        List<DFDVertex> newVertices = new ArrayList<>();
        for (var previousVertex : previousNodeVertices) {
            DFDVertex newVertex = vertex.clone();
            newVertex.getPinDFDVertexMap()
                    .put(inputPin, previousVertex);
            newVertex.getPinFlowMap()
                    .put(inputPin, flow);
            newVertices.add(newVertex);
        }
        return newVertices;
    }

    /**
     * Gets a list of nodes that are sinks of the given list of nodes
     * @param nodes A list of all nodes of which the sinks should be determined
     * @return List of sink nodes reachable by the given list of nodes
     */
    private List<Node> getEndNodes(List<Node> nodes) {
        List<Node> endNodes = new ArrayList<>(nodes);
        for (Node node : nodes) {
            if (node.getBehaviour()
                    .getInPin()
                    .isEmpty())
                endNodes.remove(node);
            for (Pin inputPin : node.getBehaviour()
                    .getInPin()) {
                for (AbstractAssignment assignment : node.getBehaviour()
                        .getAssignment()) {
                    if (assignment.getInputPins()
                            .contains(inputPin)) {
                        endNodes.remove(node);
                        break;
                    }
                }
            }
        }
        return endNodes;
    }
}

