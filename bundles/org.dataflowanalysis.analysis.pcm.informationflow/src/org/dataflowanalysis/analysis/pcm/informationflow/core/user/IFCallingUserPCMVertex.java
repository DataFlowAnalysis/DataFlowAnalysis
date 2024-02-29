package org.dataflowanalysis.analysis.pcm.informationflow.core.user;

import java.util.List;

import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.CallingUserPCMVertex;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

public class IFCallingUserPCMVertex extends CallingUserPCMVertex {

	public IFCallingUserPCMVertex(EntryLevelSystemCall element, List<? extends AbstractPCMVertex<?>> previousElements,
			ResourceProvider resourceProvider) {
		super(element, previousElements, true, resourceProvider);
		// TODO Auto-generated constructor stub
	}

}
