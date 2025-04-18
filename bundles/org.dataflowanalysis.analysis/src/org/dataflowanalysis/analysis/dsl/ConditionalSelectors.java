package org.dataflowanalysis.analysis.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.ConditionalSelector;
import org.dataflowanalysis.analysis.dsl.selectors.EmptySetOperationConditionalSelector;
import org.dataflowanalysis.analysis.dsl.selectors.VariableConditionalSelector;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

/**
 * Represents the {@link ConditionalSelector} matched by an {@link AnalysisConstraint}
 */
public class ConditionalSelectors extends AbstractParseable {
    private static final String DSL_KEYWORD = "where";
    private static final Logger logger = Logger.getLogger(ConditionalSelectors.class);

    private final List<ConditionalSelector> selectors;

    public ConditionalSelectors() {
        selectors = new ArrayList<>();
    }

    public ConditionalSelectors(List<ConditionalSelector> selectors) {
        this.selectors = selectors;
    }

    public void addSelector(ConditionalSelector selector) {
        this.selectors.add(selector);
    }

    public List<ConditionalSelector> getSelectors() {
        return new ArrayList<>(selectors);
    }

    @Override
    public String toString() {
        StringBuilder dslString = new StringBuilder();
        dslString.append(DSL_KEYWORD);
        dslString.append(" ");

        StringJoiner selectorString = new StringJoiner(" ");
        this.selectors.forEach(selector -> selectorString.add(selector.toString()));
        dslString.append(selectorString);

        return dslString.toString();
    }

    /**
     * Parses the {@link ConditionalSelectors} of an {@link AnalysisConstraint}.
     * @param string String view on the string that is parsed
     * @param context DSL context used during parsing
     * @return Returns a {@link ParseResult} that may contain the {@link ConditionalSelectors} of the
     * {@link AnalysisConstraint}
     */
    public static ParseResult<ConditionalSelectors> fromString(StringView string, DSLContext context) {
        if (string.invalid()) {
            return ParseResult.error("Unexpected end of input!");
        }
        if (!string.startsWith(DSL_KEYWORD)) {
            return string.expect(DSL_KEYWORD);
        }
        string.advance(DSL_KEYWORD.length() + 1);
        if (string.invalid()) {
            return ParseResult.error("Unexpected end of input!");
        }
        logger.info("Parsing: " + string.getString());
        List<ConditionalSelector> selectors = new ArrayList<>();
        while (!string.invalid()) {
            var selector = VariableConditionalSelector.fromString(string);
            if (selector.successful()) {
                selectors.add(selector.getResult());
                continue;
            }
            var emptySetSelector = EmptySetOperationConditionalSelector.fromString(string);
            if (emptySetSelector.successful()) {
                selectors.add(emptySetSelector.getResult());
                continue;
            }
            return ParseResult.error("Could not parse statement into conditional selector!");
        }
        if (selectors.isEmpty()) {
            string.retreat(DSL_KEYWORD.length() + 1);
            return ParseResult.error("Keyword " + DSL_KEYWORD + " is missing any selectors!");
        }
        ConditionalSelectors conditionalSelectors = new ConditionalSelectors(selectors);
        return ParseResult.ok(conditionalSelectors);
    }
}
