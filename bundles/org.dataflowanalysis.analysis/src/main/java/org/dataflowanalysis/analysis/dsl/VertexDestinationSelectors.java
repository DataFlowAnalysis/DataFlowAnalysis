package org.dataflowanalysis.analysis.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.VertexCharacteristicsListSelector;
import org.dataflowanalysis.analysis.dsl.selectors.VertexCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.selectors.VertexNameSelector;
import org.dataflowanalysis.analysis.dsl.selectors.VertexTypeSelector;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

/**
 * Represents the destination vertex {@link AbstractSelector} matched by an {@link AnalysisConstraint}
 */
public class VertexDestinationSelectors extends AbstractParseable {
    private static final String DSL_KEYWORD = "vertex";
    private static final Logger logger = LoggerManager.getLogger(VertexDestinationSelectors.class);

    private final List<AbstractSelector> selectors;

    public VertexDestinationSelectors() {
        selectors = new ArrayList<>();
    }

    public VertexDestinationSelectors(List<AbstractSelector> selectors) {
        this.selectors = selectors;
    }

    public void addSelector(AbstractSelector selector) {
        this.selectors.add(selector);
    }

    public List<AbstractSelector> getSelectors() {
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
     * Parses the {@link VertexDestinationSelectors} of an {@link AnalysisConstraint}.
     * @param string String view on the string that is parsed
     * @param context DSL context used during parsing
     * @return Returns a {@link ParseResult} that may contain the {@link VertexDestinationSelectors} of the
     * {@link AnalysisConstraint}
     */
    public static ParseResult<VertexDestinationSelectors> fromString(StringView string, DSLContext context) {
        string.skipWhitespace();
        if (string.invalid()) {
            return ParseResult.error("Unexpected end of input!");
        }
        int position = string.getPosition();
        if (!string.getString()
                .startsWith(DSL_KEYWORD)) {
            return ParseResult.error("String did not start with " + DSL_KEYWORD);
        }
        string.advance(DSL_KEYWORD.length() + 1);
        string.skipWhitespace();
        if (string.invalid()) {
            string.setPosition(position);
            return ParseResult.error("Unexpected end of input!");
        }
        logger.debug("Parsing: " + string.getString());
        List<AbstractSelector> selectors = new ArrayList<>();
        while (!string.invalid()) {
            string.skipWhitespace();
            var listSelector = VertexCharacteristicsListSelector.fromString(string, context);
            if (listSelector.successful()) {
                selectors.add(listSelector.getResult());
                continue;
            }

            var selector = VertexCharacteristicsSelector.fromString(string, context);
            if (selector.successful()) {
                selectors.add(selector.getResult());
                continue;
            }
            var nameSelector = VertexNameSelector.fromString(string, context);
            if (nameSelector.successful()) {
                selectors.add(nameSelector.getResult());
                continue;
            }
            var typeSelector = VertexTypeSelector.fromString(string, context);
            if (typeSelector.successful()) {
                selectors.add(typeSelector.getResult());
                continue;
            }
            break;
        }
        if (selectors.isEmpty()) {
            string.setPosition(position);
            return ParseResult.error("Keyword " + DSL_KEYWORD + " is missing any selectors!");
        }
        VertexDestinationSelectors vertexDestinationSelectors = new VertexDestinationSelectors(selectors);
        return ParseResult.ok(vertexDestinationSelectors);
    }
}
