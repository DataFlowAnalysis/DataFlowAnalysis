package org.dataflowanalysis.analysis.pcm.informationflow.core.user;

import java.util.List;

import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.usagemodel.Stop;

public class IFStopUserPCMVertex extends AbstractIFUserPCMVertex<Stop> {

	public IFStopUserPCMVertex(Stop element, ResourceProvider resourceProvider) {
		super(element, resourceProvider);
		// TODO Auto-generated constructor stub
	}

	public IFStopUserPCMVertex(Stop element, List<? extends AbstractPCMVertex<?>> previousElements,
			ResourceProvider resourceProvider) {
		super(element, previousElements, resourceProvider);
		// TODO Auto-generated constructor stub
	}

}
