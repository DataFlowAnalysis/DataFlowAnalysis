package org.dataflowanalysis.analysis.pcm.informationflow.core.user;

import java.util.List;

import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.CallingUserPCMVertex;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

public abstract class AbstractIFCallingUserPCMVertex extends CallingUserPCMVertex {

	public AbstractIFCallingUserPCMVertex(EntryLevelSystemCall element,
			List<? extends AbstractPCMVertex<?>> previousElements, boolean isCalling,
			ResourceProvider resourceProvider) {
		super(element, previousElements, isCalling, resourceProvider);
		// TODO Auto-generated constructor stub
	}

}
