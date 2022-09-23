package org.palladiosimulator.dataflow.confidentiality.analysis.sequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.palladiosimulator.dataflow.confidentiality.analysis.PCMAnalysisUtils;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.expression.LhsEnumCharacteristicReference;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.expressions.Term;

public class CharacteristicsCalculator {
	protected boolean evaulateRhs(Term term) {
		var characteristicType = term;
		return true;
	}
	
	public static void evaluateLhs(ConfidentialityVariableCharacterisation variableCharacterisation, String variableName, List<DataFlowVariable> oldVariables, List<DataFlowVariable> newVariables) {
		var lhs = (LhsEnumCharacteristicReference) variableCharacterisation.getLhs();
		var characteristicType = (EnumCharacteristicType) lhs.getCharacteristicType();
		var value = lhs.getLiteral();
		
		var existingVariable = oldVariables.stream().filter(it -> it.variableName().equals(variableName)).findAny();
		if (existingVariable.isPresent()) {
			handleInitializedValue(existingVariable.get(), characteristicType, value);
		} else {
			// Variable does not exist in scope
			DataFlowVariable newVariable = new DataFlowVariable(variableName);
			newVariable = newVariable.addCharacteristic(new CharacteristicValue(characteristicType, value));
			newVariables.add(newVariable);
			handleInitializedValue(newVariable, characteristicType, value);
		}
	}
	
	private static void handleInitializedValue(DataFlowVariable variable, EnumCharacteristicType characteristicType, Literal value) {
		var updatedCharacteristics = variable.getAllCharacteristics().stream()
				.filter(it -> characteristicType == null || it.characteristicType().equals(characteristicType))
				.filter(it -> value == null || it.characteristicLiteral().equals(value)).toList();
		if (value == null && characteristicType != null) {
			System.out.println("Query: " + characteristicType.getName() + ".*");
			discoverNewVariables(variable, Optional.of(characteristicType));
			return;
		}
		if (characteristicType == null) {
			System.out.println("Query: *.*");
			discoverNewVariables(variable, Optional.empty());
			return;
		}
		updatedCharacteristics.stream().forEach(it -> System.out.println("Updating variable: " + it.characteristicType().getName() + "." + it.characteristicLiteral().getName()));
	}
	
	private static List<DataFlowVariable> discoverNewVariables(DataFlowVariable variable, Optional<EnumCharacteristicType> characteristicType) {
		List<DataFlowVariable> newVariables = new ArrayList<>();
		return newVariables;
	}
}
