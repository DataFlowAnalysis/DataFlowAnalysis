package org.dataflowanalysis.analysis.dsl;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariable;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DSLContext {
    private final Map<DSLContextKey, List<ConstraintVariable>> context;

    public DSLContext() {
        this.context = new HashMap<>();
    }

    public void addMapping(DSLContextKey key, ConstraintVariable value) {
        if (this.context.containsKey(key)) {
            this.context.get(key).add(value);
        } else {
            List<ConstraintVariable> values = new ArrayList<>();
            values.add(value);
            this.context.put(key, values);
        }
    }

    public ConstraintVariable getMapping(DSLContextKey key, ConstraintVariableReference reference) {
        if (reference.name().equals("constant")) {
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

    public List<ConstraintVariable> getMappings(AbstractVertex<?> vertex) {
        return this.context.entrySet().stream()
                .filter(it -> it.getKey().vertex().equals(vertex))
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .toList();
    }
}
