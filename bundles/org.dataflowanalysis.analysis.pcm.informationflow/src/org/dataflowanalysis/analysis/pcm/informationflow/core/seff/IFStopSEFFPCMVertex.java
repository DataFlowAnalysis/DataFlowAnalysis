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
import org.palladiosimulator.pcm.seff.StopAction;

/**
 * A concrete implementation of {@link AbstractIFSEFFPCMVertex} with the
 * underlying SEFF element type {@link StopAction}.
 * 
 * If the {@code StopAction} is directly in the SEFF of a
 * {@link GuardedBranchTransition} this vertex also handles the returning
 * evaluation of the logically following {@code GuardedBranchTransition}. Note,
 * this is only relevant in case of handling implicit flow.
 *
 */
public class IFStopSEFFPCMVertex extends AbstractIFSEFFPCMVertex<StopAction> {

	/**
	 * As for a {@link SEFFPCMVertex} the vertex has an underlying StopAction SEFF
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
	public IFStopSEFFPCMVertex(StopAction element, List<? extends AbstractPCMVertex<?>> previousElements,
			Deque<AssemblyContext> context, List<Parameter> parameter, ResourceProvider resourceProvider,
			boolean considerImplicitFlow, IFPCMExtractionStrategy extractionStrategy) {
		super(element, previousElements, context, parameter, resourceProvider, considerImplicitFlow,
				extractionStrategy);
	}

	@Override
	protected AbstractIFSEFFPCMVertex<StopAction> createIFSEFFVertex(StopAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider, boolean considerImplicitFlow,
			IFPCMExtractionStrategy extractionStrategy) {
		return new IFStopSEFFPCMVertex(element, previousElements, context, parameter, resourceProvider,
				considerImplicitFlow, extractionStrategy);
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
