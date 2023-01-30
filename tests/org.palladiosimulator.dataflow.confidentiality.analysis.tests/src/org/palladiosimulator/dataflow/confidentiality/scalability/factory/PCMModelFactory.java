package org.palladiosimulator.dataflow.confidentiality.scalability.factory;

import java.io.IOException;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.CharacteristicsFactory;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.EnumCharacteristic;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.DataDictionaryCharacterizedFactory;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.allocation.AllocationFactory;
import org.palladiosimulator.pcm.core.composition.AssemblyConnector;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.CompositionFactory;
import org.palladiosimulator.pcm.core.composition.ProvidedDelegationConnector;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.CompositeDataType;
import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.system.SystemFactory;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;
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
	
	public void addAllocationContext(AssemblyContext assemblyContext, ResourceContainer resourceContainer) {
		AllocationContext allocationContext = AllocationFactory.eINSTANCE.createAllocationContext();
		allocationContext.setAllocation_AllocationContext(allocation);
		allocationContext.setAssemblyContext_AllocationContext(assemblyContext);
		allocationContext.setResourceContainer_AllocationContext(resourceContainer);
	}
	
	public AssemblyContext addAssemblyContext(RepositoryComponent repositoryComponent) {
		AssemblyContext assemblyContext = CompositionFactory.eINSTANCE.createAssemblyContext();
		assemblyContext.setEncapsulatedComponent__AssemblyContext(repositoryComponent);
		system.getAssemblyContexts__ComposedStructure().add(assemblyContext);
		return assemblyContext;
	}
	
	public OperationProvidedRole addInterfaceOperationProvidedRole(OperationInterface providedInterface, BasicComponent basicComponent) {
		OperationProvidedRole operationProvidedRole = RepositoryFactory.eINSTANCE.createOperationProvidedRole();
		operationProvidedRole.setProvidingEntity_ProvidedRole(basicComponent);
		operationProvidedRole.setProvidedInterface__OperationProvidedRole(providedInterface);
		return operationProvidedRole;
	}
	
	public void addSystemOperationProvidedRole(OperationInterface providedInterface, AssemblyContext assemblyContext, BasicComponent basicComponent) {
		OperationProvidedRole providedRoleSystem = RepositoryFactory.eINSTANCE.createOperationProvidedRole();
		providedRoleSystem.setProvidedInterface__OperationProvidedRole(providedInterface);
		providedRoleSystem.setProvidingEntity_ProvidedRole(system);
		
		OperationProvidedRole providedRoleAssembyContext = RepositoryFactory.eINSTANCE.createOperationProvidedRole();
		providedRoleAssembyContext.setProvidedInterface__OperationProvidedRole(providedInterface);
		AssemblyConnector assemblyConnector = CompositionFactory.eINSTANCE.createAssemblyConnector();
		assemblyConnector.setProvidingAssemblyContext_AssemblyConnector(assemblyContext);
		assemblyConnector.setProvidedRole_AssemblyConnector(providedRoleAssembyContext);
		providedRoleAssembyContext.setProvidingEntity_ProvidedRole(basicComponent);
		
		ProvidedDelegationConnector providedDelegationConnector = CompositionFactory.eINSTANCE.createProvidedDelegationConnector();
		providedDelegationConnector.setInnerProvidedRole_ProvidedDelegationConnector(providedRoleAssembyContext);
		providedDelegationConnector.setOuterProvidedRole_ProvidedDelegationConnector(providedRoleSystem);
		providedDelegationConnector.setAssemblyContext_ProvidedDelegationConnector(assemblyContext);
	}
	
	public ResourceContainer addResourceContainer(String name) {
		ResourceContainer resourceContainer = ResourceenvironmentFactory.eINSTANCE.createResourceContainer();
		resourceContainer.setEntityName(name);
		resourceContainer.setResourceEnvironment_ResourceContainer(resourceEnvironment);
		resourceEnvironment.getResourceContainer_ResourceEnvironment().add(resourceContainer);
		return resourceContainer;
	}
	
	public BasicComponent addBasicComponent(String name) {
		BasicComponent basicComponent = RepositoryFactory.eINSTANCE.createBasicComponent();
		basicComponent.setEntityName(name);
		basicComponent.setRepository__RepositoryComponent(repository);
		repository.getComponents__Repository().add(basicComponent);
		return basicComponent;
	}
	
	public OperationInterface addInterface(String name) {
		OperationInterface createdInterface = RepositoryFactory.eINSTANCE.createOperationInterface();
		createdInterface.setEntityName(name);
		createdInterface.setRepository__Interface(repository);
		repository.getInterfaces__Repository().add(createdInterface);
		return createdInterface;
	}
	
	public DataType addDataType(String name) {
		CompositeDataType dataType = RepositoryFactory.eINSTANCE.createCompositeDataType();
		dataType.setRepository__DataType(repository);
		dataType.setEntityName(name);
		return dataType;
	}
	
	public OperationSignature addOperationSigniture(String methodName, DataType returnDatatype) {
		OperationSignature signature = RepositoryFactory.eINSTANCE.createOperationSignature();
		signature.setEntityName(methodName);
		signature.setReturnType__OperationSignature(returnDatatype);
		return signature;
	}
	
	public void addOperationParameter(OperationSignature signiture, Parameter parameter) {
		signiture.getParameters__OperationSignature().add(parameter);
	}
	
	public void addInterfaceSigniture(OperationInterface operationInterface, OperationSignature signature) {
		operationInterface.getSignatures__OperationInterface().add(signature);
	}
	
	public PCMSEFFFactory getSEFF() {
		return new PCMSEFFFactory(repository);
	}
	
	public void addSEFF(BasicComponent component, ServiceEffectSpecification seff) {
		component.getServiceEffectSpecifications__BasicComponent().add(seff);
	}
	
	public void setProvidedInterface(OperationInterface providedInterface, BasicComponent component) {
		OperationProvidedRole operationProvidedRole = RepositoryFactory.eINSTANCE.createOperationProvidedRole();
		operationProvidedRole.setProvidedInterface__OperationProvidedRole(providedInterface);
		component.getProvidedRoles_InterfaceProvidingEntity().add(operationProvidedRole);
	}
	
	public PCMUsageFactory getUsageScenario() {
		return new PCMUsageFactory(usageModel);
	}
	
	public void addUsageScenario(UsageScenario usageScenario) {
		usageModel.getUsageScenario_UsageModel().add(usageScenario);
	}
	
	public EnumCharacteristicType addEnumCharacteristicType(String name) {
		EnumCharacteristicType type = DataDictionaryCharacterizedFactory.eINSTANCE.createEnumCharacteristicType();
		type.setName(name);
		return type;
	}
	
	public EnumCharacteristic addEnumCharacteristic(EnumCharacteristicType type, String name) {
		EnumCharacteristic characteristic = CharacteristicsFactory.eINSTANCE.createEnumCharacteristic();
		characteristic.setEntityName(name);
		characteristic.setType(type);
		return characteristic;
	}
	
	public void saveModel() throws IOException {
		this.resource.save(Map.of());
	}
}
