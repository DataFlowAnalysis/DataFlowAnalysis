package org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.dataflow;

import java.util.Optional;

import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.ConfidentialityFactory;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.expression.ExpressionFactory;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.expression.LhsEnumCharacteristicReference;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.expressions.ExpressionsFactory;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.expressions.Term;
import org.palladiosimulator.pcm.parameter.ParameterFactory;
import org.palladiosimulator.pcm.parameter.VariableUsage;
import org.palladiosimulator.pcm.seff.SetVariableAction;

public class SEFFCharacteristicsBuilder {
	private SetVariableAction action;
	
	private SEFFCharacteristicsBuilder(SetVariableAction action) {
		this.action = action;
	}
	
	public static SEFFCharacteristicsBuilder builder(SetVariableAction action) {
		return new SEFFCharacteristicsBuilder(action);
	}

	public SEFFCharacteristicsBuilder addCharacteristic(EnumCharacteristicType characteristicType, Optional<String> characteristicValue) {
		Literal literal = null;
		if (characteristicValue.isPresent()) {
			literal = characteristicType.getType().getLiterals().stream()
					.filter(it -> it.getName().equalsIgnoreCase(characteristicValue.get()))
					.findAny().orElseThrow(() -> new IllegalArgumentException("Unknown characteristic value"));
		}
		
		VariableUsage usage = ParameterFactory.eINSTANCE.createVariableUsage();
		ConfidentialityVariableCharacterisation characterisation = ConfidentialityFactory.eINSTANCE.createConfidentialityVariableCharacterisation();
		usage.setSetVariableAction_VariableUsage(action);
		characterisation.setVariableUsage_VariableCharacterisation(usage);
		LhsEnumCharacteristicReference lhs = ExpressionFactory.eINSTANCE.createLhsEnumCharacteristicReference();
 		lhs.setCharacteristicType(characteristicType);
		lhs.setLiteral(literal);
		characterisation.setLhs(lhs);
		Term term = ExpressionsFactory.eINSTANCE.createTrue();
		characterisation.setRhs(term);
		return this;
	}
}
