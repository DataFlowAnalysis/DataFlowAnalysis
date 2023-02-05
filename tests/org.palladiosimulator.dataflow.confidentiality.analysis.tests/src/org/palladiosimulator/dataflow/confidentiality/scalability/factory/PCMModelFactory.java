package org.palladiosimulator.dataflow.confidentiality.scalability.factory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.CharacteristicsFactory;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.EnumCharacteristic;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.dictionary.DictionaryFactory;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.dictionary.PCMDataDictionary;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.AssemblyAllocationBuilder;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;
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
	private List<Resource> resources;
	
	private System system;
	private Allocation allocation;
	private Repository repository;
	private ResourceEnvironment resourceEnvironment;
	private UsageModel usageModel;
	private PCMDataDictionary dictionary;
	
	public PCMModelFactory(String filePath) {
		resources = new ArrayList<>();
		File path = new File(filePath);
		
		system = SystemFactory.eINSTANCE.createSystem();
		Resource systemResource = new XMLResourceImpl(URI.createFileURI(path.getAbsolutePath() + "/generated.system"));
		systemResource.getContents().add(system);
		resources.add(systemResource);
		
		allocation = AllocationFactory.eINSTANCE.createAllocation();
		Resource allocationResource = new XMLResourceImpl(URI.createFileURI(path.getAbsolutePath() + "/generated.allocation"));
		allocationResource.getContents().add(allocation);
		resources.add(allocationResource);
		
		repository = RepositoryFactory.eINSTANCE.createRepository();
		Resource repositoryResource = new XMLResourceImpl(URI.createFileURI(path.getAbsolutePath() + "/generated.repository"));
		repositoryResource.getContents().add(repository);
		resources.add(repositoryResource);
		
		resourceEnvironment = ResourceenvironmentFactory.eINSTANCE.createResourceEnvironment();
		Resource resourceEnvironmentResource = new XMLResourceImpl(URI.createFileURI(path.getAbsolutePath() + "/generated.resourceenvironment"));
		resourceEnvironmentResource.getContents().add(resourceEnvironment);
		resources.add(resourceEnvironmentResource);
		
		usageModel = UsagemodelFactory.eINSTANCE.createUsageModel();
		Resource usageResource = new XMLResourceImpl(URI.createFileURI(path.getAbsolutePath() + "/generated.usagemodel"));
		usageResource.getContents().add(usageModel);
		resources.add(usageResource);
		
		dictionary = DictionaryFactory.eINSTANCE.createPCMDataDictionary();
		Resource dictionaryResource = new XMLResourceImpl(URI.createFileURI(path.getAbsolutePath() + "/generated.pddc"));
		dictionaryResource.getContents().add(dictionary);
		resources.add(dictionaryResource);
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
	
	public void addCharacteristicResourceContainer(ResourceContainer container, EnumCharacteristicType characteristicType, String characteristicValue) {
		Literal literal = characteristicType.getType().getLiterals().stream()
					.filter(it -> it.getName().equalsIgnoreCase(characteristicValue))
					.findAny().orElseThrow(() -> new IllegalArgumentException("Unknown characteristic value"));
		EnumCharacteristic characteristic = CharacteristicsFactory.eINSTANCE.createEnumCharacteristic();
		characteristic.setType(characteristicType);
		characteristic.getValues().add(literal);
		/*
		ResourceAssignee assignee = NodeCharacteristicsFactory.eINSTANCE.createResourceAssignee();
		assignee.setResourceContainer(container);
		assignee.getCharacteristics().add(characteristic);
		assignments.getAssignees().add(assignee);
		 */
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
	
	public System getSystem() {
		return system;
	}
	
	public void saveModel() throws IOException {
		for (Resource resource : this.resources) {
			resource.save(Map.of());
		}
	}
}
