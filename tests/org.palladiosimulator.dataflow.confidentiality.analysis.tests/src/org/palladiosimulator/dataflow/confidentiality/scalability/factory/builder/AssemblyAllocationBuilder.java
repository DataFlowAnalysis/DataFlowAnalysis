package org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder;

import java.util.UUID;

import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.CharacteristicsFactory;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.EnumCharacteristic;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.node.NodeCharacteristicBuilder;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.allocation.AllocationFactory;
import org.palladiosimulator.pcm.core.composition.AssemblyConnector;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.CompositionFactory;
import org.palladiosimulator.pcm.core.composition.ProvidedDelegationConnector;
import org.palladiosimulator.pcm.core.composition.RequiredDelegationConnector;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.system.System;

public class AssemblyAllocationBuilder {
	private System system;
	private Allocation allocation;
	private AssemblyContext assemblyContext;
	private NodeCharacteristicBuilder nodeCharacteristicBuilder;
	/*
	private Assignments assignments;
	 */
	
	private AssemblyAllocationBuilder(System system, Allocation allocation, 
			AssemblyContext assemblyContext, NodeCharacteristicBuilder nodeCharacteristicBuilder) {
		this.system = system;
		this.allocation = allocation;
		this.assemblyContext = assemblyContext;
		this.nodeCharacteristicBuilder = nodeCharacteristicBuilder;
	}
	
	public static AssemblyAllocationBuilder builder(System system, Allocation allocation, 
			AssemblyContext assemblyContext, NodeCharacteristicBuilder nodeCharacteristicBuilder) {
		return new AssemblyAllocationBuilder(system, allocation, assemblyContext, nodeCharacteristicBuilder);
	}
	
	public AssemblyAllocationBuilder addCharacteristic(EnumCharacteristicType characteristicType, String characteristicValue) {
		Literal literal = characteristicType.getType().getLiterals().stream()
					.filter(it -> it.getName().equalsIgnoreCase(characteristicValue))
					.findAny().orElseThrow(() -> new IllegalArgumentException("Unknown characteristic value"));
		EnumCharacteristic characteristic = CharacteristicsFactory.eINSTANCE.createEnumCharacteristic();
		characteristic.setType(characteristicType);
		characteristic.getValues().add(literal);
		nodeCharacteristicBuilder.addCharacteristic(assemblyContext, characteristic);
		return this;
	}
	
	public AssemblyAllocationBuilder addAllocation(String name, ResourceContainer resourceContainer) {
		AllocationContext allocationContext = AllocationFactory.eINSTANCE.createAllocationContext();
		allocationContext.setId(UUID.randomUUID().toString());
		allocationContext.setEntityName(name);
		allocationContext.setAllocation_AllocationContext(allocation);
		allocationContext.setAssemblyContext_AllocationContext(assemblyContext);
		allocationContext.setResourceContainer_AllocationContext(resourceContainer);
		this.allocation.getAllocationContexts_Allocation().add(allocationContext);
		return this;
	}
	
	public OperationRequiredRole addSystemRequiredRole(String name, OperationInterface operationInterface) {
		OperationRequiredRole requiredRoleSystem = RepositoryFactory.eINSTANCE.createOperationRequiredRole();
		requiredRoleSystem.setId(UUID.randomUUID().toString());
		requiredRoleSystem.setEntityName(name);
		requiredRoleSystem.setRequiredInterface__OperationRequiredRole(operationInterface);
		requiredRoleSystem.setRequiringEntity_RequiredRole(system);
		
		OperationRequiredRole requiredRoleAssembyContext = RepositoryFactory.eINSTANCE.createOperationRequiredRole();
		AssemblyConnector assemblyConnector = CompositionFactory.eINSTANCE.createAssemblyConnector();
		assemblyConnector.setRequiringAssemblyContext_AssemblyConnector(assemblyContext);
		assemblyConnector.setRequiredRole_AssemblyConnector(requiredRoleAssembyContext);
		requiredRoleAssembyContext.setRequiredInterface__OperationRequiredRole(operationInterface);
		requiredRoleAssembyContext.setRequiringEntity_RequiredRole(assemblyContext.getEncapsulatedComponent__AssemblyContext());
		
		RequiredDelegationConnector requiredDelegationConnector = CompositionFactory.eINSTANCE.createRequiredDelegationConnector();
		requiredDelegationConnector.setInnerRequiredRole_RequiredDelegationConnector(requiredRoleAssembyContext);
		requiredDelegationConnector.setOuterRequiredRole_RequiredDelegationConnector(requiredRoleSystem);
		requiredDelegationConnector.setAssemblyContext_RequiredDelegationConnector(assemblyContext);
		
		this.system.getConnectors__ComposedStructure().add(requiredDelegationConnector);
		return requiredRoleSystem;
	}
	
	public OperationProvidedRole addSystemProvidedRole(String name, OperationInterface operationInterface) {
		OperationProvidedRole providedRoleSystem = RepositoryFactory.eINSTANCE.createOperationProvidedRole();
		providedRoleSystem.setId(UUID.randomUUID().toString());
		providedRoleSystem.setEntityName(name);
		providedRoleSystem.setProvidedInterface__OperationProvidedRole(operationInterface);
		providedRoleSystem.setProvidingEntity_ProvidedRole(system);
		
		OperationProvidedRole providedRoleAssembyContext = RepositoryFactory.eINSTANCE.createOperationProvidedRole();
		providedRoleAssembyContext.setProvidedInterface__OperationProvidedRole(operationInterface);
		providedRoleAssembyContext.setProvidingEntity_ProvidedRole(assemblyContext.getEncapsulatedComponent__AssemblyContext());
		AssemblyConnector assemblyConnector = CompositionFactory.eINSTANCE.createAssemblyConnector();
		assemblyConnector.setProvidingAssemblyContext_AssemblyConnector(assemblyContext);
		assemblyConnector.setProvidedRole_AssemblyConnector(providedRoleAssembyContext);
		
		ProvidedDelegationConnector providedDelegationConnector = CompositionFactory.eINSTANCE.createProvidedDelegationConnector();
		providedDelegationConnector.setInnerProvidedRole_ProvidedDelegationConnector(providedRoleAssembyContext);
		providedDelegationConnector.setOuterProvidedRole_ProvidedDelegationConnector(providedRoleSystem);
		providedDelegationConnector.setAssemblyContext_ProvidedDelegationConnector(assemblyContext);
		
		this.system.getConnectors__ComposedStructure().add(providedDelegationConnector);
		return providedRoleSystem;
	}
}
