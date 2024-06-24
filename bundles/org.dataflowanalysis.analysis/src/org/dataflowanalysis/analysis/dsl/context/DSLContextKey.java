package org.dataflowanalysis.analysis.dsl.context;

import org.dataflowanalysis.analysis.core.AbstractVertex;

/**
 * Represents a context of a dsl constraint variable
 * @param variableName Variable name of the constraint variable
 * @param vertex Vertex that has the constraint variable value
 */
public record DSLContextKey(String variableName, AbstractVertex<?> vertex) {
    /**
     * Create a new DSL context key with the given variable name and vertex
     * @param variableName Given variable name
     * @param vertex Given vertex
     * @return Returns a dsl context key with the variable name and vertex
     */
    public static DSLContextKey of(String variableName, AbstractVertex<?> vertex) {
        return new DSLContextKey(variableName, vertex);
    }
}
