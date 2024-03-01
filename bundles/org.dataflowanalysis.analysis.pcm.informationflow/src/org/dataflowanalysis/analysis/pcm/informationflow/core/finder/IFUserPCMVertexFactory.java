package org.dataflowanalysis.analysis.pcm.informationflow.core.finder;

import java.util.List;

import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.finder.IUserPCMVertexFactory;
import org.dataflowanalysis.analysis.pcm.core.user.CallingUserPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.UserPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFConfigurablePCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFPCMExtractionStrategy;
import org.dataflowanalysis.analysis.pcm.informationflow.core.user.IFCallingUserPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.user.IFReturningUserPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.user.IFStartUserPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.user.IFStopUserPCMVertex;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.Stop;

/**
 * A factory for creating {@link UserPCMVertex}. Uses the implementation for
 * informationflow.
 *
 */
public class IFUserPCMVertexFactory implements IUserPCMVertexFactory {

	private boolean considerImplicitFlow;
	private IFPCMExtractionStrategy extractionStrategy;

	public IFUserPCMVertexFactory() {
		this(false, null); // TODO
	}

	public IFUserPCMVertexFactory(boolean considerImplicitFlow, IFPCMExtractionStrategy extractionStrategy) {
		this.considerImplicitFlow = considerImplicitFlow;
		this.extractionStrategy = extractionStrategy;
	}

	@Override
	public UserPCMVertex<Start> createStartElement(Start element, ResourceProvider resourceProvider) {
		var vertex = new IFStartUserPCMVertex(element, resourceProvider);
		configureVertex(vertex);
		return vertex;
	}

	@Override
	public UserPCMVertex<Start> createStartElement(Start element, List<? extends AbstractPCMVertex<?>> previousElements,
			ResourceProvider resourceProvider) {
		var vertex = new IFStartUserPCMVertex(element, previousElements, resourceProvider);
		configureVertex(vertex);
		return vertex;
	}

	@Override
	public UserPCMVertex<Stop> createStopElement(Stop element, ResourceProvider resourceProvider) {
		var vertex = new IFStopUserPCMVertex(element, resourceProvider);
		configureVertex(vertex);
		return vertex;
	}

	@Override
	public UserPCMVertex<Stop> createStopElement(Stop element, List<? extends AbstractPCMVertex<?>> previousElements,
			ResourceProvider resourceProvider) {
		var vertex = new IFStopUserPCMVertex(element, previousElements, resourceProvider);
		configureVertex(vertex);
		return vertex;
	}

	@Override
	public CallingUserPCMVertex createCallingElement(EntryLevelSystemCall element,
			List<? extends AbstractPCMVertex<?>> previousElements, ResourceProvider resourceProvider) {
		var vertex = new IFCallingUserPCMVertex(element, previousElements, resourceProvider);
		configureVertex(vertex);
		return vertex;
	}

	@Override
	public CallingUserPCMVertex createReturningElement(EntryLevelSystemCall element,
			List<? extends AbstractPCMVertex<?>> previousElements, ResourceProvider resourceProvider) {
		var vertex = new IFReturningUserPCMVertex(element, previousElements, resourceProvider);
		configureVertex(vertex);
		return vertex;
	}

	private void configureVertex(IFConfigurablePCMVertex vertex) {
		vertex.setConsiderImplicitFlow(considerImplicitFlow);
		vertex.setExtractionStrategy(extractionStrategy);
	}

}
