package org.dataflowanalysis.analysis.pcm.informationflow.core.seff;

import java.util.Deque;
import java.util.List;

import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.seff.SEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFPCMExtractionStrategy;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.ExternalCallAction;

/**
 * An concrete implementation of {@link AbstractIFCallingSEFFPCMVertex} for
 * returning behavior.
 *
 */
public class IFReturningSEFFPCMVertex extends AbstractIFCallingSEFFPCMVertex {

	/**
	 * As for a {@link SEFFPCMVertex} the vertex has an underlying SEFF element of
	 * the type ExternalCallAction which influences the behavior through defined
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
	public IFReturningSEFFPCMVertex(ExternalCallAction element, List<? extends AbstractPCMVertex<?>> previousElements,
			Deque<AssemblyContext> context, List<Parameter> parameter, ResourceProvider resourceProvider,
			boolean considerImplicitFlow, IFPCMExtractionStrategy extractionStrategy) {
		super(element, previousElements, context, parameter, false, resourceProvider, considerImplicitFlow,
				extractionStrategy);
	}

	@Override
	protected AbstractIFCallingSEFFPCMVertex createIFSEFFVertex(ExternalCallAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider, boolean considerImplicitFlow,
			IFPCMExtractionStrategy extractionStrategy) {
		return new IFReturningSEFFPCMVertex(element, previousElements, context, parameter, resourceProvider,
				considerImplicitFlow, extractionStrategy);
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
	protected void checkConfidentialityVariableCharacterisations(
			List<ConfidentialityVariableCharacterisation> characterisations) {
		return;
	}

	@Override
	protected List<DataFlowVariable> modifyOutgoingDataFlowVariables(List<DataFlowVariable> outgoingVariables) {
		return removeReturnParameter(outgoingVariables);
	}

}
