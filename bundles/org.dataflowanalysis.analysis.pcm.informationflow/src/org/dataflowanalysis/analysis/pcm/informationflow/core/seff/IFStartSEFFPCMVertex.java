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
import org.palladiosimulator.pcm.seff.GuardedBranchTransition;
import org.palladiosimulator.pcm.seff.StartAction;

import de.uka.ipd.sdq.stoex.Expression;

//TODO Note that Branching Behavior is handled in Start and Stop Vertices
public class IFStartSEFFPCMVertex extends AbstractIFSEFFPCMVertex<StartAction> {

	public IFStartSEFFPCMVertex(StartAction element, List<? extends AbstractPCMVertex<?>> previousElements,
			Deque<AssemblyContext> context, List<Parameter> parameter, ResourceProvider resourceProvider) {
		super(element, previousElements, context, parameter, resourceProvider);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected AbstractIFSEFFPCMVertex<StartAction> createIFSEFFVertex(StartAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider) {
		return new IFStartSEFFPCMVertex(element, previousElements, context, parameter, resourceProvider);
	}

	@Override
	protected List<DataFlowVariable> modifyIncomingDataFlowVariables(List<DataFlowVariable> incomingDataFlowVariables) {
		if (!isConsideringImplicitFlow()) {
			return filterCallParameters(incomingDataFlowVariables);
		}
		// incoming variables are the outgoing variables of the branch if existent
		List<DataFlowVariable> incomingVariables = incomingDataFlowVariables;
		if (isElementInGuardedBranchTransitionSEFF()) {
			incomingVariables = evaluateOutgoingDataFlowVariablesOfBranchTransition(incomingDataFlowVariables);
		}

		// Avoid parameter filter for security context layers in StartVertex
		List<DataFlowVariable> securityContextLayers = IFSecurityContextUtils
				.getAllSecurityContextLayers(incomingVariables);
		List<DataFlowVariable> modifiedIncoming = filterCallParameters(incomingVariables);

		modifiedIncoming = new ArrayList<>(modifiedIncoming);
		for (DataFlowVariable securityContextLayer : securityContextLayers) {
			if (!modifiedIncoming.contains(securityContextLayer)) {
				modifiedIncoming.add(securityContextLayer);
			}
		}
		return modifiedIncoming;
	}

	@Override
	protected List<VariableCharacterisation> extractVariableCharacterisations() {
		return new ArrayList<VariableCharacterisation>();
	}

	@Override
	protected void checkConfidentialityVariableCharacterisations(
			List<ConfidentialityVariableCharacterisation> characterisations) {
		return;
	}

	@Override
	protected List<DataFlowVariable> modifyOutgoingDataFlowVariables(List<DataFlowVariable> outgoingVariables) {
		return outgoingVariables;
	}

	/*
	 * Evaluation of GuardedBranchTransition
	 */

	private List<DataFlowVariable> evaluateOutgoingDataFlowVariablesOfBranchTransition(
			List<DataFlowVariable> incomingVariables) {
		var supposedGuardedBranchTransition = getReferencedElement().eContainer().eContainer();
		if (supposedGuardedBranchTransition instanceof GuardedBranchTransition bT) {

			List<ConfidentialityVariableCharacterisation> confChars = extractCvcsOfGuardedBranchTransition(bT);
			List<DataFlowVariable> outgoingVariables = getDataFlowVariables(getVertexCharacteristics(), confChars,
					incomingVariables);
			return outgoingVariables;
		}
		return incomingVariables;
	}

	private List<ConfidentialityVariableCharacterisation> extractCvcsOfGuardedBranchTransition(
			GuardedBranchTransition bT) {
		var incomingVariables = getIncomingDataFlowVariables();

		Expression condition = bT.getBranchCondition_GuardedBranchTransition().getExpression();
		var oldSecurityContext = IFSecurityContextUtils.getActiveSecurityContext(incomingVariables);
		String nextSecurityContextName = IFSecurityContextUtils.getNameNextSecurityContextLayer(incomingVariables);

		return getExtractionStrategy().calculateConfidentialityVariableCharacterisationForExpression(
				nextSecurityContextName, condition, oldSecurityContext);

	}

}
