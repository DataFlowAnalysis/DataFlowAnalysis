package org.palladiosimulator.dataflow.confidentiality.analysis.resource;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public interface PCMResourceLoader {
	public void loadRequiredResources();
	public UsageModel getUsageModel();
	public Allocation getAllocation();
	public <T extends EObject> List<T> lookupElementOfType(final EClass targetType);
}
