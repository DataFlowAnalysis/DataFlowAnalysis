package org.dataflowanalysis.analysis.pcm.informationflow.core.user;

import java.util.List;

import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.CallingUserPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.extraction.IFPCMExtractionStrategy;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

/**
 * An concrete implementation of {@link AbstractIFCallingUserPCMVertex} for
 * calling behavior.
 *
 */
public class IFCallingUserPCMVertex extends AbstractIFCallingUserPCMVertex {

	/**
	 * As for a {@link CallingUserPCMVertex} the vertex has an underlying
	 * {@link EntryLevelSystemCall} SEFF element which influences the behavior
	 * through defined VariableCharacterisations. The vertex can have
	 * {@code previousElements} from which the incoming DataFlowVariables are
	 * received. Furthermore, the vertex contains a {@link ResourceProvider}.
	 * Lastly, the vertex might consider implicit flow and requires an
	 * {@link IFPCMExtractionStrategy} to define how label propagation functions are
	 * extracted.
	 * 
	 * @param element              the underlying SEFF element
	 * @param previousElements     the previous vertices
	 * @param resourceProvider     the ResourceProvider
	 * @param considerImplicitFlow whether to consider implicit flow
	 * @param extractionStrategy   the extraction strategy
	 */
	public IFCallingUserPCMVertex(EntryLevelSystemCall element, List<? extends AbstractPCMVertex<?>> previousElements,
			ResourceProvider resourceProvider, boolean considerImplicitFlow,
			IFPCMExtractionStrategy extractionStrategy) {
		super(element, previousElements, true, resourceProvider, considerImplicitFlow, extractionStrategy);
	}

	@Override
	protected AbstractIFCallingUserPCMVertex createIFUserVertex(EntryLevelSystemCall element,
			List<? extends AbstractPCMVertex<?>> previousElements, ResourceProvider resourceProvider,
			boolean considerImplicitFlow, IFPCMExtractionStrategy extractionStrategy) {
		return new IFCallingUserPCMVertex(element, previousElements, resourceProvider, considerImplicitFlow,
				extractionStrategy);
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
