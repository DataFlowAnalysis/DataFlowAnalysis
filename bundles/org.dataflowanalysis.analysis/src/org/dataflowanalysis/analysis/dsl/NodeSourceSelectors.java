package org.dataflowanalysis.analysis.dsl;

import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class NodeSourceSelectors {
    private static final String DSL_KEYWORD = "node";

    private final List<AbstractSelector> selectors;


    public NodeSourceSelectors() {
        selectors = new ArrayList<>();
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

    public NodeSourceSelectors fromString(String string) {
        return new NodeSourceSelectors();
    }
}
