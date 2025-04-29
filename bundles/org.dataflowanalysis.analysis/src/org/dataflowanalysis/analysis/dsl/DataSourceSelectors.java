package org.dataflowanalysis.analysis.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.DataCharacteristicListSelector;
import org.dataflowanalysis.analysis.dsl.selectors.DataCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.selectors.VariableNameSelector;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

/**
 * Represents the source data {@link AbstractSelector} matched by an {@link AnalysisConstraint}
 */
public class DataSourceSelectors extends AbstractParseable {
    private static final Logger logger = Logger.getLogger(DataSourceSelectors.class);
    private static final String DSL_KEYWORD = "data";

    private final List<AbstractSelector> selectors;

    public DataSourceSelectors() {
        selectors = new ArrayList<>();
    }

    public DataSourceSelectors(List<AbstractSelector> selectors) {
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
     * Parses the {@link DataSourceSelectors} of an {@link AnalysisConstraint}.
     * @param string String view on the string that is parsed
     * @param context DSL context used during parsing
     * @return Returns a {@link ParseResult} that may contain the {@link DataSourceSelectors} of the
     * {@link AnalysisConstraint}
     */
    public static ParseResult<DataSourceSelectors> fromString(StringView string, DSLContext context) {
        if (string.invalid()) {
            return ParseResult.error("Unexpected end of input!");
        }
        if (!string.getString()
                .startsWith(DSL_KEYWORD)) {
            return ParseResult.error("String did not start with " + DSL_KEYWORD);
        }
        string.advance(DSL_KEYWORD.length() + 1);
        if (string.invalid()) {
            return ParseResult.error("Unexpected end of input!");
        }
        logger.info("Parsing: " + string.getString());
        List<AbstractSelector> selectors = new ArrayList<>();
        while (!string.invalid()) {
            var listSelector = DataCharacteristicListSelector.fromString(string, context);
            if (listSelector.successful()) {
                selectors.add(listSelector.getResult());
                continue;
            }
            var selector = DataCharacteristicsSelector.fromString(string, context);
            if (selector.successful()) {
                selectors.add(selector.getResult());
                continue;
            }
            var nameSelector = VariableNameSelector.fromString(string, context);
            if (nameSelector.successful()) {
                selectors.add(nameSelector.getResult());
                continue;
            }
            break;
        }
        if (selectors.isEmpty()) {
            return ParseResult.error("Keyword " + DSL_KEYWORD + " is missing any selectors!");
        }
        DataSourceSelectors dataSourceSelectors = new DataSourceSelectors(selectors);
        return ParseResult.ok(dataSourceSelectors);
    }
}
