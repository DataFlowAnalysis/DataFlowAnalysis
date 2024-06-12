package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.DSLContext;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariable;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;

public class VariableConditionalSelector implements ConditionalSelector {
	private final ConstraintVariableReference constraintVariable;
	private final boolean inverted;

	public VariableConditionalSelector(ConstraintVariableReference constraintVariable) {
		this.constraintVariable = constraintVariable;
		this.inverted = false;
	}

	public VariableConditionalSelector(ConstraintVariableReference constraintVariable, boolean inverted) {
		this.constraintVariable = constraintVariable;
		this.inverted = inverted;
	}

	@Override
	public boolean matchesSelector(AbstractVertex<?> vertex, DSLContext context) {
		ConstraintVariable variable = context.getMapping(vertex, this.constraintVariable);
		if (!variable.hasValues()) {
			return false;
		}
		return this.inverted == variable.getPossibleValues().get().isEmpty();
	}

}
