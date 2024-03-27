package org.dataflowanalysis.analysis.pcm.informationflow.core.user;

import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.CallingUserPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFConfigurablePCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFPCMExtractionStrategy;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFSecurityContextUtils;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

public abstract class AbstractIFCallingUserPCMVertex extends CallingUserPCMVertex implements IFConfigurablePCMVertex {

	private boolean considerImplicitFlow;
	private IFPCMExtractionStrategy extractionStrategy;

	public AbstractIFCallingUserPCMVertex(EntryLevelSystemCall element,
			List<? extends AbstractPCMVertex<?>> previousElements, boolean isCalling,
			ResourceProvider resourceProvider) {
		super(element, previousElements, isCalling, resourceProvider);
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
		AbstractIFCallingUserPCMVertex copy = createIFUserVertex(getReferencedElement(), List.of(), resourceProvider);
		copy.setConsiderImplicitFlow(isConsideringImplicitFlow());
		copy.setExtractionStrategy(getExtractionStrategy());
		return super.updateCopy(copy, isomorphism);
	}

	protected abstract AbstractIFCallingUserPCMVertex createIFUserVertex(EntryLevelSystemCall element,
			List<? extends AbstractPCMVertex<?>> previousElements, ResourceProvider resourceProvider);

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
