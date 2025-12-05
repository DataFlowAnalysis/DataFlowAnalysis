package org.dataflowanalysis.analysis;

import java.util.List;
import java.util.function.Predicate;
import org.apache.log4j.*;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.eclipse.xtext.linking.impl.AbstractCleaningLinker;
import org.eclipse.xtext.linking.impl.DefaultLinkingService;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.resource.containers.ResourceSetBasedAllContainersStateProvider;

/**
 * This interface represents the functionality of a data flow confidentiality analysis. To use the analysis the
 * {@link DataFlowConfidentialityAnalysis#initializeAnalysis()} method must be called. After that the flow graph of the
 * model can be determined with {@link DataFlowConfidentialityAnalysis#findFlowGraphs()}. To determine characteristics
 * at each node the method {@link FlowGraphCollection#evaluate()} must be called. Finally, a constraint can be evaluated
 * with {@link DataFlowConfidentialityAnalysis#queryDataFlow(AbstractTransposeFlowGraph, Predicate)} on each transpose
 * flow graph contained in the previously returned flow graph.
 */
public abstract class DataFlowConfidentialityAnalysis {
    public static final String PLUGIN_PATH = "org.dataflowanalysis.analysis";
    private final Logger logger = LoggerManager.getLogger(DataFlowConfidentialityAnalysis.class);

    /**
     * Initializes the analysis by setting up the execution environment and loading the referenced models
     */
    public abstract void initializeAnalysis();

    /**
     * Determines the collection of flow graphs in the referenced models
     * @return Returns the collection of flow graphs containing all flows present in the referenced models
     */
    public abstract FlowGraphCollection findFlowGraphs();

    /**
     * Evaluates a given condition on a transpose flow graph and returns all elements that violate the given condition
     * @param transposeFlowGraph Transpose flow graph that is analyzed by the analysis
     * @param condition Condition that describes a violation at one vertex. If the condition returns true, the condition is
     * violated and the vertex is included in the output. Otherwise, the vertex is not included in the result of this
     * method.
     * @return Returns a list of all nodes that matched the given condition
     */
    public List<? extends AbstractVertex<?>> queryDataFlow(AbstractTransposeFlowGraph transposeFlowGraph,
            Predicate<? super AbstractVertex<?>> condition) {
        return transposeFlowGraph.getVertices()
                .stream()
                .filter(condition)
                .toList();
    }

    /**
     * Sets the logger level of the analysis components
     * @param level Desired logger level of the analysis components
     */
    public abstract void setLoggerLevel(Level level);

    /**
     * Sets up the unified logging environment for the data flow analysis
     */
    protected void setupLoggers() {
        LoggerManager.getLogger(AbstractInternalAntlrParser.class)
                .setLevel(Level.WARN);
        LoggerManager.getLogger(DefaultLinkingService.class)
                .setLevel(Level.WARN);
        LoggerManager.getLogger(ResourceSetBasedAllContainersStateProvider.class)
                .setLevel(Level.WARN);
        LoggerManager.getLogger(AbstractCleaningLinker.class)
                .setLevel(Level.WARN);

        logger.info("Successfully initialized standalone log4j for the data flow analysis.");
    }
}
