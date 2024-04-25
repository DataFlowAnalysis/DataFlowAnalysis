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

public class ConfidentialityVariableCharacterisationTestUtils {

	private static final Logger logger = Logger.getLogger(ConfidentialityVariableCharacterisationTestUtils.class);

	private ConfidentialityVariableCharacterisationTestUtils() {
	}

	public static boolean evaluateConfidentialityCharacterisationLatticeMapping(
			List<ConfidentialityVariableCharacterisation> characterisations, Enumeration lattice,
			Map<String, String> variableToLiteralMapping, String expectedLevel) {

		Map<String, Map<String, Boolean>> variableToLiteralToBoolean = new HashMap<>();

		for (String variable : variableToLiteralMapping.keySet()) {
			String variableLevel = variableToLiteralMapping.get(variable);
			Map<String, Boolean> litteralToBoolean = new HashMap<>();
			for (Literal level : lattice.getLiterals()) {
				if (level.getName().equals(variableLevel)) {
					litteralToBoolean.put(level.getName(), true);
				} else {
					litteralToBoolean.put(level.getName(), false);
				}
			}
			variableToLiteralToBoolean.put(variable, litteralToBoolean);
		}

		for (var characterisation : characterisations) {
			var lhs = (LhsEnumCharacteristicReference) characterisation.getLhs();
			String setLevelName = lhs.getLiteral().getName();
			boolean isSet = evaluateConfidentialityCharacterisationForBooleanMapping(characterisation,
					variableToLiteralToBoolean);

			if (isSet && !setLevelName.equals(expectedLevel))
				return false;
			if (!isSet && setLevelName.equals(expectedLevel))
				return false;
		}
		return true;
	}

	public static boolean evaluateConfidentialityCharacterisationForBooleanMapping(
			ConfidentialityVariableCharacterisation characterisation, Map<String, Map<String, Boolean>> nameToBoolean) {

		Term rhs = characterisation.getRhs();
		return evaluateTerm(rhs, nameToBoolean);
	}

	private static boolean evaluateTerm(Term term, Map<String, Map<String, Boolean>> nameAndLiteralToBoolean) {
		if (term instanceof True) {
			return true;
		} else if (term instanceof False) {
			return false;
		} else if (term instanceof NamedEnumCharacteristicReference namedReference) {
			return evaluateVariable(namedReference, nameAndLiteralToBoolean);
		} else if (term instanceof And andTerm) {
			return evaluateTerm(andTerm.getLeft(), nameAndLiteralToBoolean)
					&& evaluateTerm(andTerm.getRight(), nameAndLiteralToBoolean);
		} else if (term instanceof Or orTerm) {
			return evaluateTerm(orTerm.getLeft(), nameAndLiteralToBoolean)
					|| evaluateTerm(orTerm.getRight(), nameAndLiteralToBoolean);
		} else if (term instanceof Not notTerm) {
			return !evaluateTerm(notTerm.getTerm(), nameAndLiteralToBoolean);
		} else {
			String errorMessage = "Unknown term element in ConfidentialityVariableCharacterisations rhs.";
			logger.error(errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}
	}

	private static boolean evaluateVariable(NamedEnumCharacteristicReference namedReference,
			Map<String, Map<String, Boolean>> nameAndLiteralToBoolean) {
		String variableName = namedReference.getNamedReference().getReferenceName();
		var literalToBoolean = nameAndLiteralToBoolean.get(variableName);
		if (literalToBoolean == null)
			throwMissingVariableMapping(variableName);
		String litteralName = namedReference.getLiteral().getName();
		Boolean result = literalToBoolean.get(litteralName);
		if (result == null)
			throwMissingLiteralMapping(variableName, litteralName);
		return result;
	}

	private static void throwMissingVariableMapping(String variableName) {
		String errorMessage = "The variable '" + variableName + "' is missing in the mapping";
		logger.error(errorMessage);
		throw new IllegalArgumentException(errorMessage);
	}

	private static void throwMissingLiteralMapping(String variableName, String litteralName) {
		String errorMessage = "The litteral '" + litteralName + "' is missing in the mapping for the variable '"
				+ variableName + "'.";
		logger.error(errorMessage);
		throw new IllegalArgumentException(errorMessage);
	}

	public static String cvcAsString(ConfidentialityVariableCharacterisation characterisation) {
		var lhs = (LhsEnumCharacteristicReference) characterisation.getLhs();
		var lhsVariable = characterisation.getVariableUsage_VariableCharacterisation()
				.getNamedReference__VariableUsage();
		Term rhs = characterisation.getRhs();
		return lhsAsString(lhsVariable, lhs) + " := " + termAsString(rhs);
	}

	private static String lhsAsString(AbstractNamedReference var, LhsEnumCharacteristicReference lhs) {
		return var.getReferenceName() + "." + lhs.getCharacteristicType().getName() + "." + lhs.getLiteral().getName();
	}

	private static String termAsString(Term term) {
		if (term instanceof True) {
			return "true";
		} else if (term instanceof False) {
			return "false";
		} else if (term instanceof NamedEnumCharacteristicReference namedReference) {
			return namedReference.getNamedReference().getReferenceName() + "."
					+ namedReference.getCharacteristicType().getName() + "." + namedReference.getLiteral().getName();
		} else if (term instanceof And andTerm) {
			return "(" + termAsString(andTerm.getLeft()) + ")" + " & " + "(" + termAsString(andTerm.getRight()) + ")";
		} else if (term instanceof Or orTerm) {
			return termAsString(orTerm.getLeft()) + " | " + termAsString(orTerm.getRight());
		} else if (term instanceof Not notTerm) {
			return "!(" + termAsString(notTerm.getTerm()) + ")";
		} else {
			String errorMessage = "Unknown term element in ConfidentialityVariableCharacterisations rhs.";
			logger.error(errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}
	}
}
