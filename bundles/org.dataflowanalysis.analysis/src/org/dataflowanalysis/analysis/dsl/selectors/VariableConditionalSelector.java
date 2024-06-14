package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.DSLContext;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariable;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;

import java.util.List;
import java.util.Optional;

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
		List<ConstraintVariable> variables = context.getMappings(vertex);
		Optional<ConstraintVariable> variable = variables.stream()
				.filter(it -> it.getName().equals(this.constraintVariable.name()))
				.findAny();
		if (variable.isEmpty()) {
			return false;
		}
		if (!variable.get().hasValues()) {
			return false;
		}
		return this.inverted == variable.get().getPossibleValues().get().isEmpty();
	}

}
