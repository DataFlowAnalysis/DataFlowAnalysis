package org.dataflowanalysis.analysis.dsl.selectors;

import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public class EmptySetOperationConditionalSelector implements ConditionalSelector {
    private static final String DSL_KEYWORD = "empty";
    private static final Logger logger = Logger.getLogger(EmptySetOperationConditionalSelector.class);

    private final SetOperation setOperation;

    public EmptySetOperationConditionalSelector(SetOperation setOperation) {
        this.setOperation = setOperation;
    }

    @Override
    public boolean matchesSelector(AbstractVertex<?> vertex, DSLContext context) {
        List<String> variableNames = vertex.getAllIncomingDataCharacteristics()
                .stream()
                .map(DataCharacteristic::variableName)
                .toList();
        boolean result = true;
        for (String variableName : variableNames) {
            if (result) {
                result = !setOperation.match(vertex, variableName, context)
                        .isEmpty();
            }
        }
        return !result;
    }

    public SetOperation getSetOperation() {
        return setOperation;
    }

    @Override
    public String toString() {
        return DSL_KEYWORD + " " + setOperation.toString();
    }

    /**
     * Parses a {@link EmptySetOperationConditionalSelector} object from the given view on a string
     * <p/>
     * This method expects the following format: {@code empty <SetOperation>}
     * @param string String view on the string that is parsed
     * @return {@link ParseResult} containing the {@link EmptySetOperationConditionalSelector} object
     */
    public static ParseResult<EmptySetOperationConditionalSelector> fromString(StringView string) {
        logger.info("Parsing: " + string.getString());
        if (!string.startsWith(DSL_KEYWORD)) {
            return string.expect(DSL_KEYWORD);
        }
        string.advance(DSL_KEYWORD.length() + 1);

        ParseResult<Intersection> intersection = Intersection.fromString(string);
        if (intersection.failed()) {
            string.retreat(DSL_KEYWORD.length() + 1);
            return ParseResult.error(intersection.getError());
        }
        string.advance(1);
        return ParseResult.ok(new EmptySetOperationConditionalSelector(intersection.getResult()));
    }
}
