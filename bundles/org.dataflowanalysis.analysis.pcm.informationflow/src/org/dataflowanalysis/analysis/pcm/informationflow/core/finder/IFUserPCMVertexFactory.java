package org.dataflowanalysis.analysis.pcm.informationflow.core.finder;

import java.util.List;

import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.finder.IUserPCMVertexFactory;
import org.dataflowanalysis.analysis.pcm.core.user.CallingUserPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.UserPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.user.IFCallingUserPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.user.IFReturningUserPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.user.IFStartUserPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.user.IFStopUserPCMVertex;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.Stop;

/**
 * A factory for creating {@link UserPCMVertex}. Uses the implementation for informationflow.
 *
 */
public class IFUserPCMVertexFactory implements IUserPCMVertexFactory {

	@Override
	public UserPCMVertex<Start> createStartElement(Start element, ResourceProvider resourceProvider) {
		return new IFStartUserPCMVertex(element, resourceProvider);
	}

	@Override
	public UserPCMVertex<Start> createStartElement(Start element, List<? extends AbstractPCMVertex<?>> previousElements,
			ResourceProvider resourceProvider) {
		return new IFStartUserPCMVertex(element, previousElements, resourceProvider);
	}

	@Override
	public UserPCMVertex<Stop> createStopElement(Stop element, ResourceProvider resourceProvider) {
		return new IFStopUserPCMVertex(element, resourceProvider);
	}

	@Override
	public UserPCMVertex<Stop> createStopElement(Stop element, List<? extends AbstractPCMVertex<?>> previousElements,
			ResourceProvider resourceProvider) {
		return new IFStopUserPCMVertex(element, previousElements, resourceProvider);
	}

	@Override
	public CallingUserPCMVertex createCallingElement(EntryLevelSystemCall element,
			List<? extends AbstractPCMVertex<?>> previousElements, ResourceProvider resourceProvider) {
		return new IFCallingUserPCMVertex(element, previousElements, resourceProvider);
	}

	@Override
	public CallingUserPCMVertex createReturningElement(EntryLevelSystemCall element,
			List<? extends AbstractPCMVertex<?>> previousElements, ResourceProvider resourceProvider) {
		return new IFReturningUserPCMVertex(element, previousElements, resourceProvider);
	}

}
