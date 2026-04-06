package org.dataflowanalysis.analysis.dsl;

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
}
