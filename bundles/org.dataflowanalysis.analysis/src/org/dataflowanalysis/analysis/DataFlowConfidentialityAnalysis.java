package org.dataflowanalysis.analysis;

import java.util.List;
import java.util.function.Predicate;
import org.apache.log4j.*;
import org.dataflowanalysis.analysis.core.AbstractTransposedFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.FlowGraph;
import org.eclipse.xtext.linking.impl.AbstractCleaningLinker;
import org.eclipse.xtext.linking.impl.DefaultLinkingService;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.resource.containers.ResourceSetBasedAllContainersStateProvider;

/**
 * This interface represents the functionality of a data flow confidentiality analysis. To use the analysis the
 * {@link DataFlowConfidentialityAnalysis#initializeAnalysis()} method must be called. After that the flow graph of the
 * model can be determined with {@link DataFlowConfidentialityAnalysis#findFlowGraph()}. To determine characteristics at
 * each node the method {@link FlowGraph#evaluate()} must be called. Finally, a
 * constraint can be evaluated with
 * {@link DataFlowConfidentialityAnalysis#queryDataFlow(AbstractTransposedFlowGraph, Predicate)} on each transposed flow graph
 * contained in the previously returned flow graph.
 */
public abstract class DataFlowConfidentialityAnalysis {
    public static final String PLUGIN_PATH = "org.dataflowanalysis.analysis";
    private final Logger logger = Logger.getLogger(DataFlowConfidentialityAnalysis.class);

    /**
     * Initializes the analysis by setting up the execution environment and loading the referenced models
     */
    public abstract void initializeAnalysis();

    /**
     * Determines the flow graph of the referenced models
     * @return Returns the flow graph containing all flows present in the referenced models
     */
    public abstract FlowGraph findFlowGraph();

    /**
     * Evaluates a given condition on a transposed flow graph and returns all elements that violate the given condition
     * @param transposedFlowGraph Transposed flow graph that is analyzed by the analysis
     * @param condition Condition that describes a violation at one vertex. If the condition returns true, the condition is
     * violated and the vertex is included in the output. Otherwise, the vertex is not included in the result of this
     * method.
     * @return Returns a list of all nodes that matched the given condition
     */
    public List<? extends AbstractVertex<?>> queryDataFlow(AbstractTransposedFlowGraph transposedFlowGraph, Predicate<? super AbstractVertex<?>> condition) {
        return transposedFlowGraph.getVertices().stream().filter(condition).toList();
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
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure(new ConsoleAppender(new EnhancedPatternLayout("%-6r [%p] %-35C{1} - %m%n")));

        Logger.getLogger(AbstractInternalAntlrParser.class)
                .setLevel(Level.WARN);
        Logger.getLogger(DefaultLinkingService.class)
                .setLevel(Level.WARN);
        Logger.getLogger(ResourceSetBasedAllContainersStateProvider.class)
                .setLevel(Level.WARN);
        Logger.getLogger(AbstractCleaningLinker.class)
                .setLevel(Level.WARN);

        logger.info("Successfully initialized standalone log4j for the data flow analysis.");
    }
}
