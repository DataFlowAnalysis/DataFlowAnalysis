package org.dataflowanalysis.analysis.dsl.selectors;

import java.util.List;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;

public interface SetOperation {
	/**
	 * Matches the given vertex and variable name in the given dsl context
	 * @param vertex Vertex that is matched
	 * @param variableName Variable name that is matched upon
	 * @param context DSL context
	 * @return Returns a list of all matched attributes of the vertex
	 */
	List<String> match(AbstractVertex<?> vertex, String variableName, DSLContext context);
}
