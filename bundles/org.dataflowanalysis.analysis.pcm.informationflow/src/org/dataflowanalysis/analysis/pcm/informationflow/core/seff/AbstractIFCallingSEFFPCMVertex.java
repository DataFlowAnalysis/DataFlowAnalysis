package org.dataflowanalysis.analysis.pcm.informationflow.core.seff;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.seff.CallingSEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFConfigurablePCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFPCMExtractionStrategy;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFSecurityContextUtils;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.ExternalCallAction;

public abstract class AbstractIFCallingSEFFPCMVertex extends CallingSEFFPCMVertex implements IFConfigurablePCMVertex {

	private boolean considerImplicitFlow;
	private IFPCMExtractionStrategy extractionStrategy;

	public AbstractIFCallingSEFFPCMVertex(ExternalCallAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, boolean isCalling, ResourceProvider resourceProvider,
			boolean considerImplicitFlow, IFPCMExtractionStrategy extractionStrategy) {
		super(element, previousElements, context, parameter, isCalling, resourceProvider);
		this.considerImplicitFlow = considerImplicitFlow;
		this.extractionStrategy = extractionStrategy;
	}

	public void setConsiderImplicitFlow(boolean consider) {
		this.considerImplicitFlow = consider;
	}

	public void setExtractionStrategy(IFPCMExtractionStrategy extractionStrategy) {
		this.extractionStrategy = extractionStrategy;
	}

	public boolean isConsideringImplicitFlow() {
		return considerImplicitFlow;
	}

	public IFPCMExtractionStrategy getExtractionStrategy() {
		return extractionStrategy;
	}

	/*
	 * Same as in AbstractIFSEFFPCMVertex:
	 */
	// TODO Less redundant way?

	@Override
	public AbstractPCMVertex<?> deepCopy(Map<AbstractPCMVertex<?>, AbstractPCMVertex<?>> isomorphism) {
		if (isomorphism.get(this) != null) {
			return isomorphism.get(this);
		}
		AbstractIFCallingSEFFPCMVertex copy = createIFSEFFVertex(getReferencedElement(), List.copyOf(previousElements),
				new ArrayDeque<>(getContext()), List.copyOf(getParameter()), resourceProvider, considerImplicitFlow,
				extractionStrategy);
		return super.updateCopy(copy, isomorphism);
	}

	protected abstract AbstractIFCallingSEFFPCMVertex createIFSEFFVertex(ExternalCallAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider, boolean considerImplicitFlow,
			IFPCMExtractionStrategy extractionStrategy);

	@Override
	public void evaluateDataFlow() {
		var incomingDataFlowVariables = getIncomingDataFlowVariables();
		incomingDataFlowVariables = modifyIncomingDataFlowVariables(incomingDataFlowVariables);

		// Security context should only have been added when considering implicit flow
		var securityContext = IFSecurityContextUtils.getActiveSecurityContext(incomingDataFlowVariables);

		var allVariableCharacterisations = extractVariableCharacterisations();
		var effectiveVariableCharacterisations = getExtractionStrategy()
				.calculateEffectiveConfidentialityVariableCharacterisation(allVariableCharacterisations,
						securityContext);
		checkConfidentialityVariableCharacterisations(effectiveVariableCharacterisations);

		var outgoingDataFlowVariables = getDataFlowVariables(getVertexCharacteristics(),
				effectiveVariableCharacterisations, incomingDataFlowVariables);
		outgoingDataFlowVariables = modifyOutgoingDataFlowVariables(outgoingDataFlowVariables);

		setPropagationResult(incomingDataFlowVariables, outgoingDataFlowVariables, getVertexCharacteristics());
	}

	protected abstract List<DataFlowVariable> modifyIncomingDataFlowVariables(List<DataFlowVariable> incomingVariables);

	protected abstract List<VariableCharacterisation> extractVariableCharacterisations();

	protected abstract void checkConfidentialityVariableCharacterisations(
			List<ConfidentialityVariableCharacterisation> characterisations);

	protected abstract List<DataFlowVariable> modifyOutgoingDataFlowVariables(List<DataFlowVariable> outgoingVariables);

}
