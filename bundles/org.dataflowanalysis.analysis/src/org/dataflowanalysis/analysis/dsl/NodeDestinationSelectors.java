package org.dataflowanalysis.analysis.dsl;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.DataCharacteristicsSelector;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class NodeDestinationSelectors {
    private static final String DSL_KEYWORD = "node";
    private static final Logger logger = Logger.getLogger(NodeDestinationSelectors.class);

    private final List<AbstractSelector> selectors;


    public NodeDestinationSelectors() {
        selectors = new ArrayList<>();
    }

    public NodeDestinationSelectors(List<AbstractSelector> selectors) {
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

    public static ParseResult<NodeDestinationSelectors> fromString(StringView string, DSLContext context) {
        if (!string.getString().startsWith(DSL_KEYWORD)) {
            return ParseResult.error("String did not start with " + DSL_KEYWORD);
        }
        string.advance(DSL_KEYWORD.length() + 1);
        logger.info("Parsing: " + string.getString());
        List<AbstractSelector> selectors = new ArrayList<>();
        while (!string.invalid()) {
            var selector = DataCharacteristicsSelector.fromString(string, context);
            if (selector.successful()) {
                selectors.add(selector.getResult());
            } else {
                break;
            }
        }
        if (selectors.isEmpty()) {
            return ParseResult.error("Keyword " + DSL_KEYWORD + " is missing any selectors!");
        }
        NodeDestinationSelectors nodeDestinationSelectors = new NodeDestinationSelectors(selectors);
        return ParseResult.ok(nodeDestinationSelectors);
    }
}

