package org.dataflowanalysis.analysis.pcm.informationflow.core.user;

import java.util.List;

import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.UserPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFConfigurablePCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFPCMExtractionStrategy;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

public abstract class AbstractIFUserPCMVertex<T extends AbstractUserAction> extends UserPCMVertex<T>
		implements IFConfigurablePCMVertex {
	
	private boolean considerImplicitFlow;
	private IFPCMExtractionStrategy extractionStrategy;

	public AbstractIFUserPCMVertex(T element, ResourceProvider resourceProvider) {
		super(element, resourceProvider);
		// TODO Auto-generated constructor stub
	}

	public AbstractIFUserPCMVertex(T element, List<? extends AbstractPCMVertex<?>> previousElements,
			ResourceProvider resourceProvider) {
		super(element, previousElements, resourceProvider);
		// TODO Auto-generated constructor stub
	}
	
	public void setConsiderImplicitFlow(boolean consider) {
		this.considerImplicitFlow = consider;
	}

	public void setExtractionStrategy(IFPCMExtractionStrategy extractionStrategy) {
		this.extractionStrategy = extractionStrategy;
	}

	public boolean isConsideringImplicitFlow() {
		return considerImplicitFlow;
	}

	public IFPCMExtractionStrategy getExtractionStrategy() {
		return extractionStrategy;
	}

}
