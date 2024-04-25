package org.dataflowanalysis.analysis.pcm.informationflow.core.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.parameter.CharacterisedVariable;

import de.uka.ipd.sdq.stoex.Atom;
import de.uka.ipd.sdq.stoex.BoolLiteral;
import de.uka.ipd.sdq.stoex.BooleanOperatorExpression;
import de.uka.ipd.sdq.stoex.CompareExpression;
import de.uka.ipd.sdq.stoex.Expression;
import de.uka.ipd.sdq.stoex.FunctionLiteral;
import de.uka.ipd.sdq.stoex.IfElseExpression;
import de.uka.ipd.sdq.stoex.NegativeExpression;
import de.uka.ipd.sdq.stoex.NotExpression;
import de.uka.ipd.sdq.stoex.NumericLiteral;
import de.uka.ipd.sdq.stoex.Parenthesis;
import de.uka.ipd.sdq.stoex.PowerExpression;
import de.uka.ipd.sdq.stoex.ProbabilityFunctionLiteral;
import de.uka.ipd.sdq.stoex.ProductExpression;
import de.uka.ipd.sdq.stoex.StoexFactory;
import de.uka.ipd.sdq.stoex.StringLiteral;
import de.uka.ipd.sdq.stoex.TermExpression;
import de.uka.ipd.sdq.stoex.Variable;
import de.uka.ipd.sdq.stoex.VariableReference;

/**
 * A Utils class for Variables in stoex Expressions.
 *
 */
public class IFStoexUtils {

	private static final Logger logger = Logger.getLogger(IFStoexUtils.class);

	private static final StoexFactory stoexFactory = StoexFactory.eINSTANCE;

	private IFStoexUtils() {
	}

	/**
	 * Returns a List of all Variables used in the given expression. The result may
	 * contain duplicates. Expects a non-null expression.
	 * 
	 * @param expression the given expression
	 * @return a List of all Variables used
	 */
	public static List<CharacterisedVariable> findVariablesInExpression(Expression expression) {
		// Explicitly differentiated between atoms for debugging purposes.
		// (Missing an important element like Parenthesis is more obvious.)
		if (expression instanceof Variable) {
			return findVariableInVariable((Variable) expression);
		} else if (expression instanceof BooleanOperatorExpression) {
			return findVariablesInBooleanOperatorExpression((BooleanOperatorExpression) expression);
		} else if (expression instanceof IfElseExpression) {
			return findVariablesInIfElseExpression((IfElseExpression) expression);
		} else if (expression instanceof CompareExpression) {
			return findVariablesInCompareExpression((CompareExpression) expression);
		} else if (expression instanceof TermExpression) {
			return findVariablesInTermExpression((TermExpression) expression);
		} else if (expression instanceof ProductExpression) {
			return findVariablesInProductExpression((ProductExpression) expression);
		} else if (expression instanceof PowerExpression) {
			return findVariablesInPowerExpression((PowerExpression) expression);
		} else if (expression instanceof NegativeExpression) {
			return findVariablesInNegativeExpression((NegativeExpression) expression);
		} else if (expression instanceof NotExpression) {
			return findVariablesInNotExpression((NotExpression) expression);
		} else if (expression instanceof Parenthesis) {
			return findVariablesInParenthesis((Parenthesis) expression);
		} else if (expression instanceof FunctionLiteral) {
			return findVariableInNotVariableAtom((FunctionLiteral) expression);
		} else if (expression instanceof ProbabilityFunctionLiteral) {
			return findVariableInNotVariableAtom((ProbabilityFunctionLiteral) expression);
		} else if (expression instanceof StringLiteral) {
			return findVariableInNotVariableAtom((StringLiteral) expression);
		} else if (expression instanceof BoolLiteral) {
			return findVariableInNotVariableAtom((BoolLiteral) expression);
		} else if (expression instanceof NumericLiteral) { // Interface for IntLiteral and DoubleLiteral
			return findVariableInNotVariableAtom((NumericLiteral) expression);
		} else {
			String errorMessage = "Unexpected element in stoex Expression: " + expression;
			logger.error(errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}
	}

	private static List<CharacterisedVariable> findVariableInVariable(Variable variable) {
		// Assumes that CharacterisedVariable is the sole implementation of Variable.
		if (variable instanceof CharacterisedVariable) {
			var vars = new ArrayList<CharacterisedVariable>();
			vars.add((CharacterisedVariable) variable);
			return vars;

		} else {
			String errorMessage = "Unexpected Variable element in stoex Expression:" + variable;
			logger.error(errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}
	}

	private static List<CharacterisedVariable> findVariableInNotVariableAtom(Atom atom) {
		return new ArrayList<CharacterisedVariable>();
	}

	private static List<CharacterisedVariable> findVariablesInBooleanOperatorExpression(
			BooleanOperatorExpression expression) {
		var variables = findVariablesInExpression(expression.getLeft());
		variables.addAll(findVariablesInExpression(expression.getRight()));
		return variables;
	}

	private static List<CharacterisedVariable> findVariablesInIfElseExpression(IfElseExpression expression) {
		var variables = findVariablesInExpression(expression.getConditionExpression());
		variables.addAll(findVariablesInExpression(expression.getIfExpression()));
		variables.addAll(findVariablesInExpression(expression.getElseExpression()));
		return variables;
	}

	private static List<CharacterisedVariable> findVariablesInCompareExpression(CompareExpression expression) {
		var variables = findVariablesInExpression(expression.getLeft());
		variables.addAll(findVariablesInExpression(expression.getRight()));
		return variables;
	}

	private static List<CharacterisedVariable> findVariablesInTermExpression(TermExpression expression) {
		var variables = findVariablesInExpression(expression.getLeft());
		variables.addAll(findVariablesInExpression(expression.getRight()));
		return variables;
	}

	private static List<CharacterisedVariable> findVariablesInProductExpression(ProductExpression expression) {
		var variables = findVariablesInExpression(expression.getLeft());
		variables.addAll(findVariablesInExpression(expression.getRight()));
		return variables;
	}

	private static List<CharacterisedVariable> findVariablesInPowerExpression(PowerExpression expression) {
		var variables = findVariablesInExpression(expression.getBase());
		variables.addAll(findVariablesInExpression(expression.getExponent()));
		return variables;
	}

	private static List<CharacterisedVariable> findVariablesInNegativeExpression(NegativeExpression expression) {
		return findVariablesInExpression(expression.getInner());
	}

	private static List<CharacterisedVariable> findVariablesInNotExpression(NotExpression expression) {
		return findVariablesInExpression(expression.getInner());
	}

	private static List<CharacterisedVariable> findVariablesInParenthesis(Parenthesis expression) {
		return findVariablesInExpression(expression.getInnerExpression());
	}

	/**
	 * Create a {@link VariableReference} with the given name
	 * 
	 * @param name the name
	 * @return the created reference
	 */
	public static VariableReference createReferenceFromName(String name) {
		var variableReference = stoexFactory.createVariableReference();
		variableReference.setReferenceName(name);
		return variableReference;
	}

}
