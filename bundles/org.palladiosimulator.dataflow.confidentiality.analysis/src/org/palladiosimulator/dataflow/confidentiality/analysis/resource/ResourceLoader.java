package org.palladiosimulator.dataflow.confidentiality.analysis.resource;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public interface ResourceLoader {
	/**
	 * Loads the required resources
	 */
	public void loadRequiredResources();
	
	/**
	 * Returns the usage model that the resource loader has loaded
	 * @return Usage model saved in the resources
	 */
	public UsageModel getUsageModel();
	
	/**
	 * Returns the allocation model that the resource loader has loaded
	 * @return Allocation model saved in the resources
	 */
	public Allocation getAllocation();
	
	/**
	 * Looks up an ECore element with the given class type
	 * @param <T> Type of the objects that the lookup should return
	 * @param targetType Target type of the lookup
	 * @return Returns a list of objects that are of the target type
	 */
	public <T extends EObject> List<T> lookupElementOfType(final EClass targetType);
}
