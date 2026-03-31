package org.dataflowanalysis.analysis.dsl;

import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.context.DSLContextProvider;
import org.dataflowanalysis.analysis.dsl.groups.ConditionalSelectors;
import org.dataflowanalysis.analysis.dsl.groups.DataSourceSelectors;
import org.dataflowanalysis.analysis.dsl.groups.DestinationSelectors;
import org.dataflowanalysis.analysis.dsl.groups.SourceSelectors;
import org.dataflowanalysis.analysis.dsl.groups.VertexDestinationSelectors;
import org.dataflowanalysis.analysis.dsl.groups.VertexSourceSelectors;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.ConditionalSelector;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

/**
 * Represents an analysis constraint created by the DSL
 */
public abstract class AnalysisConstraint {
    protected static final String SIMPLE_DSL_TOKEN = "-";
    protected static final String ADVANCED_DSL_TOKEN = "*";
    protected static final String DSL_NAME_SEPARATOR = ":";

    protected static final String FAILED_MATCHING_MESSAGE = "Vertex %s failed to match selector %s";
    protected static final String SUCCEEDED_MATCHING_MESSAGE = "Vertex %s matched all selectors";
    protected static final String OMMITED_TRANSPOSE_FLOW_GRAPH = "Transpose flow graph %s did not contain any violations. Omitting!";

    private final Logger logger = LoggerManager.getLogger(AnalysisConstraint.class);
    protected final String name;
    protected final org.dataflowanalysis.analysis.dsl.groups.SourceSelectors sourceSelectors;
    protected final FlowType flowType;
    protected final DestinationSelectors destinationSelectors;
    protected final org.dataflowanalysis.analysis.dsl.groups.ConditionalSelectors conditionalSelectors;
    protected final DSLContext context;

    /**
     * Create a new analysis constraint with no constraints
     */
    public AnalysisConstraint(String name) {
        this.name = name;
        this.sourceSelectors = new org.dataflowanalysis.analysis.dsl.groups.SourceSelectors();
        this.flowType = FlowType.NEVER_FLOWS;
        this.destinationSelectors = new DestinationSelectors();
        this.conditionalSelectors = new org.dataflowanalysis.analysis.dsl.groups.ConditionalSelectors();
        this.context = new DSLContext();
    }

    public AnalysisConstraint(String name, SourceSelectors sourceSelectors, FlowType flowType, DestinationSelectors destinationSelectors,
            org.dataflowanalysis.analysis.dsl.groups.ConditionalSelectors conditionalSelectors, DSLContext context) {
        this.name = name;
        this.sourceSelectors = sourceSelectors;
        this.flowType = flowType;
        this.destinationSelectors = destinationSelectors;
        this.conditionalSelectors = conditionalSelectors;
        this.context = context;
    }

    /**
     * Find violations of the constraint in the given flow graph collection
     * @param flowGraphCollection Given flow graph collection in which the constraint is evaluated
     * @return Returns a list of dsl results for each <b>violating</b> transpose flow graph
     */
    public abstract List<DSLResult> findViolations(FlowGraphCollection flowGraphCollection);

    /**
     * Adds a data source selector to the constraint
     * @param selector Data source selector that is added to the constraint
     */
    public void addDataSourceSelector(AbstractSelector selector) {
        this.sourceSelectors.addDataSourceSelector(selector);
    }

    /**
     * Adds a node source selector to the constraint
     * @param selector Node source selector that is added to the constraint
     */
    public void addNodeSourceSelector(AbstractSelector selector) {
        this.sourceSelectors.addVertexSourceSelector(selector);
    }

    /**
     * Adds a flow destination selector to the constraint
     * @param selector Flow destination selector that is added to the constraint
     */
    public void addNodeDestinationSelector(AbstractSelector selector) {
        this.destinationSelectors.addVertexDestinationSelector(selector);
    }

    /**
     * Adds a conditional selector to the constraint
     * @param selector Conditional selector that is added to the constraint
     */
    public void addConditionalSelector(ConditionalSelector selector) {
        this.conditionalSelectors.addSelector(selector);
    }

    /**
     * Returns the context of constraint variables of the constraint
     * @return Constraint variable context of the constraint
     */
    public DSLContext getContext() {
        return context;
    }

    @Override
    public abstract String toString();

    /**
     * Parses an analysis constraint from a given string view without a context provider
     * @param string View on the parsed string
     * @return Returns a {@link ParseResult} that may contain the {@link AnalysisConstraint}
     */
    public static ParseResult<? extends AnalysisConstraint> fromString(StringView string) {
        return AnalysisConstraint.fromString(string, null);
    }

    /**
     * Parses an analysis constraint from a given string view with a context provider
     * @param string View on the parsed string
     * @param contextProvider Context provider used to parse analysis-specific contents
     * @return Returns a {@link ParseResult} that may contain the {@link AnalysisConstraint}
     */
    public static ParseResult<? extends AnalysisConstraint> fromString(StringView string, DSLContextProvider contextProvider) {
        string.skipWhitespace();
        if (string.startsWith(SIMPLE_DSL_TOKEN)) {
            return SimpleAnalysisConstraint.fromString(string, contextProvider);
        } else {
            return AdvancedAnalysisConstraint.fromString(string, contextProvider);
        }
    }

    /**
     * Returns the name of the analysis constraint
     * <p/>
     * If not specified, the analysis constraint will be called "default"
     * @return Returns the name of the analysis constraint
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the data source selectors of the analysis constraint
     * @return Returns the saved data source selectors
     */
    public DataSourceSelectors getDataSourceSelectors() {
        return this.sourceSelectors.getDataSourceSelectors();
    }

    /**
     * Returns the vertex source selectors of the analysis constraint
     * @return Returns the saved vertex source selectors
     */
    public VertexSourceSelectors getVertexSourceSelectors() {
        return this.sourceSelectors.getVertexSourceSelectors();
    }

    /**
     * Returns the vertex destination selectors of the analysis constraint
     * @return Returns the saved vertex destination selectors
     */
    public VertexDestinationSelectors getVertexDestinationSelectors() {
        return this.destinationSelectors.getVertexDestinationSelectors();
    }

    /**
     * Returns the conditional selectors of the analysis constraint
     * @return Returns the saved conditional selectors
     */
    public ConditionalSelectors getConditionalSelectors() {
        return conditionalSelectors;
    }
}
