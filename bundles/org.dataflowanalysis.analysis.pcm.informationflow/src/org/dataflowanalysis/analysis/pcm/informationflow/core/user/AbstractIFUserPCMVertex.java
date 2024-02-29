package org.dataflowanalysis.analysis.pcm.informationflow.core.user;

import java.util.List;

import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.UserPCMVertex;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

public abstract class AbstractIFUserPCMVertex<T extends AbstractUserAction> extends UserPCMVertex<T> {

	public AbstractIFUserPCMVertex(T element, ResourceProvider resourceProvider) {
		super(element, resourceProvider);
		// TODO Auto-generated constructor stub
	}
	
	public AbstractIFUserPCMVertex(T element, List<? extends AbstractPCMVertex<?>> previousElements,
			ResourceProvider resourceProvider) {
		super(element, previousElements, resourceProvider);
		// TODO Auto-generated constructor stub
	}

}
