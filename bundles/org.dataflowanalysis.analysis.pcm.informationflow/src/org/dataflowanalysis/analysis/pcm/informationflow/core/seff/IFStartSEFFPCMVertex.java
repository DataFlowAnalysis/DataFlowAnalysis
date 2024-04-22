package org.dataflowanalysis.analysis.pcm.informationflow.core.seff;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.seff.SEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.extraction.IFPCMExtractionStrategy;
import org.dataflowanalysis.analysis.pcm.informationflow.core.utils.IFSecurityContextUtils;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.GuardedBranchTransition;
import org.palladiosimulator.pcm.seff.StartAction;

import de.uka.ipd.sdq.stoex.Expression;

/**
 * A concrete implementation of {@link AbstractIFSEFFPCMVertex} with the
 * underlying SEFF element type {@link StartAction}.
 * 
 * If the {@code StartAction} is directly in the SEFF of a
 * {@link GuardedBranchTransition} this vertex also handles the calling
 * evaluation of the logically previous {@code GuardedBranchTransition}. Note,
 * this is only relevant in case of handling implicit flow.
 *
 */
public class IFStartSEFFPCMVertex extends AbstractIFSEFFPCMVertex<StartAction> {

	/**
	 * As for a {@link SEFFPCMVertex} the vertex has an underlying StartAction SEFF
	 * element which influences the behavior through defined
	 * VariableCharacterisations. The vertex can have {@code previousElements} from
	 * which the incoming DataFlowVariables are received. Furthermore, the vertex
	 * contains an {@link AssemblyContext}, passed {@link Parameter}s as well as a
	 * {@link ResourceProvider}. Lastly, the vertex might consider implicit flow and
	 * requires an {@link IFPCMExtractionStrategy} to define how label propagation
	 * functions are extracted.
	 * 
	 * @param element              the underlying SEFF element
	 * @param previousElements     the previous vertices
	 * @param context              the AssemblyContext
	 * @param parameter            the passed Parameters
	 * @param resourceProvider     the ResourceProvider
	 * @param considerImplicitFlow whether to consider implicit flow
	 * @param extractionStrategy   the extraction strategy
	 */
	public IFStartSEFFPCMVertex(StartAction element, List<? extends AbstractPCMVertex<?>> previousElements,
			Deque<AssemblyContext> context, List<Parameter> parameter, ResourceProvider resourceProvider,
			boolean considerImplicitFlow, IFPCMExtractionStrategy extractionStrategy) {
		super(element, previousElements, context, parameter, resourceProvider, considerImplicitFlow,
				extractionStrategy);
	}

	@Override
	protected AbstractIFSEFFPCMVertex<StartAction> createIFSEFFVertex(StartAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider, boolean considerImplicitFlow,
			IFPCMExtractionStrategy extractionStrategy) {
		return new IFStartSEFFPCMVertex(element, previousElements, context, parameter, resourceProvider,
				considerImplicitFlow, extractionStrategy);
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
		if (supposedGuardedBranchTransition instanceof GuardedBranchTransition branchTransition) {

			List<ConfidentialityVariableCharacterisation> confChars = extractCvcsOfGuardedBranchTransition(branchTransition);
			List<DataFlowVariable> outgoingVariables = getDataFlowVariables(getVertexCharacteristics(), confChars,
					incomingVariables);
			return outgoingVariables;
		}
		return incomingVariables;
	}

	private List<ConfidentialityVariableCharacterisation> extractCvcsOfGuardedBranchTransition(
			GuardedBranchTransition branchTransition) {
		var incomingVariables = getIncomingDataFlowVariables();

		Expression condition = branchTransition.getBranchCondition_GuardedBranchTransition().getExpression();
		var oldSecurityContext = IFSecurityContextUtils.getActiveSecurityContext(incomingVariables);
		String nextSecurityContextName = IFSecurityContextUtils.getNameNextSecurityContextLayer(incomingVariables);

		return getExtractionStrategy().calculateConfidentialityVariableCharacterisationForExpression(
				nextSecurityContextName, condition, oldSecurityContext);

	}

}
