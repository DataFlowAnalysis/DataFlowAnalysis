package org.palladiosimulator.dataflow.confidentiality.scalability.factory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;
import org.palladiosimulator.dataflow.confidentiality.pcm.dddsl.DDDslStandaloneSetup;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.CharacteristicsFactory;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.EnumCharacteristic;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.dictionary.DictionaryFactory;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.dictionary.PCMDataDictionary;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.AssemblyAllocationBuilder;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.node.LegacyCharacteristicBuilder;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.node.NodeCharacteristicBuilder;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.node.NodeCharacteristicBuilderImpl;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationFactory;
import org.palladiosimulator.pcm.allocation.util.AllocationResourceImpl;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.CompositionFactory;
import org.palladiosimulator.pcm.repository.CompositeDataType;
import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.pcm.repository.util.RepositoryResourceImpl;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;
import org.palladiosimulator.pcm.resourceenvironment.util.ResourceenvironmentResourceImpl;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.system.SystemFactory;
import org.palladiosimulator.pcm.system.util.SystemResourceImpl;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;
import org.palladiosimulator.pcm.usagemodel.util.UsagemodelResourceImpl;

import com.google.inject.Injector;

public class PCMModelFactory {
	private List<Resource> resources;
	
	private System system;
	private Allocation allocation;
	private Repository repository;
	private ResourceEnvironment resourceEnvironment;
	private UsageModel usageModel;
	private PCMDataDictionary dictionary;
	
	private NodeCharacteristicBuilder nodeCharacteristicBuilder;
	
	public PCMModelFactory(String filePath, boolean legacy, Class<?> activator, String modelPath) throws IOException {
		resources = new ArrayList<>();
		var basePath = Paths.get("/home/felix/Fluidtrust/Repositories/Palladio-Addons-DataFlowConfidentiality-Analysis/scalability/org.palladiosimulator.dataflow.confidentiality.analysis.scalibility.testmodels", filePath);
		File path = new File(basePath.toString());
		
		system = SystemFactory.eINSTANCE.createSystem();
		Resource systemResource = new SystemResourceImpl(URI.createFileURI(path.getAbsolutePath() + "/generated.system"));
		systemResource.getContents().add(system);
		resources.add(systemResource);
		
		allocation = AllocationFactory.eINSTANCE.createAllocation();
		Resource allocationResource = new AllocationResourceImpl(URI.createFileURI(path.getAbsolutePath() + "/generated.allocation"));
		allocationResource.getContents().add(allocation);
		resources.add(allocationResource);
		
		repository = RepositoryFactory.eINSTANCE.createRepository();
		Resource repositoryResource = new RepositoryResourceImpl(URI.createFileURI(path.getAbsolutePath() + "/generated.repository"));
		repositoryResource.getContents().add(repository);
		resources.add(repositoryResource);
		
		resourceEnvironment = ResourceenvironmentFactory.eINSTANCE.createResourceEnvironment();
		Resource resourceEnvironmentResource = new ResourceenvironmentResourceImpl(URI.createFileURI(path.getAbsolutePath() + "/generated.resourceenvironment"));
		resourceEnvironmentResource.getContents().add(resourceEnvironment);
		resources.add(resourceEnvironmentResource);
		
		usageModel = UsagemodelFactory.eINSTANCE.createUsageModel();
		Resource usageResource = new UsagemodelResourceImpl(URI.createFileURI(path.getAbsolutePath() + "/generated.usagemodel"));
		usageResource.getContents().add(usageModel);
		resources.add(usageResource);
		
		dictionary = DictionaryFactory.eINSTANCE.createPCMDataDictionary();
		URI uri = URI.createFileURI(path.getAbsolutePath() + "/generated.pddc");
		DDDslStandaloneSetup.doSetup();
		Injector injector = new DDDslStandaloneSetup().createInjectorAndDoEMFRegistration();
		XtextResource resource = injector.getInstance(XtextResource.class);
		resource.setURI(uri);
		resource.getContents().add(dictionary);
		resources.add(resource);
		
		if(legacy) {
			this.nodeCharacteristicBuilder = new LegacyCharacteristicBuilder(activator, modelPath, usageResource, resourceEnvironmentResource, systemResource);
		} else {
			this.nodeCharacteristicBuilder = new NodeCharacteristicBuilderImpl(URI.createFileURI(path.getAbsolutePath() + "/generated.characteristics"));
		}
		this.nodeCharacteristicBuilder.setup();
	}
	
	public AssemblyAllocationBuilder addAssemblyContext(String name, RepositoryComponent repositoryComponent) {
		AssemblyContext assemblyContext = CompositionFactory.eINSTANCE.createAssemblyContext();
		assemblyContext.setId(UUID.randomUUID().toString());
		assemblyContext.setEntityName(name);
		assemblyContext.setEncapsulatedComponent__AssemblyContext(repositoryComponent);
		system.getAssemblyContexts__ComposedStructure().add(assemblyContext);
		return AssemblyAllocationBuilder.builder(system, allocation, assemblyContext, nodeCharacteristicBuilder);
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
		nodeCharacteristicBuilder.addCharacteristic(container, characteristic);
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
	
	public PCMDataDictionary getDictionary() {
		return this.dictionary;
	}
	
	public Allocation getAllocation() {
		return allocation;
	}
	
	public List<Resource> getResources() {
		return this.resources;
	}
	
	public NodeCharacteristicBuilder getNodeCharacteristicBuilder() {
		return nodeCharacteristicBuilder;
	}
	
	public void saveModel() throws IOException {
		for (Resource resource : this.resources) {
			resource.save(Map.of());
		}
		this.nodeCharacteristicBuilder.save();
	}
}
