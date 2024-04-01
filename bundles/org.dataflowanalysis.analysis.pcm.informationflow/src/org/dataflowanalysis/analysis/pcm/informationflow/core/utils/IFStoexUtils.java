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

	private static Logger logger = Logger.getLogger(IFStoexUtils.class);

	private static final StoexFactory stoexFac = StoexFactory.eINSTANCE;

	private IFStoexUtils() {
	}

	// TODO Maybe filter duplicates here? Or additional method?
	// TODO Maybe Variable is an sufficient abstraction for our use case?
	// TODO Set as better abstraction than List?
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
			String errorMsg = "Unexpected element in stoex Expression: " + expression;
			logger.error(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		}
	}

	private static List<CharacterisedVariable> findVariableInVariable(Variable variable) {
		// Assumes that CharacterisedVariable is the sole implementation of Variable.
		if (variable instanceof CharacterisedVariable) {
			var vars = new ArrayList<CharacterisedVariable>();
			vars.add((CharacterisedVariable) variable);
			return vars;

		} else {
			String errorMsg = "Unexpected Variable element in stoex Expression:" + variable;
			logger.error(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		}
	}

	private static List<CharacterisedVariable> findVariableInNotVariableAtom(Atom atom) {
		return new ArrayList<CharacterisedVariable>();
	}

	private static List<CharacterisedVariable> findVariablesInBooleanOperatorExpression(
			BooleanOperatorExpression expression) {
		var vars = findVariablesInExpression(expression.getLeft());
		vars.addAll(findVariablesInExpression(expression.getRight()));
		return vars;
	}

	private static List<CharacterisedVariable> findVariablesInIfElseExpression(IfElseExpression expression) {
		var vars = findVariablesInExpression(expression.getConditionExpression());
		vars.addAll(findVariablesInExpression(expression.getIfExpression()));
		vars.addAll(findVariablesInExpression(expression.getElseExpression()));
		return vars;
	}

	private static List<CharacterisedVariable> findVariablesInCompareExpression(CompareExpression expression) {
		var vars = findVariablesInExpression(expression.getLeft());
		vars.addAll(findVariablesInExpression(expression.getRight()));
		return vars;
	}

	private static List<CharacterisedVariable> findVariablesInTermExpression(TermExpression expression) {
		var vars = findVariablesInExpression(expression.getLeft());
		vars.addAll(findVariablesInExpression(expression.getRight()));
		return vars;
	}

	private static List<CharacterisedVariable> findVariablesInProductExpression(ProductExpression expression) {
		var vars = findVariablesInExpression(expression.getLeft());
		vars.addAll(findVariablesInExpression(expression.getRight()));
		return vars;
	}

	private static List<CharacterisedVariable> findVariablesInPowerExpression(PowerExpression expression) {
		var vars = findVariablesInExpression(expression.getBase());
		vars.addAll(findVariablesInExpression(expression.getExponent()));
		return vars;
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
		var variableRef = stoexFac.createVariableReference();
		variableRef.setReferenceName("x");
		return variableRef;
	}

}
