package org.dataflowanalysis.analysis.pcm.informationflow.core.seff;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.seff.CallingSEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFConfigurablePCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.extraction.IFPCMExtractionStrategy;
import org.dataflowanalysis.analysis.pcm.informationflow.core.utils.IFSecurityContextUtils;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.ExternalCallAction;

/**
 * A vertex for the evaluation of information flows. In comparison to
 * {@link CallingSEFFPCMVertex} ConfidentialityVariableCharacterisations can be
 * partly generated from normal VariableCharacterisations in accordance with an
 * {@link IFPCMExtractionStrategy}. Further, the vertex allows the consideration
 * of implicit flows.
 * 
 * In an evaluation step a label propagation function is calculated in
 * accordance with the {@link IFPCMExtractionStrategy}. This label propagation
 * function is specified in form of ConfidentialityVariableCharacterisations and
 * defines how the outgoing DataFlowVariables are calculated from incoming
 * DataFlowVariables.
 *
 */
public abstract class AbstractIFCallingSEFFPCMVertex extends CallingSEFFPCMVertex implements IFConfigurablePCMVertex {

	/*
	 * Note, most of the defined behavior in this class is nearly duplicated in
	 * AbstractIFSEFFPCMVertex and AbstractIFCallingUserPCMVertex. Changes in this
	 * class may require changes in the named classes.
	 */

	private boolean considerImplicitFlow;
	private IFPCMExtractionStrategy extractionStrategy;

	/**
	 * As for a {@link CallingSEFFPCMVertex} the vertex has an underlying
	 * {@code ExternalCallAction} SEFF element which influences the behavior through
	 * defined VariableCharacterisations. The vertex can have
	 * {@code previousElements} from which the incoming DataFlowVariables are
	 * received. Furthermore, the vertex contains an {@link AssemblyContext}, passed
	 * {@link Parameter}s as well as a {@link ResourceProvider}. Lastly, the vertex
	 * might consider implicit flow and requires an {@link IFPCMExtractionStrategy}
	 * to define how label propagation functions are extracted.
	 * 
	 * @param element              the underlying SEFF element
	 * @param previousElements     the previous vertices
	 * @param context              the AssemblyContext
	 * @param parameter            the passed Parameters
	 * @param isCalling            whether the vertex stands for the calling or
	 *                             returning behavior
	 * @param resourceProvider     the ResourceProvider
	 * @param considerImplicitFlow whether to consider implicit flow
	 * @param extractionStrategy   the extraction strategy
	 */
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

	/**
	 * Creates the concrete implementation of the vertex with the given parameters.
	 * The method is primary used for {@link #deepCopy(Map)}.
	 * 
	 * @param element
	 * @param previousElements
	 * @param context
	 * @param parameter
	 * @param resourceProvider
	 * @param considerImplicitFlow
	 * @param extractionStrategy
	 * @return the created vertex
	 * 
	 * @see #AbstractIFCallingSEFFPCMVertex(ExternalCallAction, List, Deque, List,
	 *      boolean, ResourceProvider, boolean, IFPCMExtractionStrategy)
	 */
	protected abstract AbstractIFCallingSEFFPCMVertex createIFSEFFVertex(ExternalCallAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider, boolean considerImplicitFlow,
			IFPCMExtractionStrategy extractionStrategy);

	/*
	 * evaluateDataFlow() implemented as template pattern
	 */

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

	/**
	 * Modifies the incoming DataFlowVariables of the vertex as the first step in
	 * {@link #evaluateDataFlow()}.
	 * 
	 * @param incomingVariables the incoming DataFlowVariables
	 * @return the modified incoming DataFlowVariables
	 */
	protected abstract List<DataFlowVariable> modifyIncomingDataFlowVariables(List<DataFlowVariable> incomingVariables);

	/**
	 * Extracts defined VariableCharacterisations from the underlying SEFF element
	 * as the second step in {@link #evaluateDataFlow()}.
	 * 
	 * @return the defined VariableCharacterisations
	 */
	protected abstract List<VariableCharacterisation> extractVariableCharacterisations();

	/**
	 * Checks the used ConfidentialityVariableCharacterisations as a step in
	 * {@link #evaluateDataFlow()}.
	 * 
	 * @param characterisations the used ConfidentialyVariableCharacterisations
	 */
	protected abstract void checkConfidentialityVariableCharacterisations(
			List<ConfidentialityVariableCharacterisation> characterisations);

	/**
	 * Modifies the calculated outgoing DataFlowVariables of the vertex as the last
	 * step in {@link #evaluateDataFlow()} before setting the propagation result.
	 * 
	 * @param outgoingVariables the calculated outgoing DataFlowVariables
	 * @return the modified outgoing DataFlowVariables
	 */
	protected abstract List<DataFlowVariable> modifyOutgoingDataFlowVariables(List<DataFlowVariable> outgoingVariables);

}