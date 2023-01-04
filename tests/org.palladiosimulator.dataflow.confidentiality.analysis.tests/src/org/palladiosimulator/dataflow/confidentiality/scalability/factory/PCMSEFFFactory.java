package org.palladiosimulator.dataflow.confidentiality.scalability.factory;

import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.SeffFactory;
import org.palladiosimulator.pcm.seff.SetVariableAction;
import org.palladiosimulator.pcm.seff.StopAction;

public class PCMSEFFFactory {
	private Repository repository;
	private ResourceDemandingSEFF seff;
	
	public PCMSEFFFactory(Repository repository) {
		this.repository = repository;
		this.seff = SeffFactory.eINSTANCE.createResourceDemandingSEFF();
	}
	
	public void setComponent(BasicComponent component) {
		seff.setBasicComponent_ServiceEffectSpecification(component);
	}

	public void setSigniture(OperationSignature signiture) {
		seff.setDescribedService__SEFF(signiture);
	}
	
	public void addAction(AbstractAction action) {
		seff.getSteps_Behaviour().add(action);
	}
	
	public AbstractAction addSetVariableAction(String name) {
		SetVariableAction setVariableAction = SeffFactory.eINSTANCE.createSetVariableAction();
		setVariableAction.setEntityName(name);
		setVariableAction.setPredecessor_AbstractAction(seff.getSteps_Behaviour().get(seff.getSteps_Behaviour().size() - 1));
		seff.getSteps_Behaviour().get(seff.getSteps_Behaviour().size() - 1).setSuccessor_AbstractAction(setVariableAction);
		return setVariableAction;
	}
	
	public AbstractAction addCallAction(String name, OperationRequiredRole operationRequiredRole) {
		ExternalCallAction callAction = SeffFactory.eINSTANCE.createExternalCallAction();
		callAction.setEntityName(name);
		callAction.setRole_ExternalService(operationRequiredRole);
		
		callAction.setPredecessor_AbstractAction(seff.getSteps_Behaviour().get(seff.getSteps_Behaviour().size() - 1));
		seff.getSteps_Behaviour().get(seff.getSteps_Behaviour().size() - 1).setSuccessor_AbstractAction(callAction);
		return callAction;
	}
	
	public ResourceDemandingSEFF getSEFF() {
		StopAction stopAction = SeffFactory.eINSTANCE.createStopAction();
		seff.getSteps_Behaviour().get(seff.getSteps_Behaviour().size() - 1).setSuccessor_AbstractAction(stopAction);
		stopAction.setPredecessor_AbstractAction(seff.getSteps_Behaviour().get(seff.getSteps_Behaviour().size() - 1));
		return seff;
	}
}
