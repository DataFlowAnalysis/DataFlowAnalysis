package org.dataflowanalysis.analysis.pcm.core.finder;

import java.util.Deque;
import java.util.List;

import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.seff.CallingSEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.seff.SEFFPCMVertex;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.SetVariableAction;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.seff.StopAction;

/**
 * A factory for creating {@link SEFFPCMVertex}. Uses the standard implementation of {@link SEFFPCMVertex}.
 *
 */
public class SEFFPCMVertexFactory implements ISEFFPCMVertexFactory {

	@Override
	public SEFFPCMVertex<StartAction> createStartElement(StartAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider) {
		return new SEFFPCMVertex<StartAction>(element, previousElements, context, parameter, resourceProvider);
	}

	@Override
	public SEFFPCMVertex<StopAction> createStopElement(StopAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider) {
		return new SEFFPCMVertex<StopAction>(element, previousElements, context, parameter, resourceProvider);
	}

	@Override
	public SEFFPCMVertex<SetVariableAction> createSetVariableElement(SetVariableAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider) {
		return new SEFFPCMVertex<SetVariableAction>(element, previousElements, context, parameter, resourceProvider);
	}

	@Override
	public CallingSEFFPCMVertex createCallingElement(ExternalCallAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider) {
		return new CallingSEFFPCMVertex(element, previousElements, context, parameter, true, resourceProvider);
	}

	@Override
	public CallingSEFFPCMVertex createReturningElement(ExternalCallAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider) {
		return new CallingSEFFPCMVertex(element, previousElements, context, parameter, false, resourceProvider);
	}

}
