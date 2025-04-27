package org.dataflowanalysis.analysis.dfd.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.TransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.resource.DFDResourceProvider;
import org.dataflowanalysis.dfd.datadictionary.AbstractAssignment;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.ForwardingAssignment;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;

public class DFDTransposeFlowGraphFinder implements TransposeFlowGraphFinder{
	private final Logger logger = Logger.getLogger(TransposeFlowGraphFinder.class);
    protected final DataFlowDiagram dataFlowDiagram;
    private boolean hasCycles = false;
    private int cycleDepth = 1;
    
    private Map<Set<Pin>, List<DFDVertex>> mapInPinsToExistingVertices = new HashMap<>();

    public DFDTransposeFlowGraphFinder(DFDResourceProvider resourceProvider) {
        this.dataFlowDiagram = resourceProvider.getDataFlowDiagram();
    }

    public DFDTransposeFlowGraphFinder(DataDictionary dataDictionary, DataFlowDiagram dataFlowDiagram) {
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
            List<DFDVertex> sinks = createVerticesForGivenPins(endNode, new HashSet<Pin>(endNode.getBehavior().getInPin()), sources, new HashMap<>());
            if (!sourceNodes.isEmpty()) {
                sinks = sinks.stream()
                        .filter(it -> new DFDTransposeFlowGraph(it).getVertices()
                                .stream()
                                .filter(DFDVertex.class::isInstance)
                                .map(DFDVertex.class::cast)
                                .anyMatch(vertex -> sources.contains(vertex.getReferencedElement())))
                        .toList();
            }
            sinks.stream().distinct().forEach(sink -> transposeFlowGraphs.add(new DFDTransposeFlowGraph(sink)));
        }
        return transposeFlowGraphs;
    }
    
    /**
     * Builds a list of sink vertices with previous vertices for the creation of transpose flow graphs.
     * <p/>
     * This method preforms the determination of sinks recursively
     * @param sink Single sink vertex without previous vertices calculated
     * @param inputPins Relevant input pins on the given vertex
     * @param numberOfPreviousAppeareancesInTFG Tracks how often a given combination has already appeared in the TFG, necessary for cycle detection.
     * @return List of sinks created from the initial sink with previous vertices calculated
     */
    private List<DFDVertex> createVerticesForGivenPins(Node node, Set<Pin> pins, List<Node> sourceNodes, Map<FlowConfigurationRecord, Integer> numberOfPreviousAppeareancesInTFG) {
    	List<DFDVertex> vertices = new ArrayList<>();
    	
    	//If there are no required pins or the node is contained in source nodes it is a source and we dont need to calculate incoming flows
    	if (pins.isEmpty()) {
    		vertices.add(new DFDVertex(node, new HashMap<>(), new HashMap<>()));
    		return vertices;
    	}
    	
    	//If we already examined what happens with this configuration of input pins we dont need to do it again.
    	if (mapInPinsToExistingVertices.containsKey(pins)) {
    		mapInPinsToExistingVertices.get(pins).stream().forEach(vertex -> {
    			vertices.add(vertex);
    		});
    		return vertices;
    	}
    	
    	var incomingFlowCombinations = calculateIncomingFlowCombinations(pins);
    	
    	for (var combination : incomingFlowCombinations.stream().filter(combination -> numberOfPreviousAppeareancesInTFG.getOrDefault(combination, 0) < cycleDepth).toList()) {    	
    		vertices.addAll(createAllVerticesForCombination(node, combination, sourceNodes, numberOfPreviousAppeareancesInTFG));
    		if (vertices.isEmpty()) return vertices; //This is necessary to kill TFGs with a cycle that dont start in a cycle
    	}  	
    	
    	//This is necessary for creating TFGs that only start in a cycle. I dont like it and it makes the "if (vertices.isEmpty()) return vertices" necessary
    	if (vertices.isEmpty() && incomingFlowCombinations.size() == 1) {
    		vertices.add(new DFDVertex(node, new HashMap<>(), new HashMap<>()));
    	}
    	
    	mapInPinsToExistingVertices.put(pins, vertices);    	
    	return vertices;
    }
    
    /**
     * For a given combinations of incoming flows calculate all previous vertices and create the vertices for the node.
     * @param node Referenced element	
     * @param combination Given combination
     * @param sourceNodes Source Nodes to pass through to createVerticesForGivenPins
     * @param numberOfPreviousAppeareancesInTFG Tracks how often a given combination has already appeared in the TFG, necessary for cycle detection.
     * @return Created DFD Vertices
     */
    private List<DFDVertex> createAllVerticesForCombination(Node node, FlowConfigurationRecord combination, List<Node> sourceNodes, Map<FlowConfigurationRecord, Integer> numberOfPreviousAppeareancesInTFG) {
    	var mapPinToVertices = new HashMap<Pin, List<DFDVertex>>();
    	
    	//If we already went through the configuration once, we have a cycle
    	if (numberOfPreviousAppeareancesInTFG.containsKey(combination)) {
    		if (!hasCycles) {
	    		hasCycles = true;
	    		logger.warn("Resolving cycles: Stopping cyclic behavior for analysis, may cause unwanted behavior");
    		}
    		numberOfPreviousAppeareancesInTFG.put(combination, numberOfPreviousAppeareancesInTFG.get(combination) + 1);
    	} else {
    		numberOfPreviousAppeareancesInTFG.put(combination, 0);
    	}
    	
		//For the given combination of input pins and incoming flows calculate all previous vertices
		combination.incomingFlows.keySet().stream().forEach(key -> {
			var previousNode = combination.incomingFlows.get(key).getSourceNode();
			var previousOutPin = combination.incomingFlows.get(key).getSourcePin();
			var requiredinPins = calculateInPinsFromOutPin(previousNode, previousOutPin);
			
			mapPinToVertices.put(key, createVerticesForGivenPins(previousNode, requiredinPins, sourceNodes, numberOfPreviousAppeareancesInTFG));
		});
		
		//If for a pin more than one vertex is returned that means a converging flow or cycle further down the TFG and the TFG needs to be cloned
		List<HashMap<Pin, DFDVertex>> mapPinToVertexList = new ArrayList<>();
		mapPinToVertexList.add(new HashMap<>());
		mapPinToVertices.keySet().stream().forEach(key -> {
			if (mapPinToVertices.get(key).size() == 1) {
				mapPinToVertexList.forEach(map -> map.put(key, mapPinToVertices.get(key).get(0)));
			} else {
				List<HashMap<Pin, DFDVertex>> mapsToRemove = new ArrayList<>();
				List<HashMap<Pin, DFDVertex>> newMaps = new ArrayList<>();
				mapPinToVertexList.forEach(map -> {
					mapsToRemove.add(map);
					mapPinToVertices.get(key).stream().forEach(vertex -> {
						var newMap = new HashMap<>(map);
						newMap.put(key, vertex);
						newMaps.add(newMap);
					});
				});
				mapPinToVertexList.removeAll(mapsToRemove);
				mapPinToVertexList.addAll(newMaps);
			}
		});
		
		List<DFDVertex> vertices = new ArrayList<>();
		
		mapPinToVertexList.forEach(map -> {
			vertices.add(new DFDVertex(node, map, combination.incomingFlows));
		});
		
		return vertices;
    }
    
    /**
     * Calculates all TFG Combinations possible with the given number of input pins
     * Reminder: If 2 Flows go into the same pin we create 2 TFG
     * @param pins Pins we analyze
     * @return all possible flow combinations
     */
    private List<FlowConfigurationRecord> calculateIncomingFlowCombinations (Set<Pin> pins) {
    	Map<Pin, List<Flow>> incomingFlowsPerPin = new HashMap<>();
    	pins.stream().forEach(pin -> {
    		incomingFlowsPerPin.putIfAbsent(pin, new ArrayList<>());
    		incomingFlowsPerPin.get(pin).addAll(dataFlowDiagram.getFlows().stream().filter(flow -> flow.getDestinationPin().equals(pin)).toList());    		
    	});
    	
    	List<FlowConfigurationRecord> combinations = new ArrayList<>();
    	combinations.add(new FlowConfigurationRecord(new HashMap<>()));
    	
    	//If there is more than one flow going into a single pin we need to create multiple configurations 
    	incomingFlowsPerPin.forEach((key, incomingFlowsToPin) -> {
    		if (incomingFlowsToPin.size() == 1) {
    			combinations.forEach(combination -> combination.incomingFlows.put(key, incomingFlowsToPin.get(0)));
    		} else {
    			var combinationsToRemove = new ArrayList<FlowConfigurationRecord>();
    			var newCombinations = new ArrayList<FlowConfigurationRecord>();
    			combinations.forEach(combination -> {
    				combinationsToRemove.add(combination);
    				var base = new FlowConfigurationRecord(combination.incomingFlows);
    				incomingFlowsToPin.stream().forEach(flow -> {
    					var newCombination = base.clone();
    					newCombination.incomingFlows.put(key, flow);
    					newCombinations.add(newCombination);
    				});
    			});
    			combinations.removeAll(combinationsToRemove);
    			combinations.addAll(newCombinations);
    		}    		
    	});
    	
    	return combinations;
    }
    
    /**
     * Calculates all In Pins required for a out Pin to fire
     * @param node Node containing the outPin
     * @param outPin
     * @return all required Pins
     */
    private Set<Pin> calculateInPinsFromOutPin(Node node, Pin outPin) {
    	Set<Pin> requiredInPins = new HashSet<>();
    	requiredInPins.addAll(node.getBehavior().getAssignment().stream().filter(ForwardingAssignment.class::isInstance).filter(assignment -> assignment.getOutputPin().equals(outPin)).flatMap(it -> ((ForwardingAssignment)it).getInputPins().stream()).toList());
    	requiredInPins.addAll(node.getBehavior().getAssignment().stream().filter(Assignment.class::isInstance).filter(assignment -> assignment.getOutputPin().equals(outPin)).flatMap(it -> ((Assignment)it).getInputPins().stream()).toList());
    	return requiredInPins;
    }
    
    /**
     * Represents one instance of one possible way from one node      * 
     */
    private record FlowConfigurationRecord(Map<Pin, Flow> incomingFlows) {
    	public FlowConfigurationRecord clone() {
    		return new FlowConfigurationRecord(new HashMap<Pin, Flow>(incomingFlows));
    	}
    };
    
    /**
     * Gets a list of nodes that are sinks of the given list of nodes
     * @param nodes A list of all nodes of which the sinks should be determined
     * @return List of sink nodes reachable by the given list of nodes
     */
    protected List<Node> getEndNodes(List<Node> nodes) {
        List<Node> endNodes = new ArrayList<>(nodes);
        for (Node node : nodes) {
            if (node.getBehavior()
                    .getInPin()
                    .isEmpty())
                endNodes.remove(node);
            for (Pin inputPin : node.getBehavior()
                    .getInPin()) {
                for (AbstractAssignment abstractAssignment : node.getBehavior()
                        .getAssignment()) {
                	if ((abstractAssignment instanceof ForwardingAssignment forwardingAssignment && forwardingAssignment.getInputPins().contains(inputPin)) ||
                			(abstractAssignment instanceof Assignment assignment && assignment.getInputPins().contains(inputPin))) {
	                        endNodes.remove(node);
	                        break;
                	}  
                }
            }
        }
        if (endNodes.isEmpty() && !nodes.isEmpty()) {
        	throw new IllegalArgumentException("DFD terminates in a cycle, no sink can be identified.");
        }
        return endNodes;
    }
    
    /**
     * Allows for setting a custom cycle depth, default is 1
     * @param i Custom cycle depth, needs to be >= 0
     */
    public void setCycleDepth(int i) {
    	if (i < 0) logger.error("Cycle Depth has to be bigger or equal to 0");
    	else cycleDepth = i;
    }
    
    public boolean hasCycles() {
    	return hasCycles;
    }
}
