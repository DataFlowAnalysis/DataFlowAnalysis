package org.dataflowanalysis.analysis.pcm.informationflow.core.finder;

import java.util.Deque;
import java.util.List;

import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.finder.ISEFFPCMVertexFactory;
import org.dataflowanalysis.analysis.pcm.core.seff.CallingSEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.seff.SEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFConfigurablePCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFPCMExtractionStrategy;
import org.dataflowanalysis.analysis.pcm.informationflow.core.seff.IFCallingSEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.seff.IFReturningSEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.seff.IFSetVariableSEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.seff.IFStartSEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.seff.IFStopSEFFPCMVertex;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.SetVariableAction;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.seff.StopAction;

/**
 * A factory for creating {@link SEFFPCMVertex}. Uses the implementation for
 * informationflow.
 *
 */
public class IFSEFFPCMVertextFactory implements ISEFFPCMVertexFactory {

	private boolean considerImplicitFlow;
	private IFPCMExtractionStrategy extractionStrategy;

	public IFSEFFPCMVertextFactory() {
		this(false, null); // TODO
	}

	public IFSEFFPCMVertextFactory(boolean considerImplicitFlow, IFPCMExtractionStrategy extractionStrategy) {
		this.considerImplicitFlow = considerImplicitFlow;
		this.extractionStrategy = extractionStrategy;
	}

	@Override
	public SEFFPCMVertex<StartAction> createStartElement(StartAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider) {
		var vertex = new IFStartSEFFPCMVertex(element, previousElements, context, parameter, resourceProvider);
		configureVertex(vertex);
		return vertex;
	}

	@Override
	public SEFFPCMVertex<StopAction> createStopElement(StopAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider) {
		var vertex = new IFStopSEFFPCMVertex(element, previousElements, context, parameter, resourceProvider);
		configureVertex(vertex);
		return vertex;
	}

	@Override
	public SEFFPCMVertex<SetVariableAction> createSetVariableElement(SetVariableAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider) {
		var vertex = new IFSetVariableSEFFPCMVertex(element, previousElements, context, parameter, resourceProvider);
		configureVertex(vertex);
		return vertex;
	}

	@Override
	public CallingSEFFPCMVertex createCallingElement(ExternalCallAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider) {
		var vertex = new IFCallingSEFFPCMVertex(element, previousElements, context, parameter, resourceProvider);
		configureVertex(vertex);
		return vertex;
	}

	@Override
	public CallingSEFFPCMVertex createReturningElement(ExternalCallAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider) {
		var vertex = new IFReturningSEFFPCMVertex(element, previousElements, context, parameter, resourceProvider);
		configureVertex(vertex);
		return vertex;
	}

	private void configureVertex(IFConfigurablePCMVertex vertex) {
		vertex.setConsiderImplicitFlow(considerImplicitFlow);
		vertex.setExtractionStrategy(extractionStrategy);
	}

}
