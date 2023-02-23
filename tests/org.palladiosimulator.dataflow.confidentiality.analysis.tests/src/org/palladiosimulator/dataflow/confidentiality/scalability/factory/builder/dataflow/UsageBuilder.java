package org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.dataflow;

import java.util.UUID;

import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.CharacteristicsFactory;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.EnumCharacteristic;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.node.NodeCharacteristicBuilder;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;
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
	private NodeCharacteristicBuilder nodeCharacteristicBuilder;
	
	private UsageBuilder(UsageModel usage, NodeCharacteristicBuilder nodeCharacteristicBuilder) {
		this.usage = usage;
		this.nodeCharacteristicBuilder = nodeCharacteristicBuilder;
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
	
	public static UsageBuilder builder(UsageModel usage, NodeCharacteristicBuilder nodeCharacteristicBuilder) {
		return new UsageBuilder(usage, nodeCharacteristicBuilder);
	}
	
	public UsageBuilder setName(String name) {
		this.scenario.setEntityName(name.concat("Scenario"));
		this.behaviour.setEntityName(name.concat("Behaviour"));
		return this;
	}
	
	public UsageBuilder addCharacteristic(EnumCharacteristicType characteristicType, String characteristicValue) {
		Literal literal = characteristicType.getType().getLiterals().stream()
					.filter(it -> it.getName().equalsIgnoreCase(characteristicValue))
					.findAny().orElseThrow(() -> new IllegalArgumentException("Unknown characteristic value"));
		EnumCharacteristic characteristic = CharacteristicsFactory.eINSTANCE.createEnumCharacteristic();
		characteristic.setType(characteristicType);
		characteristic.getValues().add(literal);
		nodeCharacteristicBuilder.addCharacteristic(scenario, characteristic);
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
		return UsageCallBuilder.builder(call, this);
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
