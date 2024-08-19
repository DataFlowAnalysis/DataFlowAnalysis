package org.dataflowanalysis.analysis.dfd.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.dfd.resource.DFDResourceProvider;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * The DFDTransposeFlowGraphFinder determines all transpose flow graphs contained in a model, allowing cycles
 */
public class DFDCyclicTransposeFlowGraphFinder extends DFDTransposeFlowGraphFinder {
    private final Logger logger = Logger.getLogger(DFDCyclicTransposeFlowGraphFinder.class);
    private static final int ITERATIONS_OF_LOOP = 1;
    private boolean hasCycles;

    /***
     * The DFDTransposeFlowGraphFinder determines all transpose flow graphs contained in a model, allowing cycles
     */
    public DFDCyclicTransposeFlowGraphFinder(DFDResourceProvider resourceProvider) {
        super(resourceProvider);
        hasCycles = false;
    }

    /***
     * The DFDTransposeFlowGraphFinder determines all transpose flow graphs contained in a model, allowing cycles
     */
    public DFDCyclicTransposeFlowGraphFinder(DataDictionary dataDictionary, DataFlowDiagram dataFlowDiagram) {
        super(dataDictionary, dataFlowDiagram);
        hasCycles = false;
    }

    /**
     * Finds all transpose flow graphs in a dataflowdiagram model instance
     * @return Returns a list of all transpose flow graphs
     */

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
            List<String> previousNodesInTransposeFlow = new ArrayList<>();

            previousNodesInTransposeFlow.add(endNode.getEntityName());

