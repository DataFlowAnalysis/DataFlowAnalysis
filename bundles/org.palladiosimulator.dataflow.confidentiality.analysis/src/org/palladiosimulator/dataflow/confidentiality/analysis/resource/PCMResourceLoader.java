package org.palladiosimulator.dataflow.confidentiality.analysis.resource;

import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public interface PCMResourceLoader {
	public void loadRequiredResources();
	public UsageModel getUsageModel();
	public Allocation getAllocation();
}
