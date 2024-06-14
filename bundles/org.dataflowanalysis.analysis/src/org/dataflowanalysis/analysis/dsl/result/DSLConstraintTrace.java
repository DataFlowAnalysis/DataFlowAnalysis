package org.dataflowanalysis.analysis.dsl.result;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.ConditionalSelector;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DSLConstraintTrace {
    private final Map<AbstractVertex<?>, AbstractSelector> missingSelectors;
    private final Map<AbstractVertex<?>, ConditionalSelector> missingConditionalSelectors;

    public DSLConstraintTrace() {
        this.missingSelectors = new HashMap<>();
        this.missingConditionalSelectors = new HashMap<>();
    }

    public void addMissingSelector(AbstractVertex<?> key, AbstractSelector missingSelector) {
        this.missingSelectors.put(key, missingSelector);
    }

    public void addMissingConditionalSelector(AbstractVertex<?> key, ConditionalSelector missingSelector) {
        this.missingConditionalSelectors.put(key, missingSelector);
    }

    public Optional<AbstractSelector> getMissingSelectors(AbstractVertex<?> vertex) {
        return Optional.ofNullable(this.missingSelectors.get(vertex));
    }

    public Optional<ConditionalSelector> getMissingConditionalSelectors(AbstractVertex<?> vertex) {
        return Optional.ofNullable(this.missingConditionalSelectors.get(vertex));
    }
}
