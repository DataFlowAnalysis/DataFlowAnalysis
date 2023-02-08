package org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.dataflow;

import java.util.Optional;
import java.util.UUID;

import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.ConfidentialityFactory;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.expression.ExpressionFactory;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.expression.LhsEnumCharacteristicReference;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.expression.NamedEnumCharacteristicReference;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.expressions.ExpressionsFactory;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.expressions.Term;
import org.palladiosimulator.pcm.parameter.ParameterFactory;
import org.palladiosimulator.pcm.parameter.VariableUsage;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

import de.uka.ipd.sdq.stoex.AbstractNamedReference;
import de.uka.ipd.sdq.stoex.StoexFactory;

public class UsageCallBuilder {
	private UsageBuilder builder;
	private EntryLevelSystemCall call;
	
	private UsageCallBuilder(EntryLevelSystemCall call, UsageBuilder builder) {
		this.call = call;
		this.builder = builder;
	}

	public static UsageCallBuilder builder(EntryLevelSystemCall call, UsageBuilder builder) {
		return new UsageCallBuilder(call, builder);
	}
	
	public UsageCallBuilder setCallee(OperationProvidedRole providedRole, OperationSignature signature) {
		this.call.setProvidedRole_EntryLevelSystemCall(providedRole);
		this.call.setOperationSignature__EntryLevelSystemCall(signature);
		return this;
	}
	
	public UsageCallBuilder addInputCharacteristic(String variable, EnumCharacteristicType characteristicType, Optional<String> characteristicValue) {
		Literal literal = null;
		if (characteristicValue.isPresent()) {
			literal = characteristicType.getType().getLiterals().stream()
					.filter(it -> it.getName().equalsIgnoreCase(characteristicValue.get()))
					.findAny().orElseThrow(() -> new IllegalArgumentException("Unknown characteristic value"));
		}
		VariableUsage usage = ParameterFactory.eINSTANCE.createVariableUsage();
		AbstractNamedReference reference = StoexFactory.eINSTANCE.createVariableReference();
		reference.setReferenceName(variable);
		usage.setNamedReference__VariableUsage(reference);
		ConfidentialityVariableCharacterisation characterisation = ConfidentialityFactory.eINSTANCE.createConfidentialityVariableCharacterisation();
		usage.setEntryLevelSystemCall_InputParameterUsage(call);
		characterisation.setVariableUsage_VariableCharacterisation(usage);
		LhsEnumCharacteristicReference lhs = ExpressionFactory.eINSTANCE.createLhsEnumCharacteristicReference();
		lhs.setCharacteristicType(characteristicType);
		lhs.setLiteral(literal);
		characterisation.setLhs(lhs);
		Term term = ExpressionsFactory.eINSTANCE.createTrue();
		characterisation.setRhs(term);
		return this;
	}
	
	public UsageCallBuilder addOutputCharacteristic(String variable, EnumCharacteristicType characteristicType, Optional<String> characteristicValue) {
		Literal literal = null;
		if (characteristicValue.isPresent()) {
			literal = characteristicType.getType().getLiterals().stream()
					.filter(it -> it.getName().equalsIgnoreCase(characteristicValue.get()))
					.findAny().orElseThrow(() -> new IllegalArgumentException("Unknown characteristic value"));
		}
		VariableUsage usage = ParameterFactory.eINSTANCE.createVariableUsage();
		AbstractNamedReference reference = StoexFactory.eINSTANCE.createVariableReference();
		reference.setReferenceName(variable);
		usage.setNamedReference__VariableUsage(reference);
		ConfidentialityVariableCharacterisation characterisation = ConfidentialityFactory.eINSTANCE.createConfidentialityVariableCharacterisation();
		usage.setEntryLevelSystemCall_OutputParameterUsage(call);
		characterisation.setVariableUsage_VariableCharacterisation(usage);
		LhsEnumCharacteristicReference lhs = ExpressionFactory.eINSTANCE.createLhsEnumCharacteristicReference();
 		lhs.setCharacteristicType(characteristicType);
		lhs.setLiteral(literal);
		characterisation.setLhs(lhs);
		Term term = ExpressionsFactory.eINSTANCE.createTrue();
		characterisation.setRhs(term);
		return this;
	}
	
	public UsageCallBuilder addDefaultReturn(String variable) {
		VariableUsage usage = ParameterFactory.eINSTANCE.createVariableUsage();
		usage.setEntryLevelSystemCall_OutputParameterUsage(call);
		
		AbstractNamedReference reference = StoexFactory.eINSTANCE.createVariableReference();
		reference.setReferenceName(variable);
		usage.setNamedReference__VariableUsage(reference);
		
		ConfidentialityVariableCharacterisation characterisation = ConfidentialityFactory.eINSTANCE.createConfidentialityVariableCharacterisation();
		characterisation.setVariableUsage_VariableCharacterisation(usage);
		
		LhsEnumCharacteristicReference lhs = ExpressionFactory.eINSTANCE.createLhsEnumCharacteristicReference();
 		lhs.setCharacteristicType(null);
		lhs.setLiteral(null);
		characterisation.setLhs(lhs);
		
		NamedEnumCharacteristicReference term = ExpressionFactory.eINSTANCE.createNamedEnumCharacteristicReference();
		AbstractNamedReference returnReference = StoexFactory.eINSTANCE.createVariableReference();
		returnReference.setReferenceName("RETURN");
		term.setNamedReference(returnReference);
		term.setId(UUID.randomUUID().toString());
		term.setLiteral(null);
		term.setCharacteristicType(null);
		characterisation.setRhs(term);
		return this;
	}
	
	public UsageBuilder buildCall() {
		return this.builder;
	}
}
