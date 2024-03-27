package org.dataflowanalysis.analysis.pcm.informationflow.core.seff;

import java.util.Deque;
import java.util.List;

import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.ExternalCallAction;

public class IFReturningSEFFPCMVertex extends AbstractIFCallingSEFFPCMVertex {

	public IFReturningSEFFPCMVertex(ExternalCallAction element, List<? extends AbstractPCMVertex<?>> previousElements,
			Deque<AssemblyContext> context, List<Parameter> parameter, ResourceProvider resourceProvider) {
		super(element, previousElements, context, parameter, false, resourceProvider);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected AbstractIFCallingSEFFPCMVertex createIFSEFFVertex(ExternalCallAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider) {
		return new IFReturningSEFFPCMVertex(element, previousElements, context, parameter, resourceProvider);
	}
	

	@Override
	protected List<DataFlowVariable> modifyIncomingDataFlowVariables(List<DataFlowVariable> incomingVariables) {
		return incomingVariables;
	}
	
	@Override
	protected List<VariableCharacterisation> extractVariableCharacterisations() {
		ExternalCallAction element = getReferencedElement();
		return element.getReturnVariableUsage__CallReturnAction().stream()
				.flatMap(it -> it.getVariableCharacterisation_VariableUsage().stream()).toList();
	}

	@Override
	protected void checkConfidentialityVariableCharacterisations(List<ConfidentialityVariableCharacterisation> characterisations) {
		return;
	}

	@Override
	protected List<DataFlowVariable> modifyOutgoingDataFlowVariables(List<DataFlowVariable> outgoingVariables) {
		return removeReturnParameter(outgoingVariables);
	}

}
