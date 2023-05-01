package org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.dataflow;

import java.util.UUID;

import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.Signature;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.BranchAction;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.ProbabilisticBranchTransition;
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
		this.seff.getSteps_Behaviour().add(lastAction);
	}
	
	public static SEFFBuilder builder(BasicComponent component, Signature signature) {
		ResourceDemandingSEFF seff = SeffFactory.eINSTANCE.createResourceDemandingSEFF();
		seff.setId(UUID.randomUUID().toString());
		seff.setBasicComponent_ServiceEffectSpecification(component);
		seff.setDescribedService__SEFF(signature);
		return new SEFFBuilder(seff);
	}
	
	public static SEFFBuilder nested(ResourceDemandingSEFF seff) {
		return new SEFFBuilder(seff);
	}
	
	public SEFFBuilder addVariableAction(String name) {
		SetVariableAction action = SeffFactory.eINSTANCE.createSetVariableAction();
		action.setId(UUID.randomUUID().toString());
		action.setEntityName(name);
		action.setPredecessor_AbstractAction(this.lastAction);
		this.lastAction.setSuccessor_AbstractAction(action);
		this.lastAction = action;
		this.seff.getSteps_Behaviour().add(action);
		return this;
	}
	
	public SEFFCallBuilder addCall(String name) {
		ExternalCallAction call = SeffFactory.eINSTANCE.createExternalCallAction();
		call.setId(UUID.randomUUID().toString());
		call.setEntityName(name);
		call.setPredecessor_AbstractAction(this.lastAction);
		this.lastAction.setSuccessor_AbstractAction(call);
		this.lastAction = call;
		this.seff.getSteps_Behaviour().add(call);
		return SEFFCallBuilder.builder(call, this);
	}
	
	public SEFFBranchBuilder addBranch(String name) {
		BranchAction branch = SeffFactory.eINSTANCE.createBranchAction();
		branch.setId(UUID.randomUUID().toString());
		branch.setEntityName(name);
		branch.setPredecessor_AbstractAction(this.lastAction);
		branch.setResourceDemandingBehaviour_AbstractAction(seff);
		
		ProbabilisticBranchTransition transitionLeft = SeffFactory.eINSTANCE.createProbabilisticBranchTransition();
		transitionLeft.setId(UUID.randomUUID().toString());
		transitionLeft.setEntityName(name + "Transition1");
		transitionLeft.setBranchAction_AbstractBranchTransition(branch);
		transitionLeft.setBranchProbability(0.5);
		branch.getBranches_Branch().add(transitionLeft);
		
		ProbabilisticBranchTransition transitionRight = SeffFactory.eINSTANCE.createProbabilisticBranchTransition();
		transitionRight.setId(UUID.randomUUID().toString());
		transitionRight.setEntityName(name + "Transition2");
		transitionRight.setBranchAction_AbstractBranchTransition(branch);
		transitionRight.setBranchProbability(0.5);
		branch.getBranches_Branch().add(transitionRight);
		
		this.lastAction.setSuccessor_AbstractAction(branch);
		this.lastAction = branch;
		this.seff.getSteps_Behaviour().add(branch);
		return new SEFFBranchBuilder(this, transitionLeft, transitionRight);
	}
	
	public void build() {
		StopAction action = SeffFactory.eINSTANCE.createStopAction();
		action.setId(UUID.randomUUID().toString());
		action.setResourceDemandingBehaviour_AbstractAction(seff);
		action.setPredecessor_AbstractAction(this.lastAction);
		this.lastAction.setSuccessor_AbstractAction(action);
		this.seff.getSteps_Behaviour().add(action);
	}
	
	public ResourceDemandingSEFF getSEFF() {
		return this.seff;
	}
}
