package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;

public class VariableNameSelector extends DataSelector {
    private final String variableName;

    /**
     * Constructs a new instance of a {@link VariableNameSelector} with the given variable name
     * @param variableName Variable name the {@link DataSelector} should match
     */
    public VariableNameSelector(DSLContext context, String variableName) {
        super(context);
        this.variableName = variableName;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        return vertex.getAllDataCharacteristics().stream()
                .anyMatch(it -> it.variableName().equals(this.variableName));
    }
}
