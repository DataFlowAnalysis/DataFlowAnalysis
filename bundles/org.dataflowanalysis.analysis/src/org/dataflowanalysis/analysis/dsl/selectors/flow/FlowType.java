package org.dataflowanalysis.analysis.dsl.selectors.flow;

import java.util.List;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.conditional.ConditionalSelector;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public enum FlowType {
    NEVER_FLOWS("neverFlows"),
    FLOWS("flows"),
    ALWAYS_FLOWS("alwaysFlows"),
    NOT_ALWAYS_FLOWS("notAlwaysFlows");

    private final String name;

    FlowType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static ParseResult<FlowType> fromString(StringView string) {
        if (string.startsWith("neverFlows")) {
            string.advance("neverFlows".length());
            return ParseResult.ok(FlowType.NEVER_FLOWS);
        }
        if (string.startsWith("flows")) {
            string.advance("flows".length());
            return ParseResult.ok(FlowType.FLOWS);
        }
        if (string.startsWith("alwaysFlows")) {
            string.advance("alwaysFlows".length());
            return ParseResult.ok(FlowType.ALWAYS_FLOWS);
        }
        if (string.startsWith("notAlwaysFlows")) {
            string.advance("notAlwaysFlows".length());
            return ParseResult.ok(FlowType.NOT_ALWAYS_FLOWS);
        }
        return ParseResult.error("Invalid flow type: %s!".formatted(string.getString()));
    }

    public AbstractFlowType instantiate(AbstractSelector sourceSelector, AbstractSelector destinationSelector,
            List<ConditionalSelector> conditionalSelectors, FlowGraphCollection flowGraphCollection) {
        switch (this) {
            case NEVER_FLOWS -> {
                return new NeverFlowType(sourceSelector, destinationSelector, conditionalSelectors,
                        flowGraphCollection);
            }
            case FLOWS -> {
                return new DoesFlowType(sourceSelector, destinationSelector, conditionalSelectors, flowGraphCollection);
            }
            case ALWAYS_FLOWS -> {
                return new AlwaysFlowType(sourceSelector, destinationSelector, conditionalSelectors,
                        flowGraphCollection);
            }
            case NOT_ALWAYS_FLOWS -> {
                return new NotAlwaysFlowType(sourceSelector, destinationSelector, conditionalSelectors,
                        flowGraphCollection);
            }
        }
        throw new IllegalArgumentException("No valid instantiation found for flow type!");
    }
}
