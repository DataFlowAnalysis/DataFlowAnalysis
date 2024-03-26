package org.dataflowanalysis.analysis.pcm.informationflow.core.seff;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.seff.CallingSEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFConfigurablePCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFPCMExtractionStrategy;
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
			List<Parameter> parameter, boolean isCalling, ResourceProvider resourceProvider) {
		super(element, previousElements, context, parameter, isCalling, resourceProvider);
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

	/*
	 * Same as in AbstractIFSEFFPCMVertex:
	 */
	// TODO Less redundant way?

	@Override
	public AbstractPCMVertex<?> deepCopy(Map<AbstractPCMVertex<?>, AbstractPCMVertex<?>> isomorphism) {
		if (isomorphism.get(this) != null) {
			return isomorphism.get(this);
		}
		AbstractIFCallingSEFFPCMVertex copy = createIFSEFFVertex(getReferencedElement(), List.of(),
				new ArrayDeque<>(context), new ArrayList<>(getParameter()), resourceProvider);
		copy.setConsiderImplicitFlow(isConsideringImplicitFlow());
		copy.setExtractionStrategy(getExtractionStrategy());
		return super.updateCopy(copy, isomorphism);
	}

	protected abstract AbstractIFCallingSEFFPCMVertex createIFSEFFVertex(ExternalCallAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider);

	protected List<VariableCharacterisation> extractVariableCharacterisations() {
		return new ArrayList<VariableCharacterisation>();
	}

	@Override
	protected List<DataFlowVariable> getDataFlowVariables(List<CharacteristicValue> vertexCharacteristics,
			List<ConfidentialityVariableCharacterisation> variableCharacterisations,
			List<DataFlowVariable> oldDataFlowVariables) {
		List<VariableCharacterisation> allVariableCharacterisations = extractVariableCharacterisations();
		List<ConfidentialityVariableCharacterisation> effectiveVariableCharacterisations = extractionStrategy
				.calculateEffectiveConfidentialityVariableCharacterisation(allVariableCharacterisations);
		return super.getDataFlowVariables(vertexCharacteristics, effectiveVariableCharacterisations,
				oldDataFlowVariables);

	}

}
