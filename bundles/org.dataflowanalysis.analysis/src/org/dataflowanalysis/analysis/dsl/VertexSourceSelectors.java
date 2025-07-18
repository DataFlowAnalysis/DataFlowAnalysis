package org.dataflowanalysis.analysis.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.VertexCharacteristicsListSelector;
import org.dataflowanalysis.analysis.dsl.selectors.VertexCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.selectors.VertexTypeSelector;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

/**
 * Represents the source vertex {@link AbstractSelector} matched by an {@link AnalysisConstraint}
 */
public class VertexSourceSelectors extends AbstractParseable {
    private static final String DSL_KEYWORD = "vertex";
    private static final Logger logger = Logger.getLogger(VertexSourceSelectors.class);

    private final List<AbstractSelector> selectors;

    public VertexSourceSelectors() {
        selectors = new ArrayList<>();
    }

    public VertexSourceSelectors(List<AbstractSelector> selectors) {
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
     * Parses the {@link VertexSourceSelectors} of an {@link AnalysisConstraint}.
     * @param string String view on the string that is parsed
     * @param context DSL context used during parsing
     * @return Returns a {@link ParseResult} that may contain the {@link VertexSourceSelectors} of the
     * {@link AnalysisConstraint}
     */
    public static ParseResult<VertexSourceSelectors> fromString(StringView string, DSLContext context) {
        if (string.invalid()) {
            return ParseResult.error("Unexpected end of input!");
        }
        if (!string.getString()
                .startsWith(DSL_KEYWORD)) {
            return ParseResult.error("String did not start with " + DSL_KEYWORD);
        }
        string.advance(DSL_KEYWORD.length() + 1);
        if (string.invalid()) {
            string.retreat(DSL_KEYWORD.length() + 1);
            return ParseResult.error("Unexpected end of input!");
        }
        logger.info("Parsing: " + string.getString());
        List<AbstractSelector> selectors = new ArrayList<>();
        while (!string.invalid()) {
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

            var typeSelector = VertexTypeSelector.fromString(string, context);
            if (typeSelector.successful()) {
                selectors.add(typeSelector.getResult());
                continue;
            }
            break;
        }
        if (selectors.isEmpty()) {
            string.retreat(DSL_KEYWORD.length() + 1);
            return ParseResult.error("Keyword " + DSL_KEYWORD + " is missing any selectors!");
        }
        VertexSourceSelectors vertexSourceSelectors = new VertexSourceSelectors(selectors);
        return ParseResult.ok(vertexSourceSelectors);
    }
}
