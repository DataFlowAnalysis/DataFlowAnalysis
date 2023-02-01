package org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.dataflow;

import java.util.UUID;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.Stop;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;

public class UsageBuilder {
	private UsageModel usage;
	private UsageScenario scenario;
	private ScenarioBehaviour behaviour;
	private AbstractUserAction lastAction;
	
	private UsageBuilder(UsageModel usage) {
		this.usage = usage;
		this.scenario = UsagemodelFactory.eINSTANCE.createUsageScenario();
		this.scenario.setId(UUID.randomUUID().toString());
		this.behaviour = UsagemodelFactory.eINSTANCE.createScenarioBehaviour();
		this.behaviour.setId(UUID.randomUUID().toString());
		this.scenario.setScenarioBehaviour_UsageScenario(behaviour);
		this.scenario.setUsageModel_UsageScenario(usage);
		this.behaviour.setUsageScenario_SenarioBehaviour(scenario);
		this.usage.getUsageScenario_UsageModel().add(scenario);
		this.lastAction = UsagemodelFactory.eINSTANCE.createStart();
		this.lastAction.setId(UUID.randomUUID().toString());
		this.lastAction.setScenarioBehaviour_AbstractUserAction(behaviour);
		this.behaviour.getActions_ScenarioBehaviour().add(lastAction);
	}
	
	public static UsageBuilder builder(UsageModel usage) {
		return new UsageBuilder(usage);
	}
	
	public UsageBuilder setName(String name) {
		this.scenario.setEntityName(name.concat("Scenario"));
		this.behaviour.setEntityName(name.concat("Behaviour"));
		return this;
	}
	
	public UsageCallBuilder addCall(String name) {
		EntryLevelSystemCall call = UsagemodelFactory.eINSTANCE.createEntryLevelSystemCall();
		call.setId(UUID.randomUUID().toString());
		call.setEntityName(name);
		call.setScenarioBehaviour_AbstractUserAction(behaviour);
		call.setPredecessor(lastAction);
		this.lastAction.setSuccessor(call);
		this.lastAction = call;
		return UsageCallBuilder.builder(call);
	}
	
	public UsageBuilder addBranch(String name) {
		// TODO: Branch Builder
		return this;
	}
	
	public void build() {
		Stop action = UsagemodelFactory.eINSTANCE.createStop();
		action.setId(UUID.randomUUID().toString());
		action.setScenarioBehaviour_AbstractUserAction(behaviour);
		action.setPredecessor(this.lastAction);
		this.lastAction.setSuccessor(action);
	}
}
