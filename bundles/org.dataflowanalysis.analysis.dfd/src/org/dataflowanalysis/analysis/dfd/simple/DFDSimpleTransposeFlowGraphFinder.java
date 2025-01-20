package org.dataflowanalysis.analysis.dfd.simple;

import java.util.*;

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

/**
 * The DFDTransposeFlowGraphFinder determines all transpose flow graphs contained in a model
 */
public class DFDSimpleTransposeFlowGraphFinder implements TransposeFlowGraphFinder {
    protected final DataFlowDiagram dataFlowDiagram;
    
    private Map<Node, DFDSimpleVertex> mapNodeToExistingVertex = new HashMap<>();

    public DFDSimpleTransposeFlowGraphFinder(DFDResourceProvider resourceProvider) {
        this.dataFlowDiagram = resourceProvider.getDataFlowDiagram();
    }

    public DFDSimpleTransposeFlowGraphFinder(DataDictionary dataDictionary, DataFlowDiagram dataFlowDiagram) {
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
        List<DFDSimpleTransposeFlowGraph> transposeFlowGraphs = new ArrayList<>();
        
        System.out.println(dataFlowDiagram.getNodes());

        
        for (Node endNode : getEndNodes(dataFlowDiagram.getNodes())) {
            DFDSimpleVertex sink = determineSinks(endNode);
            transposeFlowGraphs.add(new DFDSimpleTransposeFlowGraph(sink));
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
    private DFDSimpleVertex determineSinks(Node node) {
    	if (mapNodeToExistingVertex.get(node) != null) return mapNodeToExistingVertex.get(node);
    	
    	if (!verifySimplicity(node)) throw new IllegalArgumentException("DFD not simple: outPin not requiring all InPins");
    	
    	Map<Pin, Flow> pinToFlowMap = new HashMap<>();
    	Set<DFDSimpleVertex> previousVertices = new HashSet<>();
    	
    	node.getBehavior().getInPin().forEach(pin -> {
    		var incomingFlows = dataFlowDiagram.getFlows().stream().filter(flow -> flow.getDestinationPin().equals(pin)).toList();
    		if (incomingFlows.size() != 1) throw new IllegalArgumentException("DFD not simple: Number of flows to inpin not 1");
    		var incomingFlow = incomingFlows.get(0);
    		pinToFlowMap.put(pin, incomingFlow);
    		previousVertices.add(determineSinks(incomingFlow.getSourceNode()));
    	});
    	
    	node.getBehavior().getOutPin().forEach(pin -> {
    		var outgoingFlows = dataFlowDiagram.getFlows().stream().filter(flow -> flow.getSourcePin().equals(pin)).toList();
    		if (outgoingFlows.size() == 0) throw new IllegalArgumentException("DFD not simple: Dead output pin");
    		var flowname = outgoingFlows.get(0).getEntityName();
    		outgoingFlows.forEach(it -> {
    			if (!it.getEntityName().equals(flowname)) throw new IllegalArgumentException("DFD not simple: All outgoing flows from one pin must have the same name");
    		});
    		var outgoingFlow = outgoingFlows.get(0);
    		pinToFlowMap.put(pin, outgoingFlow);
    	});
    	
    	var vertex = new DFDSimpleVertex(node, previousVertices, pinToFlowMap);
    	mapNodeToExistingVertex.put(node, vertex);
    	return vertex;
    }
    
    //Assumption!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private boolean verifySimplicity(Node node) {		
    	return node.getBehavior().getAssignment().stream().filter(ForwardingAssignment.class::isInstance).anyMatch(it -> ((ForwardingAssignment)it).getInputPins().equals(node.getBehavior().getInPin()))
    			|| node.getBehavior().getAssignment().stream().filter(Assignment.class::isInstance).anyMatch(it -> ((Assignment)it).getInputPins().equals(node.getBehavior().getInPin()))
    			|| node.getBehavior().getInPin().size() == 0
    			|| node.getBehavior().getOutPin().size() == 0;    	
    }
    
    

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
        return endNodes;
    }
}
