package org.dataflowanalysis.analysis.pcm.informationflow.core.seff;

import java.util.Deque;
import java.util.List;

import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.seff.SEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.extraction.IFPCMExtractionStrategy;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.SetVariableAction;

/**
 * A concrete implementation of {@link AbstractIFSEFFPCMVertex} with the
 * underlying SEFF element type {@link SetVariableAction}.
 *
 */
public class IFSetVariableSEFFPCMVertex extends AbstractIFSEFFPCMVertex<SetVariableAction> {

	/**
	 * As for a {@link SEFFPCMVertex} the vertex has an underlying SetVariableAction
	 * SEFF element which influences the behavior through defined
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
	public IFSetVariableSEFFPCMVertex(SetVariableAction element, List<? extends AbstractPCMVertex<?>> previousElements,
			Deque<AssemblyContext> context, List<Parameter> parameter, ResourceProvider resourceProvider,
			boolean considerImplicitFlow, IFPCMExtractionStrategy extractionStrategy) {
		super(element, previousElements, context, parameter, resourceProvider, considerImplicitFlow,
				extractionStrategy);
	}

	@Override
	protected AbstractIFSEFFPCMVertex<SetVariableAction> createIFSEFFVertex(SetVariableAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider, boolean considerImplicitFlow,
			IFPCMExtractionStrategy extractionStrategy) {
		return new IFSetVariableSEFFPCMVertex(element, previousElements, context, parameter, resourceProvider,
				considerImplicitFlow, extractionStrategy);
	}

	@Override
	protected List<DataFlowVariable> modifyIncomingDataFlowVariables(List<DataFlowVariable> incomingVariables) {
		return incomingVariables;
	}

	@Override
	protected List<VariableCharacterisation> extractVariableCharacterisations() {
		SetVariableAction element = getReferencedElement();
		return element.getLocalVariableUsages_SetVariableAction().stream()
				.flatMap(it -> it.getVariableCharacterisation_VariableUsage().stream()).toList();
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

}