package org.dataflowanalysis.analysis.dsl;

import java.util.List;
import java.util.StringJoiner;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.context.DSLContextProvider;
import org.dataflowanalysis.analysis.dsl.groups.ConditionalSelectors;
import org.dataflowanalysis.analysis.dsl.groups.DestinationSelectors;
import org.dataflowanalysis.analysis.dsl.groups.SourceSelectors;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public class AdvancedAnalysisConstraint extends AnalysisConstraint {
    public AdvancedAnalysisConstraint(String name) {
        super(name);
    }

    public AdvancedAnalysisConstraint(String name,
            org.dataflowanalysis.analysis.dsl.groups.SourceSelectors sourceSelectors, FlowType flowType,
            DestinationSelectors destinationSelectors,
            org.dataflowanalysis.analysis.dsl.groups.ConditionalSelectors conditionalSelectors, DSLContext context) {
        super(name, sourceSelectors, flowType, destinationSelectors, conditionalSelectors, context);
    }

    @Override
    public List<DSLResult> findViolations(FlowGraphCollection flowGraphCollection) {
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    public String toString() {
        StringJoiner dslString = new StringJoiner(" ");
        dslString.add(ADVANCED_DSL_TOKEN);
        dslString.add(this.name + DSL_NAME_SEPARATOR);
        dslString.add(sourceSelectors.toString());
        dslString.add(flowType.toString());
        dslString.add(destinationSelectors.toString());
        if (!this.conditionalSelectors.getSelectors()
                .isEmpty()) {
            dslString.add(this.conditionalSelectors.toString());
        }
        return dslString.toString();
    }

    public static ParseResult<? extends AnalysisConstraint> fromString(StringView string,
            DSLContextProvider contextProvider) {
        DSLContext context = new DSLContext(contextProvider);
        string.skipWhitespace();
        if (!string.startsWith(ADVANCED_DSL_TOKEN)) {
            return string.expect(ADVANCED_DSL_TOKEN);
        }
        string.advance(ADVANCED_DSL_TOKEN.length() + 1);
        string.skipWhitespace();
        int index = string.getString()
                .indexOf(DSL_NAME_SEPARATOR);
        if (index == -1) {
            return ParseResult.error("Invalid DSL Constraint: Did delimit constraint name with " + DSL_NAME_SEPARATOR);
        }
        String name = string.getString()
                .substring(0, index);
        string.advance(name.length());
        if (!string.startsWith(DSL_NAME_SEPARATOR)) {
            return string.expect(DSL_NAME_SEPARATOR);
        }
        string.advance(DSL_NAME_SEPARATOR.length() + 1);
        string.skipWhitespace();
        var sourceSelectors = SourceSelectors.fromString(string, context);
        if (sourceSelectors.failed()) {
            return ParseResult.error(sourceSelectors.getError());
        }
        string.skipWhitespace();
        var flowType = FlowType.fromString(string);
        if (flowType.failed()) {
            return ParseResult.error(flowType.getError());
        }

        string.skipWhitespace();
        if (string.empty()) {
            return ParseResult.ok(new AdvancedAnalysisConstraint(name, sourceSelectors.getResult(),
                    flowType.getResult(), new DestinationSelectors(),
                    new org.dataflowanalysis.analysis.dsl.groups.ConditionalSelectors(), context));
        }

        ParseResult<DestinationSelectors> destinationSelectorsParseResult = DestinationSelectors.fromString(string,
                context);
        if (destinationSelectorsParseResult.failed()) {
            return ParseResult.error(destinationSelectorsParseResult.getError());
        }
        DestinationSelectors destinationSelectors = destinationSelectorsParseResult.getResult();

        string.skipWhitespace();
        ParseResult<org.dataflowanalysis.analysis.dsl.groups.ConditionalSelectors> conditionalSelectorsParseResult = org.dataflowanalysis.analysis.dsl.groups.ConditionalSelectors
                .fromString(string, context);
        org.dataflowanalysis.analysis.dsl.groups.ConditionalSelectors conditionalSelectors = conditionalSelectorsParseResult
                .or(new ConditionalSelectors());

        string.skipWhitespace();
        if (!string.empty()) {
            return ParseResult.error("Unexpected symbols: " + string.getString());
        }
        return ParseResult.ok(new AdvancedAnalysisConstraint(name, sourceSelectors.getResult(), flowType.getResult(),
                destinationSelectors, conditionalSelectors, context));
    }
}
