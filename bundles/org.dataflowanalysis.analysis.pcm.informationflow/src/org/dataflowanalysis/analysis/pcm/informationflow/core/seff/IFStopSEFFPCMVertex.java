package org.dataflowanalysis.analysis.pcm.informationflow.core.seff;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFSecurityContextUtils;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.StopAction;

public class IFStopSEFFPCMVertex extends AbstractIFSEFFPCMVertex<StopAction> {

	public IFStopSEFFPCMVertex(StopAction element, List<? extends AbstractPCMVertex<?>> previousElements,
			Deque<AssemblyContext> context, List<Parameter> parameter, ResourceProvider resourceProvider) {
		super(element, previousElements, context, parameter, resourceProvider);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected AbstractIFSEFFPCMVertex<StopAction> createIFSEFFVertex(StopAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider) {
		return new IFStopSEFFPCMVertex(element, previousElements, context, parameter, resourceProvider);
	}

	@Override
	protected List<DataFlowVariable> modifyIncomingDataFlowVariables(List<DataFlowVariable> incomingVariables) {
		return incomingVariables;
	}

	@Override
	protected List<VariableCharacterisation> extractVariableCharacterisations() {
		return new ArrayList<>();
	}

	@Override
	protected void checkConfidentialityVariableCharacterisations(
			List<ConfidentialityVariableCharacterisation> characterisations) {
		return;
	}

	@Override
	protected List<DataFlowVariable> modifyOutgoingDataFlowVariables(List<DataFlowVariable> outgoingDataFlowVariables) {
		if (!isConsideringImplicitFlow()) {
			return filterReturnParameter(outgoingDataFlowVariables);
		}
		// Avoid parameter filter for security context layers in StopVertex
		List<DataFlowVariable> securityContextLayers = IFSecurityContextUtils
				.getAllSecurityContextLayers(outgoingDataFlowVariables);
		List<DataFlowVariable> modifiedOutgoing = filterReturnParameter(outgoingDataFlowVariables);

		modifiedOutgoing = new ArrayList<>(modifiedOutgoing);
		for (DataFlowVariable securityContextLayer : securityContextLayers) {
			if (!modifiedOutgoing.contains(securityContextLayer)) {
				modifiedOutgoing.add(securityContextLayer);
			}
		}

		// behavior of branch in case of branch stop
		if (isElementInGuardedBranchTransitionSEFF()) {
			outgoingDataFlowVariables = modifyOutgoingDataFlowVariablesOfBranch(outgoingDataFlowVariables);
		}
		return outgoingDataFlowVariables;
	}

	private List<DataFlowVariable> modifyOutgoingDataFlowVariablesOfBranch(List<DataFlowVariable> incomingVariables) {
		List<DataFlowVariable> outgoingVariables = IFSecurityContextUtils.removeSecurityContextLayer(incomingVariables);
		return outgoingVariables;
	}

}
