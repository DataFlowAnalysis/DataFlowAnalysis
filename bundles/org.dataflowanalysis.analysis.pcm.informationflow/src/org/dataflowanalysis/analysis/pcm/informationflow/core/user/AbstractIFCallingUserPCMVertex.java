package org.dataflowanalysis.analysis.pcm.informationflow.core.user;

import java.util.List;

import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.CallingUserPCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFConfigurablePCMVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFPCMExtractionStrategy;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

public abstract class AbstractIFCallingUserPCMVertex extends CallingUserPCMVertex implements IFConfigurablePCMVertex {
	
	private boolean considerImplicitFlow;
	private IFPCMExtractionStrategy extractionStrategy;

	public AbstractIFCallingUserPCMVertex(EntryLevelSystemCall element,
			List<? extends AbstractPCMVertex<?>> previousElements, boolean isCalling,
			ResourceProvider resourceProvider) {
		super(element, previousElements, isCalling, resourceProvider);
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
