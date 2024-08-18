package org.dataflowanalysis.analysis.dfd.core;

import java.util.*;

import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.TransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.resource.DFDResourceProvider;
import org.dataflowanalysis.dfd.datadictionary.AbstractAssignment;
import org.dataflowanalysis.dfd.datadictionary.Behaviour;
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
    protected final DataFlowDiagram dataFlowDiagram;
    
    private Map<Pin, DFDVertex> mapOutPinToExistingVertex = new HashMap<>();

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
            List<DFDVertex> sinks = determineSinks(new DFDVertex(endNode, new HashMap<>(), new HashMap<>()), endNode.getBehaviour()
                    .getInPin(), sources);
            if (!sourceNodes.isEmpty()) {
                sinks = sinks.stream()
                        .filter(it -> new DFDTransposeFlowGraph(it).getVertices()
                                .stream()
                                .filter(DFDVertex.class::isInstance)
                                .map(DFDVertex.class::cast)
                                .anyMatch(vertex -> sources.contains(vertex.getReferencedElement())))
                        .toList();
            }
            sinks.parallelStream().forEach(sink -> sink.unify(new HashSet<>()));
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
    private List<DFDVertex> determineSinks(DFDVertex sink, List<Pin> pins, List<Node> sourceNodes) {
        List<DFDVertex> vertices = new ArrayList<>();
        vertices.add(sink);

        if (sourceNodes.contains(sink.getReferencedElement())) {
            return vertices;
        }
        
        var inputPins = new ArrayList<>(pins);
        
        Map<Pin, List<Pin>> inToPreviousNodeInPinsMap = new HashMap<>();
        for (var pin : inputPins) {
        	Set<Pin> outputPins = new HashSet<>();
        	inToPreviousNodeInPinsMap.put(pin, new ArrayList<>());
        	dataFlowDiagram.getFlows()
	            .stream()
	            .filter(flow -> flow.getDestinationPin().equals(pin))
	            .forEach(flow -> outputPins.add(flow.getSourcePin()));
        	
        	outputPins.stream().forEach(outPin -> {
        		Behaviour behaviour = (Behaviour) outPin.eContainer();
        		behaviour.getAssignment().stream().filter(it -> it.getOutputPin().equals(outPin)).forEach(it -> inToPreviousNodeInPinsMap.get(pin).addAll(it.getInputPins()));
        	});
        }
                
        Map<Pin, List<Pin>> mapInPinToEqualInPin = new HashMap<>();
        var keyList = inToPreviousNodeInPinsMap.keySet().stream().toList();
        for (int i = 0; i < keyList.size(); i++) {
        	var key = keyList.get(i);
        	var inPins = inToPreviousNodeInPinsMap.get(key);
        	for (int j = i + 1; j < keyList.size(); j++) {
        		var key2 = keyList.get(j);
        		var inPin2 = inToPreviousNodeInPinsMap.get(key2);
        		if (inPins.containsAll(inPin2) && inPin2.containsAll(inPins)) {
        			if (mapInPinToEqualInPin.getOrDefault(key, null) == null) mapInPinToEqualInPin.put(key, new ArrayList<>());
        			mapInPinToEqualInPin.get(key).add(key2);
        		};
        	}
        }
        
        mapInPinToEqualInPin.keySet().stream().map(mapInPinToEqualInPin::get).forEach(inputPins::removeAll);

        for (Pin inputPin : inputPins) {
            List<Flow> incomingFlowsToPin = dataFlowDiagram.getFlows()
                    .stream()
                    .filter(flow -> flow.getDestinationPin()
                            .equals(inputPin))
                    .toList();

            List<DFDVertex> finalVertices = vertices;
            vertices = incomingFlowsToPin.stream()
                    .flatMap(flow -> handleIncomingFlow(flow, inputPin, finalVertices, sourceNodes, mapInPinToEqualInPin.getOrDefault(inputPin, new ArrayList<>())).stream())
                    .toList();
        }
        
        return vertices;
    }

    public List<DFDVertex> handleIncomingFlow(Flow incomingFlow, Pin inputPin, List<DFDVertex> vertices, List<Node> sourceNodes, List<Pin> equalPins) {
        List<DFDVertex> result = new ArrayList<>();
        
        var outPin = incomingFlow.getSourcePin();
        if (mapOutPinToExistingVertex.get(outPin) != null) {
        	for (DFDVertex vertex : vertices) {
        		 List<DFDVertex> previousNodeVertices = new ArrayList<>();
        		 var newVertex = mapOutPinToExistingVertex.get(outPin).copy(new IdentityHashMap<>());
        		 previousNodeVertices.add(newVertex);
        		 result.addAll(cloneVertexForMultipleFlowGraphs(vertex, inputPin, incomingFlow, previousNodeVertices, equalPins));
            }
        	return result;
        }

        Node previousNode = incomingFlow.getSourceNode();
        List<Pin> previousNodeInputPins = getAllPreviousNodeInputPins(previousNode, incomingFlow);
        List<DFDVertex> previousNodeVertices = determineSinks(new DFDVertex(previousNode, new HashMap<>(), new HashMap<>()), previousNodeInputPins,
                sourceNodes);
        if (!previousNodeVertices.isEmpty()) {
        	mapOutPinToExistingVertex.put(outPin, previousNodeVertices.get(0));
        }

        if (vertices.size() == 1 && previousNodeVertices.size() == 1 && vertices.get(0).getPinDFDVertexMap().isEmpty()) {
        	 	var vertex = vertices.get(0);
        	 	vertex.getPinDFDVertexMap()
                         .put(inputPin, previousNodeVertices.get(0));
                 equalPins.forEach(it -> vertex.getPinDFDVertexMap().put(it, previousNodeVertices.get(0)));
                 vertex.getPinFlowMap()
                         .put(inputPin, incomingFlow);
                 equalPins.forEach(it -> {
                 	var newFlow = dataFlowDiagram.getFlows().stream().filter(inFlow -> (inFlow.getDestinationPin().equals(it) && inFlow.getSourceNode().equals(incomingFlow.getSourceNode()))).findAny().orElseThrow();
                 	vertex.getPinFlowMap().put(it, newFlow);
                 });
                 result.add(vertex);
                 return result;
             
        }
        for (DFDVertex vertex : vertices) {
            result.addAll(cloneVertexForMultipleFlowGraphs(vertex, inputPin, incomingFlow, previousNodeVertices, equalPins));
        }
        
        
        return result;
    }

    /**
     * present node
     * @param previousNode Previous node
     * @param flow Flow from previous into present node
     * @return List of all required pins
     */
    protected List<Pin> getAllPreviousNodeInputPins(Node previousNode, Flow flow) {
        Set<Pin> previousNodeInputPins = new HashSet<>();
        for (var assignment : previousNode.getBehaviour()
                .getAssignment()) {
            if (assignment.getOutputPin()
                    .equals(flow.getSourcePin())) {
                previousNodeInputPins.addAll(assignment.getInputPins());
            }
        }
        
        return new ArrayList<Pin>(previousNodeInputPins);
    }

    /**
     * Clones a vertex with its predecessors to use in multiple other flow graphs
     * @param vertex Vertex that should be cloned
     * @param inputPin Input pin to the vertex from the previous vertices
     * @param flow Flow between the input pin and the copied vertex
     * @param previousNodeVertices List of previous vertices
     * @return Returns a list of cloned vertices required for usage in multiple flow graphs
     */
    protected List<DFDVertex> cloneVertexForMultipleFlowGraphs(DFDVertex vertex, Pin inputPin, Flow flow, List<DFDVertex> previousNodeVertices, List<Pin> equalPins) {
        List<DFDVertex> newVertices = new ArrayList<>();
        for (var previousVertex : previousNodeVertices) {
            DFDVertex newVertex = vertex.copy(new IdentityHashMap<>());
            newVertex.getPinDFDVertexMap()
                    .put(inputPin, previousVertex);
            equalPins.forEach(it -> newVertex.getPinDFDVertexMap().put(it, previousVertex));
            newVertex.getPinFlowMap()
                    .put(inputPin, flow);
            equalPins.forEach(it -> {
            	var newFlow = dataFlowDiagram.getFlows().stream().filter(inFlow -> (inFlow.getDestinationPin().equals(it) && inFlow.getSourceNode().equals(flow.getSourceNode()))).findAny().orElseThrow();
            	newVertex.getPinFlowMap().put(it, newFlow);
            });
            newVertices.add(newVertex);
            newVertex.unify(new HashSet<>());
        }
        return newVertices;
    }
    
    

    /**
     * Gets a list of nodes that are sinks of the given list of nodes
     * @param nodes A list of all nodes of which the sinks should be determined
     * @return List of sink nodes reachable by the given list of nodes
     */
    protected List<Node> getEndNodes(List<Node> nodes) {
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
