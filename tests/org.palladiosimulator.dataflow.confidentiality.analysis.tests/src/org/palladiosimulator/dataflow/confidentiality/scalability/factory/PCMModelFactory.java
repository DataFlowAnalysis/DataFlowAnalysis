package org.palladiosimulator.dataflow.confidentiality.scalability.factory;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.AssemblyAllocationBuilder;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationFactory;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.CompositionFactory;
import org.palladiosimulator.pcm.repository.CompositeDataType;
import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.system.SystemFactory;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;

public class PCMModelFactory {
	private Resource resource;
	
	private System system;
	private Allocation allocation;
	private Repository repository;
	private ResourceEnvironment resourceEnvironment;
	private UsageModel usageModel;
	
	public PCMModelFactory(String filePath) {
		resource = new XMLResourceImpl(URI.createFileURI(filePath));
		
		system = SystemFactory.eINSTANCE.createSystem();
		allocation = AllocationFactory.eINSTANCE.createAllocation();
		repository = RepositoryFactory.eINSTANCE.createRepository();
		resourceEnvironment = ResourceenvironmentFactory.eINSTANCE.createResourceEnvironment();
		usageModel = UsagemodelFactory.eINSTANCE.createUsageModel();
		
		resource.getContents().add(system);
		resource.getContents().add(allocation);
		resource.getContents().add(repository);
		resource.getContents().add(resourceEnvironment);
		resource.getContents().add(usageModel);
	}
	
	public AssemblyAllocationBuilder addAssemblyContext(String name, RepositoryComponent repositoryComponent) {
		AssemblyContext assemblyContext = CompositionFactory.eINSTANCE.createAssemblyContext();
		assemblyContext.setId(UUID.randomUUID().toString());
		assemblyContext.setEntityName(name);
		assemblyContext.setEncapsulatedComponent__AssemblyContext(repositoryComponent);
		system.getAssemblyContexts__ComposedStructure().add(assemblyContext);
		return AssemblyAllocationBuilder.builder(system, allocation, assemblyContext);
	}
	
	public ResourceContainer addResourceContainer(String name) {
		ResourceContainer resourceContainer = ResourceenvironmentFactory.eINSTANCE.createResourceContainer();
		resourceContainer.setEntityName(name);
		resourceContainer.setResourceEnvironment_ResourceContainer(resourceEnvironment);
		resourceEnvironment.getResourceContainer_ResourceEnvironment().add(resourceContainer);
		return resourceContainer;
	}
	
	public DataType addDataType(String name) {
		CompositeDataType dataType = RepositoryFactory.eINSTANCE.createCompositeDataType();
		dataType.setRepository__DataType(repository);
		dataType.setEntityName(name);
		return dataType;
	}
	
	public Repository getRepository() {
		return repository;
	}
	
	public UsageModel getUsageModel() {
		return usageModel;
	}
	
	public void saveModel() throws IOException {
		this.resource.save(Map.of());
	}
}
