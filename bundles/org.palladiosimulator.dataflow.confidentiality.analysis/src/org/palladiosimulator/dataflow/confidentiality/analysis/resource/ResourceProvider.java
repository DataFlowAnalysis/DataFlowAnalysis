package org.palladiosimulator.dataflow.confidentiality.analysis.resource;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.RepositoryPackage;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentPackage;
import org.palladiosimulator.pcm.system.SystemPackage;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public interface ResourceProvider {
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
	
	
	/**
	 * Determines, whether the resource loader has sufficient resources to run the analysis
	 * @return This method returns true, if the analysis can be executed with the resource loader. Otherwise, the method returns false
	 */
	public default boolean sufficientResourcesLoaded() {
		if (this.getUsageModel() == null || this.getAllocation() == null) {
			return false;
		}
		if (this.lookupElementOfType(RepositoryPackage.eINSTANCE.getRepository()).isEmpty()) {
			return false;
		}
		if (this.lookupElementOfType(SystemPackage.eINSTANCE.getSystem()).isEmpty()) {
			return false;
		}
		if (this.lookupElementOfType(ResourceenvironmentPackage.eINSTANCE.getResourceEnvironment()).isEmpty()) {
			return false;
		}
		return true;
	}
	/**
	 * Looks up an ECore element with the given class type
	 * @param id  Id of the objects that the lookup should return
	 * @return Returns the object with the given id
	 */
	public EObject lookupElementWithId(String id);
	
	/**
	 * 
	 */
	public Entity findElement(Predicate<Entity> condition);
}
