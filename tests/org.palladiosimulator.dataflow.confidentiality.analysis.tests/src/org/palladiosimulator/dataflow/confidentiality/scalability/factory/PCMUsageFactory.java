package org.palladiosimulator.dataflow.confidentiality.scalability.factory;

import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.ConfidentialityFactory;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.CharacteristicsFactory;
import org.palladiosimulator.mdsdprofiles.api.ProfileAPI;
import org.palladiosimulator.pcm.PcmFactory;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;

public class PCMUsageFactory {
	private UsageModel usageModel;
	private UsageScenario usageScenario;
	private ScenarioBehaviour scenarioBehaviour;
	
	public PCMUsageFactory(UsageModel usageModel) {
		this.usageModel = usageModel;
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
	
	// Add action to usage
	public void addAction() {
		
	}
	
	// Add confid to usage
	public void addConfi() {
		ConfidentialityVariableCharacterisation characterisation = ConfidentialityFactory.eINSTANCE.createConfidentialityVariableCharacterisation();
		var test = CharacteristicsFactory.eINSTANCE;
	}
	
	public UsageScenario getUsageScenario() {
		AbstractUserAction stopAction = UsagemodelFactory.eINSTANCE.createStop();
		stopAction.setScenarioBehaviour_AbstractUserAction(scenarioBehaviour);
		stopAction.setPredecessor(scenarioBehaviour.getActions_ScenarioBehaviour().get(scenarioBehaviour.getActions_ScenarioBehaviour().size() - 1));
		scenarioBehaviour.getActions_ScenarioBehaviour().get(scenarioBehaviour.getActions_ScenarioBehaviour().size() - 1).setSuccessor(stopAction);
		return this.usageScenario;
	}
}
