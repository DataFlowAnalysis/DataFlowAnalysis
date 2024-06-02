package org.dataflowanalysis.analysis.dfd.core;

import java.util.*;

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
public class DFDTransposeFlowGraphFinder implements TransposeFlowGraphFinder {
    private final DataDictionary dataDictionary;
    private final DataFlowDiagram dataFlowDiagram;


    public DFDTransposeFlowGraphFinder(DFDResourceProvider resourceProvider) {
        this.dataDictionary = resourceProvider.getDataDictionary();
        this.dataFlowDiagram = resourceProvider.getDataFlowDiagram();
    }

    public DFDTransposeFlowGraphFinder(DataDictionary dataDictionary, DataFlowDiagram dataFlowDiagram) {
        this.dataDictionary = dataDictionary;
        this.dataFlowDiagram = dataFlowDiagram;
    }

    /**
     * Finds all transpose flow graphs in a dataflowdiagram model instance
     * @return Returns a list of all transpose flow graphs
     */
    @Override
    public List<? extends AbstractTransposeFlowGraph> findTransposeFlowGraphs() {
        return this.findTransposeFlowGraphs(getEndNodes(dataFlowDiagram.getNodes()), List.of());
    }
    
    @Override
    public List<? extends AbstractTransposeFlowGraph> findTransposeFlowGraphs(List<?> sourceNodes) {
        return this.findTransposeFlowGraphs(getEndNodes(dataFlowDiagram.getNodes()), sourceNodes);
    }

    @Override
    public List<? extends AbstractTransposeFlowGraph> findTransposeFlowGraphs(List<?> sinkNodes, List<?> sourceNodes) {
        List<Node> potentialSinks = sinkNodes.stream()
                .filter(Node.class::isInstance)
                .map(Node.class::cast)
                .toList();
        List<Node> sources = sourceNodes.stream()
                .filter(Node.class::isInstance)
                .map(Node.class::cast)
                .toList();
        List<DFDTransposeFlowGraph> transposeFlowGraphs = new ArrayList<>();

        for (Node endNode : potentialSinks) {
            List<DFDVertex> sinks = determineSinks(new DFDVertex(endNode, new HashMap<>(), new HashMap<>()),
                    endNode.getBehaviour().getInPin(), sources);
            if (!sourceNodes.isEmpty()) {
                sinks = sinks.stream()
                        .filter(it -> new DFDTransposeFlowGraph(it).getVertices().stream()
                                .filter(DFDVertex.class::isInstance)
                                .map(DFDVertex.class::cast)
                                .anyMatch(vertex -> sources.contains(vertex.getReferencedElement())))
                        .toList();
            }
            sinks.forEach(sink -> sink.unify(new HashSet<>()));
            sinks.forEach(sink -> transposeFlowGraphs.add(new DFDTransposeFlowGraph(sink)));
        }
        return transposeFlowGraphs;
    }

    /**
     * Builds a list of sink vertices with previous vertices for the creation of transpose flow graphs.
     * <p/>
     * This method preforms the determination of sinks recursively
     * @param sink Single sink vertex without previous vertices calculated
     * @param inputPins Relevant input pins on the given vertex
     * @return List of sinks created from the initial sink with previous vertices calculated
     */
    private List<DFDVertex> determineSinks(DFDVertex sink, List<Pin> inputPins, List<Node> sourceNodes) {
        List<DFDVertex> vertices = new ArrayList<>();
        vertices.add(sink);

        if (sourceNodes.contains(sink.getReferencedElement())) {
            return vertices;
        }

        for (Pin inputPin : inputPins) {
            List<Flow> incomingFlowsToPin = dataFlowDiagram.getFlows().stream()
                    .filter(flow -> flow.getDestinationPin().equals(inputPin))
                    .toList();

            List<DFDVertex> finalVertices = vertices;
            vertices = incomingFlowsToPin.stream()
                            .flatMap(flow -> handleIncomingFlow(flow, inputPin, finalVertices, sourceNodes).stream())
                            .toList();
        }
        return vertices;
    }

    public List<DFDVertex> handleIncomingFlow(Flow incomingFlow, Pin inputPin, List<DFDVertex> vertices, List<Node> sourceNodes) {
        List<DFDVertex> result = new ArrayList<>();
        for (DFDVertex vertex : vertices) {
            Node previousNode = incomingFlow.getSourceNode();
            List<Pin> previousNodeInputPins = getAllPreviousNodeInputPins(previousNode, incomingFlow);
            List<DFDVertex> previousNodeVertices = determineSinks(new DFDVertex(previousNode, new HashMap<>(), new HashMap<>()),
                    previousNodeInputPins, sourceNodes);
            result.addAll(cloneVertexForMultipleFlowGraphs(vertex, inputPin, incomingFlow, previousNodeVertices));
        }
        return result;
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
            DFDVertex newVertex = vertex.copy(new IdentityHashMap<>());
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
