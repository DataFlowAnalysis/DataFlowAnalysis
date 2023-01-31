package org.palladiosimulator.dataflow.confidentiality.scalability.factory;

import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.ConfidentialityFactory;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.EnumCharacteristic;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.expression.ExpressionFactory;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.expression.LhsEnumCharacteristicReference;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.expressions.ExpressionsFactory;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.expressions.Term;
import org.palladiosimulator.pcm.parameter.ParameterFactory;
import org.palladiosimulator.pcm.parameter.VariableUsage;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;

public class PCMUsageFactory {
	private UsageScenario usageScenario;
	private ScenarioBehaviour scenarioBehaviour;
	
	public PCMUsageFactory(UsageModel usageModel) {
		this.usageScenario = UsagemodelFactory.eINSTANCE.createUsageScenario();
		usageScenario.setUsageModel_UsageScenario(usageModel);
		this.scenarioBehaviour = UsagemodelFactory.eINSTANCE.createScenarioBehaviour();
		usageScenario.setScenarioBehaviour_UsageScenario(scenarioBehaviour);
		scenarioBehaviour.setUsageScenario_SenarioBehaviour(usageScenario);
		
		AbstractUserAction startAction = UsagemodelFactory.eINSTANCE.createStart();
		startAction.setScenarioBehaviour_AbstractUserAction(scenarioBehaviour);
		scenarioBehaviour.getActions_ScenarioBehaviour().add(startAction);
	}
	
	public void addCall(OperationProvidedRole operationProvidedRole) {
		EntryLevelSystemCall callAction = UsagemodelFactory.eINSTANCE.createEntryLevelSystemCall();
		callAction.setProvidedRole_EntryLevelSystemCall(operationProvidedRole);
		callAction.setScenarioBehaviour_AbstractUserAction(scenarioBehaviour);
		callAction.setPredecessor(scenarioBehaviour.getActions_ScenarioBehaviour().get(scenarioBehaviour.getActions_ScenarioBehaviour().size() - 1));
		scenarioBehaviour.getActions_ScenarioBehaviour().get(scenarioBehaviour.getActions_ScenarioBehaviour().size() - 1).setSuccessor(callAction);
	}
	
	public void addCharacterizationCall() {
		VariableUsage usage = ParameterFactory.eINSTANCE.createVariableUsage();
		ConfidentialityVariableCharacterisation characterisation = ConfidentialityFactory.eINSTANCE.createConfidentialityVariableCharacterisation();
		EntryLevelSystemCall callAction = (EntryLevelSystemCall) scenarioBehaviour.getActions_ScenarioBehaviour().get(scenarioBehaviour.getActions_ScenarioBehaviour().size() - 1);
		usage.setEntryLevelSystemCall_InputParameterUsage(callAction);
		characterisation.setVariableUsage_VariableCharacterisation(usage);
		characterisation.setLhs(null);
		characterisation.setRhs(null);
	}
	
	public void addCharacterizationReturn(EnumCharacteristic characteristic, Literal characteristicValue) {
		VariableUsage usage = ParameterFactory.eINSTANCE.createVariableUsage();
		ConfidentialityVariableCharacterisation characterisation = ConfidentialityFactory.eINSTANCE.createConfidentialityVariableCharacterisation();
		EntryLevelSystemCall callAction = (EntryLevelSystemCall) scenarioBehaviour.getActions_ScenarioBehaviour().get(scenarioBehaviour.getActions_ScenarioBehaviour().size() - 1);
		usage.setEntryLevelSystemCall_OutputParameterUsage(callAction);
		characterisation.setVariableUsage_VariableCharacterisation(usage);
		LhsEnumCharacteristicReference lhs = ExpressionFactory.eINSTANCE.createLhsEnumCharacteristicReference();
 		lhs.setCharacteristicType(characteristic.getType());
		lhs.setLiteral(characteristicValue);
		characterisation.setLhs(lhs);
		Term term = ExpressionsFactory.eINSTANCE.createTrue();
		characterisation.setRhs(term);
	}
	
	public UsageScenario getUsageScenario() {
		AbstractUserAction stopAction = UsagemodelFactory.eINSTANCE.createStop();
		stopAction.setScenarioBehaviour_AbstractUserAction(scenarioBehaviour);
		stopAction.setPredecessor(scenarioBehaviour.getActions_ScenarioBehaviour().get(scenarioBehaviour.getActions_ScenarioBehaviour().size() - 1));
		scenarioBehaviour.getActions_ScenarioBehaviour().get(scenarioBehaviour.getActions_ScenarioBehaviour().size() - 1).setSuccessor(stopAction);
		return this.usageScenario;
	}
}
