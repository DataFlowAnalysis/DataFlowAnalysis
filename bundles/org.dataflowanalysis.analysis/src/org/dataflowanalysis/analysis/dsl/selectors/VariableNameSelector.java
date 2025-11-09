package org.dataflowanalysis.analysis.dsl.selectors;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public class VariableNameSelector extends DataSelector {
    private static final String DSL_KEYWORD = "named";
    private static final Logger logger = LoggerManager.getLogger(VariableNameSelector.class);

    private final String variableName;

    /**
     * Constructs a new instance of a {@link VariableNameSelector} with the given variable name
     * @param variableName Variable name the {@link DataSelector} should match
     */
    public VariableNameSelector(DSLContext context, String variableName) {
        super(context);
        this.variableName = variableName;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        return vertex.getAllDataCharacteristics()
                .stream()
                .anyMatch(it -> it.variableName()
                        .equals(this.variableName));
    }

    /**
     * Returns the variable name the data flowing to the node must (or must not) have
     * @return Returns the variable name matched by the selector
     */
    public String getVariableName() {
        return variableName;
    }

    @Override
    public String toString() {
        return DSL_KEYWORD + " " + this.variableName;
    }

    /**
     * Parses a {@link VariableNameSelector} object from the given view on a string
     * <p/>
     * This method expects the following format: {@code named <Name>}
     * @param string String view on the string that is parsed
     * @return {@link ParseResult} containing the {@link VariableNameSelector} object
     */
    public static ParseResult<VariableNameSelector> fromString(StringView string, DSLContext context) {
        string.skipWhitespace();
        if (string.invalid() || string.empty()) {
            return ParseResult.error("Cannot parse variable name selector from empty or invalid string!");
        }
        logger.info("Parsing: " + string.getString());
        int position = string.getPosition();
        if (!string.startsWith(DSL_KEYWORD)) {
            return string.expect(DSL_KEYWORD);
        }
        string.advance(DSL_KEYWORD.length() + 1);
        if (string.invalid() || string.empty()) {
            string.setPosition(position);
            return ParseResult.error("Cannot parse variable name selector from empty or invalid string!");
        }
        String[] split = string.getString()
                .split(" ");
        if (split.length == 0 || split[0].isEmpty()) {
            string.setPosition(position);
            return ParseResult.error("Invalid variable name in variable name selector!");
        }
        string.advance(split[0].length());
        string.advance(1);
        return ParseResult.ok(new VariableNameSelector(context, split[0]));
    }
}
