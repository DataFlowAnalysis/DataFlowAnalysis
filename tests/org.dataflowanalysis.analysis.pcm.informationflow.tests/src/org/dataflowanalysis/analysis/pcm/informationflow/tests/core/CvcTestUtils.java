package org.dataflowanalysis.analysis.pcm.informationflow.tests.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Enumeration;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.And;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.False;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.Not;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.Or;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.Term;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.True;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.dataflowanalysis.pcm.extension.model.confidentiality.expression.LhsEnumCharacteristicReference;
import org.dataflowanalysis.pcm.extension.model.confidentiality.expression.NamedEnumCharacteristicReference;

import de.uka.ipd.sdq.stoex.AbstractNamedReference;

public class CvcTestUtils {

	private static final Logger logger = Logger.getLogger(CvcTestUtils.class);

	private CvcTestUtils() {
	}

	public static boolean evaluateCvcLatticeMapping(List<ConfidentialityVariableCharacterisation> cvcs,
			Enumeration lattice, Map<String, String> variableToLiteralMapping, String expectedLevel) {

		Map<String, Map<String, Boolean>> varToLiteralToBoolean = new HashMap<>();

		for (String variable : variableToLiteralMapping.keySet()) {
			String varLevel = variableToLiteralMapping.get(variable);
			Map<String, Boolean> litteralToBoolean = new HashMap<>();
			for (Literal level : lattice.getLiterals()) {
				if (level.getName().equals(varLevel)) {
					litteralToBoolean.put(level.getName(), true);
				} else {
					litteralToBoolean.put(level.getName(), false);
				}
			}
			varToLiteralToBoolean.put(variable, litteralToBoolean);
		}

		for (var cvc : cvcs) {
			var lhs = (LhsEnumCharacteristicReference) cvc.getLhs();
			String setLevelName = lhs.getLiteral().getName();
			boolean isSet = evaluateCvcForBooleanMapping(cvc, varToLiteralToBoolean);

			if (isSet && !setLevelName.equals(expectedLevel))
				return false;
			if (!isSet && setLevelName.equals(expectedLevel))
				return false;
		}
		return true;
	}

	public static boolean evaluateCvcForBooleanMapping(ConfidentialityVariableCharacterisation cvc,
			Map<String, Map<String, Boolean>> nameToBoolean) {

		Term rhs = cvc.getRhs();
		return evaluateTerm(rhs, nameToBoolean);
	}

	private static boolean evaluateTerm(Term term, Map<String, Map<String, Boolean>> nameAndLiteralToBoolean) {
		if (term instanceof True) {
			return true;
		} else if (term instanceof False) {
			return false;
		} else if (term instanceof NamedEnumCharacteristicReference namedRef) {
			return evaluateVariable(namedRef, nameAndLiteralToBoolean);
		} else if (term instanceof And andTerm) {
			return evaluateTerm(andTerm.getLeft(), nameAndLiteralToBoolean)
					&& evaluateTerm(andTerm.getRight(), nameAndLiteralToBoolean);
		} else if (term instanceof Or orTerm) {
			return evaluateTerm(orTerm.getLeft(), nameAndLiteralToBoolean)
					|| evaluateTerm(orTerm.getRight(), nameAndLiteralToBoolean);
		} else if (term instanceof Not notTerm) {
			return !evaluateTerm(notTerm.getTerm(), nameAndLiteralToBoolean);
		} else {
			String errorMsg = "Unknown term element in ConfidentialityVariableCharacterisations rhs.";
			logger.error(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		}
	}

	private static boolean evaluateVariable(NamedEnumCharacteristicReference namedRef,
			Map<String, Map<String, Boolean>> nameAndLiteralToBoolean) {
		String varName = namedRef.getNamedReference().getReferenceName();
		var literalToBoolean = nameAndLiteralToBoolean.get(varName);
		if (literalToBoolean == null)
			throwMissingVariableMapping(varName);
		String litteralName = namedRef.getLiteral().getName();
		Boolean result = literalToBoolean.get(litteralName);
		if (result == null)
			throwMissingLiteralMapping(varName, litteralName);
		return result;
	}

	private static void throwMissingVariableMapping(String varName) {
		String errorMsg = "The variable '" + varName + "' is missing in the mapping";
		logger.error(errorMsg);
		throw new IllegalArgumentException(errorMsg);
	}

	private static void throwMissingLiteralMapping(String varName, String litteralName) {
		String errorMsg = "The litteral '" + litteralName + "' is missing in the mapping for the variable '" + varName
				+ "'.";
		logger.error(errorMsg);
		throw new IllegalArgumentException(errorMsg);
	}

	public static String cvcAsString(ConfidentialityVariableCharacterisation cvc) {
		var lhs = (LhsEnumCharacteristicReference) cvc.getLhs();
		var lhsVar = cvc.getVariableUsage_VariableCharacterisation().getNamedReference__VariableUsage();
		Term rhs = cvc.getRhs();
		return lhsAsString(lhsVar, lhs) + " := " + termAsString(rhs);
	}

	private static String lhsAsString(AbstractNamedReference var, LhsEnumCharacteristicReference lhs) {
		return var.getReferenceName() + "." + lhs.getCharacteristicType().getName() + "." + lhs.getLiteral().getName();
	}

	private static String termAsString(Term term) {
		if (term instanceof True) {
			return "true";
		} else if (term instanceof False) {
			return "false";
		} else if (term instanceof NamedEnumCharacteristicReference) {
			var namedRef = (NamedEnumCharacteristicReference) term;
			return namedRef.getNamedReference().getReferenceName() + "." + namedRef.getCharacteristicType().getName()
					+ "." + namedRef.getLiteral().getName();
		} else if (term instanceof And andTerm) {
			return "(" + termAsString(andTerm.getLeft()) + ")" + " & " + "(" + termAsString(andTerm.getRight()) + ")";
		} else if (term instanceof Or orTerm) {
			return termAsString(orTerm.getLeft()) + " | " + termAsString(orTerm.getRight());
		} else if (term instanceof Not notTerm) {
			return "!(" + termAsString(notTerm.getTerm()) + ")";
		} else {
			String errorMsg = "Unknown term element in ConfidentialityVariableCharacterisations rhs.";
			logger.error(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		}
	}
}
