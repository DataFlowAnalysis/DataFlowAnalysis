package org.dataflowanalysis.analysis.pcm.informationflow.core.user;

import java.util.List;

import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

public class IFCallingUserPCMVertex extends AbstractIFCallingUserPCMVertex {

	public IFCallingUserPCMVertex(EntryLevelSystemCall element, List<? extends AbstractPCMVertex<?>> previousElements,
			ResourceProvider resourceProvider) {
		super(element, previousElements, true, resourceProvider);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected AbstractIFCallingUserPCMVertex createIFUserVertex(EntryLevelSystemCall element,
			List<? extends AbstractPCMVertex<?>> previousElements, ResourceProvider resourceProvider) {
		return new IFCallingUserPCMVertex(element, previousElements, resourceProvider);
	}

	@Override
	protected List<DataFlowVariable> modifyIncomingDataFlowVariables(List<DataFlowVariable> incomingVariables) {
		return incomingVariables;
	}

	@Override
	protected List<VariableCharacterisation> extractVariableCharacterisations() {
		return getReferencedElement().getInputParameterUsages_EntryLevelSystemCall().stream()
				.flatMap(it -> it.getVariableCharacterisation_VariableUsage().stream()).toList();
	}

	@Override
	protected void checkConfidentialityVariableCharacterisations(
			List<ConfidentialityVariableCharacterisation> characterisations) {
		checkCallParameter(getReferencedElement().getOperationSignature__EntryLevelSystemCall(), characterisations);
	}

	@Override
	protected List<DataFlowVariable> modifyOutgoingDataFlowVariables(List<DataFlowVariable> outgoingVariables) {
		return outgoingVariables;
	}

}
