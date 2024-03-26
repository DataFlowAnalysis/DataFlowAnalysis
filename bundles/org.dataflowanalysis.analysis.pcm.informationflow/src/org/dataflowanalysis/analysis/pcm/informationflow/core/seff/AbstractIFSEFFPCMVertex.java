package org.dataflowanalysis.analysis.pcm.informationflow.core.seff;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.seff.SEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFConfigurablePCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFPCMExtractionStrategy;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.AbstractAction;

public abstract class AbstractIFSEFFPCMVertex<T extends AbstractAction> extends SEFFPCMVertex<T>
		implements IFConfigurablePCMVertex {

	private boolean considerImplicitFlow;
	private IFPCMExtractionStrategy extractionStrategy;

	public AbstractIFSEFFPCMVertex(T element, List<? extends AbstractPCMVertex<?>> previousElements,
			Deque<AssemblyContext> context, List<Parameter> parameter, ResourceProvider resourceProvider) {
		super(element, previousElements, context, parameter, resourceProvider);
		// TODO Auto-generated constructor stub
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

	@Override
	public AbstractPCMVertex<?> deepCopy(Map<AbstractPCMVertex<?>, AbstractPCMVertex<?>> isomorphism) {
		if (isomorphism.get(this) != null) {
			return isomorphism.get(this);
		}
		AbstractIFSEFFPCMVertex<T> copy = createIFSEFFVertex(getReferencedElement(), List.of(),
				new ArrayDeque<>(context), new ArrayList<>(getParameter()), resourceProvider);
		copy.setConsiderImplicitFlow(isConsideringImplicitFlow());
		copy.setExtractionStrategy(getExtractionStrategy());
		return super.updateCopy(copy, isomorphism);
	}

	protected abstract AbstractIFSEFFPCMVertex<T> createIFSEFFVertex(T element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider);

	/*
	 * Assumed sequence of evaluateDataFlow()
	 */

//	public void evaluateDataFlow() {
//		List<DataFlowVariable> incoming = getIncomingDataFlowVariables();
//		modifyIncomingDataFlowVariables(incoming);
//
//		List<VariableCharacterisation> variableCharacterisations = extractStandardVariableCharacterisations();
//		List<ConfidentialityVariableCharacterisation> propagationCharacterisations = extractionStrategy
//				.calculateEffectiveConfidentialityVariableCharacterisation(variableCharacterisations);
//
//		List<DataFlowVariable> outgoing = getDataFlowVariables(getVertexCharacteristics(), propagationCharacterisations,
//				incoming);
//		modifyOutgoingDataFlowVariables(outgoing);
//
//		setPropagationResult(incoming, outgoing, getVertexCharacteristics());
//	}

//	protected void modifyIncomingDataFlowVariables(List<DataFlowVariable> incoming) {
//
//	}

	protected List<VariableCharacterisation> extractStandardVariableCharacterisations() {
		return new ArrayList<VariableCharacterisation>();
	}

//	protected void modifyOutgoingDataFlowVariables(List<DataFlowVariable> outgoing) {
//
//	}

	/*
	 * Quick Entry Points TODO Change the implementation of the vertices in PCM to
	 * create better extension points
	 */

	@Override
	protected List<DataFlowVariable> getDataFlowVariables(List<CharacteristicValue> vertexCharacteristics,
			List<ConfidentialityVariableCharacterisation> variableCharacterisations,
			List<DataFlowVariable> oldDataFlowVariables) {
		List<VariableCharacterisation> allVariableCharacterisations = extractStandardVariableCharacterisations();
		List<ConfidentialityVariableCharacterisation> effectiveVariableCharacterisations = extractionStrategy
				.calculateEffectiveConfidentialityVariableCharacterisation(allVariableCharacterisations);
		return super.getDataFlowVariables(vertexCharacteristics, effectiveVariableCharacterisations,
				oldDataFlowVariables);

	}

}
