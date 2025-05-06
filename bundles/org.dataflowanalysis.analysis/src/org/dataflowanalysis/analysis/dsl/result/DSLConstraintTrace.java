package org.dataflowanalysis.analysis.dsl.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.ConditionalSelector;

/**
 * Constraint trace that contains missing selectors and conditional selectors of all vertices
 */
public class DSLConstraintTrace {
    private final Map<AbstractVertex<?>, List<AbstractSelector>> missingSelectors;
    private final Map<AbstractVertex<?>, List<ConditionalSelector>> missingConditionalSelectors;

    /**
     * Create a new empty dsl constraint trace
     */
    public DSLConstraintTrace() {
        this.missingSelectors = new HashMap<>();
        this.missingConditionalSelectors = new HashMap<>();
    }

    /**
     * Adds a missing selector at a given vertex
     * @param key Vertex that has a missing selector
     * @param missingSelector Selector that has not been fulfilled by the vertex
     */
    public void addMissingSelector(AbstractVertex<?> key, AbstractSelector missingSelector) {
        List<AbstractSelector> updatedValue = this.missingSelectors.getOrDefault(key, new ArrayList<>());
        updatedValue.add(missingSelector);
        this.missingSelectors.put(key, updatedValue);
    }

    /**
     * Adds a missing conditional selector at a given vertex
     * @param key Vertex that has a missing conditional selector
     * @param missingSelector Selector that has not been fulfilled by the vertex
     */
    public void addMissingConditionalSelector(AbstractVertex<?> key, ConditionalSelector missingSelector) {
        List<ConditionalSelector> updatedValue = this.missingConditionalSelectors.getOrDefault(key, new ArrayList<>());
        updatedValue.add(missingSelector);
        this.missingConditionalSelectors.put(key, updatedValue);
    }

    /**
     * Returns a list of missing selectors of a vertex
     * @param vertex Given vertex
     * @return Returns the missing selectors of a vertex
     */
    public Optional<List<AbstractSelector>> getMissingSelectors(AbstractVertex<?> vertex) {
        return Optional.ofNullable(this.missingSelectors.get(vertex));
    }

    /**
     * Returns a list of missing conditional selectors of a vertex
     * @param vertex Given vertex
     * @return Returns the missing conditional selectors of a vertex
     */
    public Optional<List<ConditionalSelector>> getMissingConditionalSelectors(AbstractVertex<?> vertex) {
        return Optional.ofNullable(this.missingConditionalSelectors.get(vertex));
    }
}
