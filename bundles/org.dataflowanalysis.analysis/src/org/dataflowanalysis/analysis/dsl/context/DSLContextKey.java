package org.dataflowanalysis.analysis.dsl.context;

import org.dataflowanalysis.analysis.core.AbstractVertex;

public record DSLContextKey(String variableName, AbstractVertex<?> vertex) {
    public static DSLContextKey of(String variableName, AbstractVertex<?> vertex) {
        return new DSLContextKey(variableName, vertex);
    }
}
