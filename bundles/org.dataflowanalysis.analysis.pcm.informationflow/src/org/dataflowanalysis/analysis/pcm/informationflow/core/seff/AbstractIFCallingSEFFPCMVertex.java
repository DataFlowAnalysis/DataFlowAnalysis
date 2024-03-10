package org.dataflowanalysis.analysis.pcm.informationflow.core.seff;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

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

	protected List<VariableCharacterisation> extractStandardVariableCharacterisations() {
		return new ArrayList<VariableCharacterisation>();
	}

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
