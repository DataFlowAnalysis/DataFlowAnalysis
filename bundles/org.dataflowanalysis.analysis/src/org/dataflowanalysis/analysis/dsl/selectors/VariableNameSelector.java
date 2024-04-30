package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;

public class VariableNameSelector extends DataSelector {
    private final String variableName;

    /**
     * Constructs a new instance of a {@link VariableNameSelector} with the given variable name
     * @param variableName Variable name the {@link DataSelector} should match
     */
    public VariableNameSelector(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        return vertex.getAllDataCharacteristics().stream()
                .anyMatch(it -> it.variableName().equals(this.variableName));
    }
}
