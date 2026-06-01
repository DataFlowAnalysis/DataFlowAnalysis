package org.dataflowanalysis.analysis.dsl;

import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.context.DSLContextProvider;
import org.dataflowanalysis.analysis.dsl.groups.ConditionalSelectors;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.AnySelector;
import org.dataflowanalysis.analysis.dsl.selectors.conditional.ConditionalSelector;
import org.dataflowanalysis.analysis.dsl.selectors.flow.FlowType;
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

    protected static final String OMMITED_TRANSPOSE_FLOW_GRAPH = "Transpose flow graph %s did not contain any violations. Omitting!";

    private final Logger logger = LoggerManager.getLogger(AnalysisConstraint.class);
    protected final String name;
    protected final FlowType flowType;

    protected AbstractSelector sourceSelector;
    protected AbstractSelector destinationSelector;
    protected final ConditionalSelectors conditionalSelectors;
    protected final DSLContext context;

    /**
     * Create a new analysis constraint with no constraints
     */
    public AnalysisConstraint(String name) {
        this.name = name;
        this.context = new DSLContext();
        this.flowType = FlowType.NEVER_FLOWS;

        this.sourceSelector = new AnySelector(this.context);
        this.destinationSelector = new AnySelector(this.context);
        this.conditionalSelectors = new ConditionalSelectors();
    }

    public AnalysisConstraint(String name, AbstractSelector sourceSelector, FlowType flowType,
            AbstractSelector destinationSelector, ConditionalSelectors conditionalSelectors, DSLContext context) {
        this.name = name;
        this.sourceSelector = sourceSelector;
        this.flowType = flowType;
        this.destinationSelector = destinationSelector;
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
    public static ParseResult<? extends AnalysisConstraint> fromString(StringView string,
            DSLContextProvider contextProvider) {
        string.skipWhitespace();
        if (string.startsWith(SIMPLE_DSL_TOKEN)) {
            return SimpleAnalysisConstraint.fromString(string, contextProvider);
        } else {
            return AdvancedAnalysisConstraint.fromString(string, contextProvider);
        }
    }

    public AbstractSelector getSourceSelector() {
        return sourceSelector;
    }

    public AbstractSelector getDestinationSelector() {
        return destinationSelector;
    }

    public void setSourceSelector(AbstractSelector sourceSelector) {
        this.sourceSelector = sourceSelector;
    }

    public void setDestinationSelector(AbstractSelector destinationSelector) {
        this.destinationSelector = destinationSelector;
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
     * Returns the conditional selectors of the analysis constraint
     * @return Returns the saved conditional selectors
     */
    public ConditionalSelectors getConditionalSelectors() {
        return conditionalSelectors;
    }
}
