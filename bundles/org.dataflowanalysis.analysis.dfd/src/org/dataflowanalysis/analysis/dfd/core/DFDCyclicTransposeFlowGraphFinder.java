package org.dataflowanalysis.analysis.dfd.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
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
public class DFDCyclicTransposeFlowGraphFinder extends DFDTransposeFlowGraphFinder {



    public DFDCyclicTransposeFlowGraphFinder(DFDResourceProvider resourceProvider) {
        super(resourceProvider);
    }

    public DFDCyclicTransposeFlowGraphFinder(DataDictionary dataDictionary, DataFlowDiagram dataFlowDiagram) {
        super( dataDictionary, dataFlowDiagram);
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
            
            List<String> previousNodesInTransposeFlow = new ArrayList<>();
            previousNodesInTransposeFlow.add(incomingFlowsToPin.get(0).getDestinationNode().getEntityName());
            previousNodesInTransposeFlow.add(incomingFlowsToPin.get(0).getSourceNode().getEntityName());
            
            List<DFDVertex> finalVertices = vertices;
            vertices = (incomingFlowsToPin.stream()
                            .flatMap(flow -> handleIncomingFlow(flow, inputPin, finalVertices, sourceNodes,previousNodesInTransposeFlow).stream())
                            .toList());
        }
        return vertices;
    }
    
    private List<DFDVertex> determineSinks(DFDVertex sink, List<Pin> inputPins, List<Node> sourceNodes, List<String> previousNodesInTransposeFlow) {
        List<DFDVertex> vertices = new ArrayList<>();
        vertices.add(sink);
        
        
        if (sourceNodes.contains(sink.getReferencedElement())) {
            return vertices;
        }

        for (Pin inputPin : inputPins) {
            List<Flow> incomingFlowsToPin = dataFlowDiagram.getFlows().stream()
                    .filter(flow -> flow.getDestinationPin().equals(inputPin))
                    .toList();
            
            if(!LoopCheck(previousNodesInTransposeFlow, incomingFlowsToPin.get(0).getSourceNode().getEntityName())) continue;
            
            previousNodesInTransposeFlow.add(incomingFlowsToPin.get(0).getSourceNode().getEntityName());
            
            List<DFDVertex> finalVertices = vertices;
            vertices = (incomingFlowsToPin.stream()
                            .flatMap(flow -> handleIncomingFlow(flow, inputPin, finalVertices, sourceNodes,previousNodesInTransposeFlow).stream())
                            .toList());
        }
        return vertices;
    }
    
    public List<DFDVertex> handleIncomingFlow(Flow incomingFlow, Pin inputPin, List<DFDVertex> vertices, List<Node> sourceNodes, List<String> previousNodesInTransposeFlow) {
        List<DFDVertex> result = new ArrayList<>();       
        
        Node previousNode = incomingFlow.getSourceNode();
        
        List<Pin> previousNodeInputPins = getAllPreviousNodeInputPins(previousNode, incomingFlow);
       
        //Das new DFDVertex & Flow Copy um Problem zu behandeln. --> von implements to extends core copy
        List<DFDVertex> previousNodeVertices = determineSinks(new DFDVertex(previousNode, new HashMap<>(), new HashMap<>()),
                previousNodeInputPins, sourceNodes, new ArrayList<>(previousNodesInTransposeFlow));
        
        for (DFDVertex vertex : vertices) {
            result.addAll(cloneFlowandVertexForMultipleFlowGraphs(vertex, inputPin, incomingFlow, previousNodeVertices));
        }
        return result;
    } 
    
    private Boolean LoopCheck(List<String> list, String element) {
        long count = list.stream()
                .filter(item -> item.equals(element))
                .count();
        return count <= 2;
    }

    /**
     * Clones a vertex with its predecessors to use in multiple other flow graphs
     * @param vertex Vertex that should be cloned
     * @param inputPin Input pin to the vertex from the previous vertices
     * @param flow Flow between the input pin and the copied vertex
     * @param previousNodeVertices List of previous vertices
     * @return Returns a list of cloned vertices required for usage in multiple flow graphs
     */
    private List<DFDVertex> cloneFlowandVertexForMultipleFlowGraphs(DFDVertex vertex, Pin inputPin, Flow flow, List<DFDVertex> previousNodeVertices) {
        List<DFDVertex> newVertices = new ArrayList<>();
        //Flow newFlow = flow.copy()
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
}

