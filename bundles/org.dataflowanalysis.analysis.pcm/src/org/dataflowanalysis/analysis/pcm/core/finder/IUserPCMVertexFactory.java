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
 * A factory for creating {@link UserPCMVertex}.
 *
 */
public interface IUserPCMVertexFactory {
	
	/**
	 * Creates a {@link UserPCMVertex} for a Start element with the given parameters.
	 * @param element
	 * @param resourceProvider
	 * @return the UserPCMVertex
	 */
	public UserPCMVertex<Start> createStartElement(Start element, ResourceProvider resourceProvider);
	/**
	 * Creates a {@link UserPCMVertex} for a Start element with the given parameters.
	 * @param element
	 * @param previousElements
	 * @param resourceProvider
	 * @return the UserPCMVertex
	 */
	public UserPCMVertex<Start> createStartElement(Start element, List<? extends AbstractPCMVertex<?>> previousElements, ResourceProvider resourceProvider);
	/**
	 * Creates a {@link UserPCMVertex} for a Stop element with the given parameters.
	 * @param element
	 * @param resourceProvider
	 * @return the UserPCMVertex
	 */
	public UserPCMVertex<Stop> createStopElement(Stop element, ResourceProvider resourceProvider);
	/**
	 * Creates a {@link UserPCMVertex} for a Stop element with the given parameters.
	 * @param element
	 * @param previousElements
	 * @param resourceProvider
	 * @return the UserPCMVertex
	 */
	public UserPCMVertex<Stop> createStopElement(Stop element, List<? extends AbstractPCMVertex<?>> previousElements, ResourceProvider resourceProvider);
	
	/**
	 * Creates a calling {@link CallingUserPCMVertex} for an EntryLevelSystemCall element with the given parameters.
	 * @param element
	 * @param previousElements
	 * @param resourceProvider
	 * @return the CallingUserPCMVertex
	 */
	public CallingUserPCMVertex createCallingElement(EntryLevelSystemCall element, List<? extends AbstractPCMVertex<?>> previousElements, ResourceProvider resourceProvider);
	/**
	 * Creates a returning {@link CallingUserPCMVertex} for an EntryLevelSystemCall element with the given parameters.
	 * @param element
	 * @param previousElements
	 * @param resourceProvider
	 * @return the CallingUserPCMVertex
	 */
	public CallingUserPCMVertex createReturningElement(EntryLevelSystemCall element, List<? extends AbstractPCMVertex<?>> previousElements, ResourceProvider resourceProvider);

}
