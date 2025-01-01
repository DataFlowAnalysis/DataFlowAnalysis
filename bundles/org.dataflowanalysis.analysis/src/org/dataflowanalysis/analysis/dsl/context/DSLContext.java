package org.dataflowanalysis.analysis.dsl.context;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariable;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the constraint variable context of one constraint
 */
public class DSLContext {
    private final Map<DSLContextKey, List<ConstraintVariable>> context;
    private final Optional<DSLContextProvider> contextProvider;

    /**
     * Create a new empty dsl context
     */
    public DSLContext() {
        this.context = new HashMap<>();
        this.contextProvider = Optional.empty();
    }

    public DSLContext(DSLContextProvider contextProvider) {
        this.context = new HashMap<>();
        this.contextProvider = Optional.ofNullable(contextProvider);
    }

    /**
     * Add the given constraint variable with the given key
     * @param key DSL Context key of the given constraint variable value
     * @param value Constraint variable value of the context key
     */
    public void addMapping(DSLContextKey key, ConstraintVariable value) {
        if (this.context.containsKey(key)) {
            this.context.get(key).add(value);
        } else {
            List<ConstraintVariable> values = new ArrayList<>();
            values.add(value);
            this.context.put(key, values);
        }
    }

    /**
     * Get the constraint variable in a given context with a reference
     * @param key Given context of the constraint variable reference
     * @param reference Given constraint variable reference
     * @return Returns the constraint variable value of the reference in the given context
     */
    public ConstraintVariable getMapping(DSLContextKey key, ConstraintVariableReference reference) {
        if (reference.name().equals(ConstraintVariable.CONSTANT_NAME)) {
            return new ConstraintVariable(reference.name(), new ArrayList<>(reference.values().get()));
        }
        if (!this.context.containsKey(key)) {
            ConstraintVariable variable = new ConstraintVariable(reference.name(), reference.values());
            this.addMapping(key, variable);
            return variable;
        }
        return this.context.get(key).stream()
                .filter(it -> it.getName().equals(reference.name()))
                .findFirst().orElseGet(() -> {
                    ConstraintVariable variable = new ConstraintVariable(reference.name(), reference.values());
                    this.addMapping(key, variable);
                    return variable;
                });
    }

    /**
     * Returns the constraint variables of a given vertex
     * @param vertex Given vertex of which the constraint variables are calculated
     * @return Returns a list of all constraint variables of a given vertex
     */
    public List<ConstraintVariable> getMappings(AbstractVertex<?> vertex) {
        return this.context.entrySet().stream()
                .filter(it -> it.getKey().vertex().equals(vertex))
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .toList();
    }

    public Optional<DSLContextProvider> getContextProvider() {
        return contextProvider;
    }
}