            List<DFDVertex> sinks = determineSinks(new DFDVertex(endNode, new HashMap<>(), new HashMap<>()), endNode.getBehaviour()
                    .getInPin(), sources, previousNodesInTransposeFlow);
            if (!sourceNodes.isEmpty()) {
                sinks = sinks.stream()
                        .filter(it -> new DFDTransposeFlowGraph(it).getVertices()
                                .stream()
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
     * @return whether the provided DFD has cyclic behavior or not, since resolving Cycles can lead to unexpected behavior
     */
    public boolean hasCycles() {
        return hasCycles;
    }

    /**
     * Builds a list of sink vertices with previous vertices for the creation of transpose flow graphs.
     * <p/>
     * This method preforms the determination of sinks recursively
     * @param sink Single sink vertex without previous vertices calculated
     * @param inputPins Relevant input pins on the given vertex
     * @return List of sinks created from the initial sink with previous vertices calculated
     */
    private List<DFDVertex> determineSinks(DFDVertex sink, List<Pin> inputPins, List<Node> sourceNodes, List<String> previousNodesInTransposeFlow) {
        List<DFDVertex> vertices = new ArrayList<>();
        vertices.add(sink);

        if (sourceNodes.contains(sink.getReferencedElement())) {
            return vertices;
        }
        for (Pin inputPin : inputPins) {
            List<Flow> incomingFlowsToPin = dataFlowDiagram.getFlows()
                    .stream()
                    .filter(flow -> flow.getDestinationPin()
                            .equals(inputPin))
                    .toList();
           

            List<DFDVertex> finalVertices = vertices;
            
            vertices = incomingFlowsToPin.stream()
                    .flatMap(flow -> handleIncomingFlowCyclic(flow, inputPin, finalVertices, sourceNodes, previousNodesInTransposeFlow).stream())
                    .toList();
        }
        return vertices;
    }
    
    private List<DFDVertex> loopAwareDetermineSinks(DFDVertex sink, List<Pin> inputPins, List<Node> sourceNodes, List<String> previousNodesInTransposeFlow) {
        List<DFDVertex> vertices = new ArrayList<>();
        vertices.add(sink);

        if (sourceNodes.contains(sink.getReferencedElement())) {
            return vertices;
        }
        for (Pin inputPin : inputPins) {
            List<Flow> incomingFlowsToPin = dataFlowDiagram.getFlows()
                    .stream()
                    .filter(flow -> flow.getDestinationPin()
                            .equals(inputPin))
                    .toList();
           List<DFDVertex> finalVertices = vertices;
           
           
           Set<DFDVertex> uniqueVertices = new HashSet<>();
           
           for (var flow : incomingFlowsToPin) {
               //Skipping flows that cause cycles
               if(previousNodesInTransposeFlow.contains(flow.getSourceNode().getEntityName())) continue;
               
               List<DFDVertex> result = handleIncomingFlowCyclic(flow, inputPin, finalVertices, sourceNodes, previousNodesInTransposeFlow);
               
               uniqueVertices.addAll(result);
           }
           //If we skip every flow due to cyclic behavior we need to create a new sink
           if (!uniqueVertices.isEmpty())
        	   vertices = new ArrayList<>(uniqueVertices);
        }
        return vertices;
    }

    /**
     * Handles flow to determine sink
     * @param incomingFlow relevant flow
     * @param inputPin Relevant input pin on the given vertex
     * @param finalVertices
     * @param sourceNodes
     * @param previousNodesInTransposeFlow List of all Nodes part of the current transpose Flow
     */
    private List<DFDVertex> handleIncomingFlowCyclic(Flow incomingFlow, Pin inputPin, List<DFDVertex> finalVertices, List<Node> sourceNodes,
            List<String> previousNodesInTransposeFlow) {

        var copyPreviousNodesInTransposeFlow = new ArrayList<>(previousNodesInTransposeFlow);

        List<DFDVertex> result = new ArrayList<>();

        Node previousNode = incomingFlow.getSourceNode();

        List<Pin> previousNodeInputPins = getAllPreviousNodeInputPins(previousNode, incomingFlow);

        List<DFDVertex> previousNodeVertices = new ArrayList<>();

        if (!loopCheck(copyPreviousNodesInTransposeFlow, previousNode.getEntityName())) {
            if (!hasCycles) {
                logger.warn("Resolving cycles: Stopping cyclic behavior for analysis, may cause unwanted behavior");
                hasCycles = true;
            }
            copyPreviousNodesInTransposeFlow.add(previousNode.getEntityName());
            previousNodeVertices = loopAwareDetermineSinks(new DFDVertex(previousNode, new HashMap<>(), new HashMap<>()), previousNodeInputPins, sourceNodes,
                    copyPreviousNodesInTransposeFlow);
            
        } else {
            copyPreviousNodesInTransposeFlow.add(previousNode.getEntityName());
            previousNodeVertices = determineSinks(new DFDVertex(previousNode, new HashMap<>(), new HashMap<>()), previousNodeInputPins, sourceNodes,
                    copyPreviousNodesInTransposeFlow);
        }
        
        for (DFDVertex vertex : finalVertices) {
            result.addAll(cloneFlowAndVertexForMultipleFlowGraphs(vertex, inputPin, incomingFlow, previousNodeVertices));
        }
        return result;
    }

    /**
     * checks if the source of incoming flow is part of a loop (We allow first iteration of loop)
     * @param previousNodesInTransposeFlow List of all Nodes part of the current transpose Flow
     * @param sourceNode
     */
    private boolean loopCheck(List<String> previousNodesInTransposeFlow, String sourceNode) {
        long count = previousNodesInTransposeFlow.stream()
                .filter(item -> item.equals(sourceNode))
                .count();
        return count < ITERATIONS_OF_LOOP;
    }

    /**
     * Clones a vertex with its predecessors to use in multiple other flow graphs
     * @param vertex Vertex that should be cloned
     * @param inputPin Input pin to the vertex from the previous vertices
     * @param flow Flow between the input pin and the copied vertex
     * @param previousNodeVertices List of previous vertices
     * @return Returns a list of cloned vertices required for usage in multiple flow graphs
     */
    private List<DFDVertex> cloneFlowAndVertexForMultipleFlowGraphs(DFDVertex vertex, Pin inputPin, Flow flow, List<DFDVertex> previousNodeVertices) {
        List<DFDVertex> newVertices = new ArrayList<>();

        // If we run loop more than once, we may also need to copy the flow, for the analysis
        Flow newFlow = ITERATIONS_OF_LOOP > 1 ? EcoreUtil.copy(flow) : flow;

        for (var previousVertex : previousNodeVertices) {
            DFDVertex newVertex = vertex.copy(new IdentityHashMap<>());
            newVertex.getPinDFDVertexMap()
                    .put(inputPin, previousVertex);
            newVertex.getPinFlowMap()
                    .put(inputPin, newFlow);
            newVertices.add(newVertex);
        }
        return newVertices;
    }
}
