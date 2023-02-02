package org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.dataflow;

import java.util.UUID;

import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.Signature;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.SeffFactory;
import org.palladiosimulator.pcm.seff.SetVariableAction;
import org.palladiosimulator.pcm.seff.StopAction;

public class SEFFBuilder {
	private ResourceDemandingSEFF seff;
	private AbstractAction lastAction;
	
	private SEFFBuilder(ResourceDemandingSEFF seff) {
		this.seff = seff;
		this.lastAction = SeffFactory.eINSTANCE.createStartAction();
		this.lastAction.setId(UUID.randomUUID().toString());
		this.lastAction.setResourceDemandingBehaviour_AbstractAction(seff);
	}
	
	public static SEFFBuilder builder(BasicComponent component, Signature signature) {
		ResourceDemandingSEFF seff = SeffFactory.eINSTANCE.createResourceDemandingSEFF();
		seff.setId(UUID.randomUUID().toString());
		seff.setBasicComponent_ServiceEffectSpecification(component);
		seff.setDescribedService__SEFF(signature);
		return new SEFFBuilder(seff);
	}
	
	public SEFFBuilder addVariableAction(String name) {
		SetVariableAction action = SeffFactory.eINSTANCE.createSetVariableAction();
		action.setId(UUID.randomUUID().toString());
		action.setEntityName(name);
		action.setPredecessor_AbstractAction(this.lastAction);
		this.lastAction.setSuccessor_AbstractAction(action);
		this.lastAction = action;
		return this;
	}
	
	public SEFFCallBuilder addCall(String name) {
		ExternalCallAction call = SeffFactory.eINSTANCE.createExternalCallAction();
		call.setId(UUID.randomUUID().toString());
		call.setEntityName(name);
		call.setPredecessor_AbstractAction(this.lastAction);
		this.lastAction.setSuccessor_AbstractAction(call);
		this.lastAction = call;
		return SEFFCallBuilder.builder(call, this);
	}
	
	public void build() {
		StopAction action = SeffFactory.eINSTANCE.createStopAction();
		action.setId(UUID.randomUUID().toString());
		action.setResourceDemandingBehaviour_AbstractAction(seff);
		action.setPredecessor_AbstractAction(this.lastAction);
		this.lastAction.setSuccessor_AbstractAction(action);
	}
}
