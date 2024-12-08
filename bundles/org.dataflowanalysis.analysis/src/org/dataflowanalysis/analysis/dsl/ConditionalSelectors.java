package org.dataflowanalysis.analysis.dsl;

import org.dataflowanalysis.analysis.dsl.selectors.ConditionalSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ConditionalSelectors {
    private static final String DSL_KEYWORD = "where";

    private final List<ConditionalSelector> selectors;


    public ConditionalSelectors() {
        selectors = new ArrayList<>();
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

    public NodeSourceSelectors fromString(String string) {
        return new NodeSourceSelectors();
    }
}
