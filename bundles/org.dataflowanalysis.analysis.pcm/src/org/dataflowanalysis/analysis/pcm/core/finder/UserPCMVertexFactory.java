package org.dataflowanalysis.analysis.pcm.core.finder;

import java.util.List;

import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.CallingUserPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.UserPCMVertex;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.Stop;

/**
 * A factory for creating {@link UserPCMVertex}. Uses the standard implementation of {@link UserPCMVertex}.
 *
 */
public class UserPCMVertexFactory implements IUserPCMVertexFactory {

	@Override
	public UserPCMVertex<Start> createStartElement(Start element, ResourceProvider resourceProvider) {
		return new UserPCMVertex<Start>(element, resourceProvider);
	}

	@Override
	public UserPCMVertex<Start> createStartElement(Start element, List<? extends AbstractPCMVertex<?>> previousElements,
			ResourceProvider resourceProvider) {
		return new UserPCMVertex<Start>(element, previousElements, resourceProvider);
	}

	@Override
	public UserPCMVertex<Stop> createStopElement(Stop element, ResourceProvider resourceProvider) {
		return new UserPCMVertex<Stop>(element, resourceProvider);
	}

	@Override
	public UserPCMVertex<Stop> createStopElement(Stop element, List<? extends AbstractPCMVertex<?>> previousElements,
			ResourceProvider resourceProvider) {
		return new UserPCMVertex<Stop>(element, previousElements, resourceProvider);
	}

	@Override
	public CallingUserPCMVertex createCallingElement(EntryLevelSystemCall element,
			List<? extends AbstractPCMVertex<?>> previousElements, ResourceProvider resourceProvider) {
		return new CallingUserPCMVertex(element, previousElements, true, resourceProvider);
	}

	@Override
	public CallingUserPCMVertex createReturningElement(EntryLevelSystemCall element,
			List<? extends AbstractPCMVertex<?>> previousElements, ResourceProvider resourceProvider) {
		return new CallingUserPCMVertex(element, previousElements, false, resourceProvider);
	}

}