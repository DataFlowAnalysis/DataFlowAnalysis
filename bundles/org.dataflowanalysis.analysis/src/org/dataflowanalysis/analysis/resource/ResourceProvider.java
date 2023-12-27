package org.dataflowanalysis.analysis.resource;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

public interface ResourceProvider {
	/**
	 * Loads the required resources
	 */
	public void loadRequiredResources();
	
	/**
	 * Looks up an ECore element with the given class type
	 * @param <T> Type of the objects that the lookup should return
	 * @param targetType Target type of the lookup
	 * @return Returns a list of objects that are of the target type
	 */
	public <T extends EObject> List<T> lookupElementOfType(final EClass targetType);
	
	
	/**
	 * Collects all loaded resources loaded by the resource provider
	 * @return Returns a list of all loaded resources
	 */
	public Collection<Resource> getResources();
	
	/**
	 * Determines, whether the resource loader has sufficient resources to run the analysis
	 * @return This method returns true, if the analysis can be executed with the resource loader. Otherwise, the method returns false
	 */
	public boolean sufficientResourcesLoaded();

	/**
	 * Looks up an ECore element with the given class type
	 * @param id  Id of the objects that the lookup should return
	 * @return Returns the object with the given id
	 */
	public Optional<EObject> lookupElementWithId(String id);

	/**
	 * Finds an element that satisfies the given condition
	 * @param condition Condition the element should satisfy
	 * @return Returns the first element found that satisfies the given condition
	 */
	public Optional<EObject> findElement(Predicate<EObject> condition);
}
