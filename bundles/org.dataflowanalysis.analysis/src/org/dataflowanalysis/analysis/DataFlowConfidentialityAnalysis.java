package org.dataflowanalysis.analysis;

import java.util.List;
import java.util.function.Predicate;
import org.apache.log4j.Level;
import org.dataflowanalysis.analysis.flowgraph.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.flowgraph.AbstractVertex;
import org.dataflowanalysis.analysis.flowgraph.FlowGraph;

/**
 * This interface represents the functionality of a data flow confidentiality analysis. To use the analysis the
 * {@link DataFlowConfidentialityAnalysis#initializeAnalysis()} method must be called. After that the flow graph of the
 * model can be determined with {@link DataFlowConfidentialityAnalysis#findFlowGraph()}. To determine characteristics at
 * each node the method {@link DataFlowConfidentialityAnalysis#evaluateFlowGraph(FlowGraph)} must be called. Finally, a
 * constraint can be evaluated with
 * {@link DataFlowConfidentialityAnalysis#queryDataFlow(AbstractPartialFlowGraph, Predicate)} on each partial flow graph
 * contained in the previously returned flow graph. TODO: Naming: Confidentiality required here? Can´t this also analyse
 * something else?
 */
public interface DataFlowConfidentialityAnalysis {
    public static final String PLUGIN_PATH = "org.dataflowanalysis.analysis";

    /**
     * Initializes the analysis by setting up the execution environment and loading the referenced models
     * @return Returns true, if initialization was successful. Otherwise, the method returns false
     */
    public boolean initializeAnalysis();

    /**
     * Determines the flow graph of the referenced models
     * @return Returns the flow graph containing all flows present in the referenced models
     */
    public FlowGraph findFlowGraph();

    /**
     * Evaluates the flow graph and executes the label propagation on all vertices
     * @param flowGraph Flow Graph that should be evaluated
     * @return Returns a new flow graph that contains the evaluated vertices
     */
    public FlowGraph evaluateFlowGraph(FlowGraph flowGraph);

    /**
     * Evaluates a given condition on an partial flow graph and returns all elements that violate the given condition
     * @param partialFlowGraph Partial flow graph that is analyzed by the analysis
     * @param condition Condition that describes a violation at one vertex. If the condition returns true, the condition is
     * violated and the vertex is included in the output. Otherwise, the vertex is not included in the result of this
     * method.
     * @return Returns a list of all nodes that matched the given condition
     */
    public List<? extends AbstractVertex<?>> queryDataFlow(AbstractPartialFlowGraph partialFlowGraph, Predicate<? super AbstractVertex<?>> condition);

    /**
     * Sets the logger level of the analysis components
     * @param level Desired logger level of the analysis components
     */
    public void setLoggerLevel(Level level);
}
