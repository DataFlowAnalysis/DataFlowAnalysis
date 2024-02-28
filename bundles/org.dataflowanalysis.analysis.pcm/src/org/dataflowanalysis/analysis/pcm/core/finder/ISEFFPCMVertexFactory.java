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
 * A factory for creating {@link SEFFPCMVertex}.
 *
 */
public interface ISEFFPCMVertexFactory {
	
	/**
	 * Creates a {@link SEFFPCMVertex} for a StartAction with the given parameters.
	 * @param element
	 * @param previousElements
	 * @param context
	 * @param parameter
	 * @param resourceProvider
	 * @return the SEFFPCMVertex
	 */
	public SEFFPCMVertex<StartAction> createStartElement(StartAction element, List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context, List<Parameter> parameter, ResourceProvider resourceProvider);
	/**
	 * Creates a {@link SEFFPCMVertex} for a StopAction with the given parameters.
	 * @param element
	 * @param previousElements
	 * @param context
	 * @param parameter
	 * @param resourceProvider
	 * @return the SEFFPCMVertex
	 */
	public SEFFPCMVertex<StopAction> createStopElement(StopAction element, List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context, List<Parameter> parameter, ResourceProvider resourceProvider);
	/**
	 * Creates a {@link SEFFPCMVertex} for a SetVariableAction with the given parameters.
	 * @param element
	 * @param previousElements
	 * @param context
	 * @param parameter
	 * @param resourceProvider
	 * @return the SEFFPCMVertex
	 */
	public SEFFPCMVertex<SetVariableAction> createSetVariableElement(SetVariableAction element, List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context, List<Parameter> parameter, ResourceProvider resourceProvider);
	
	/**
	 * Creates a calling {@link CallingSEFFPCMVertex} for an ExternalCallAction with the given parameters.
	 * @param element
	 * @param previousElements
	 * @param context
	 * @param parameter
	 * @param resourceProvider
	 * @return the CallingSEFFPCMVertex
	 */
	public CallingSEFFPCMVertex createCallingElement(ExternalCallAction element, List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context, List<Parameter> parameter, ResourceProvider resourceProvider);
	/**
	 * Creates a returning {@link CallingSEFFPCMVertex} for an ExternalCallAction with the given parameters.
	 * @param element
	 * @param previousElements
	 * @param context
	 * @param parameter
	 * @param resourceProvider
	 * @return the CallingSEFFPCMVertex
	 */
	public CallingSEFFPCMVertex createReturningElement(ExternalCallAction element, List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context, List<Parameter> parameter, ResourceProvider resourceProvider);

}
