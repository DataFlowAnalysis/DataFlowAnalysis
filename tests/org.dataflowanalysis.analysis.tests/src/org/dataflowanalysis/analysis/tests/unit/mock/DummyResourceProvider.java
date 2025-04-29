package org.dataflowanalysis.analysis.tests.unit.mock;

import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.DataDictionaryCharacterizedFactory;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristic;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Enumeration;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.dataflowanalysis.pcm.extension.model.confidentiality.characteristics.CharacteristicsFactory;
import org.dataflowanalysis.pcm.extension.model.confidentiality.dictionary.DictionaryFactory;
import org.dataflowanalysis.pcm.extension.model.confidentiality.dictionary.PCMDataDictionary;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.AssemblyAssignee;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.Assignments;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.NodeCharacteristicsFactory;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.ResourceAssignee;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.UsageAssignee;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.allocation.AllocationFactory;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.CompositionFactory;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.system.SystemFactory;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;

import java.util.List;
import java.util.UUID;

public class DummyResourceProvider extends PCMResourceProvider {
    private final PCMDataDictionary dataDictionary;
    private final Assignments assignments;
    private final Allocation allocation;
    private final UsageModel usageModel;
    private final System system;
    private final ResourceEnvironment resourceEnvironment;

    public DummyResourceProvider() {
        dataDictionary = DictionaryFactory.eINSTANCE.createPCMDataDictionary();
        Resource resource = new XMIResourceImpl();
        resource.getContents().add(dataDictionary);
        this.resources.getResources().add(resource);

        assignments = NodeCharacteristicsFactory.eINSTANCE.createAssignments();
        Resource assignmentsResource = new XMIResourceImpl();
        assignmentsResource.getContents().add(assignments);
        this.resources.getResources().add(assignmentsResource);

        allocation = AllocationFactory.eINSTANCE.createAllocation();
        Resource allocationResource = new XMIResourceImpl();
        allocationResource.getContents().add(allocation);
        this.resources.getResources().add(allocationResource);

        usageModel = UsagemodelFactory.eINSTANCE.createUsageModel();
        Resource usageResource = new XMIResourceImpl();
        usageResource.getContents().add(usageModel);
        this.resources.getResources().add(usageResource);

        system = SystemFactory.eINSTANCE.createSystem();
        Resource systemResource = new XMIResourceImpl();
        systemResource.getContents().add(system);
        this.resources.getResources().add(systemResource);

        resourceEnvironment = ResourceenvironmentFactory.eINSTANCE.createResourceEnvironment();
        Resource resourceResource = new XMIResourceImpl();
        resourceResource.getContents().add(resourceEnvironment);
        this.resources.getResources().add(resourceResource);
    }

    @Override
    public UsageModel getUsageModel() {
        return this.usageModel;
    }

    @Override
    public Allocation getAllocation() {
        return this.allocation;
    }

    @Override
    public void loadRequiredResources() {

    }

    public void addType(String type, List<String> values) {
        Enumeration enumeration = DataDictionaryCharacterizedFactory.eINSTANCE.createEnumeration();
        enumeration.setName(type);
        enumeration.setId(String.valueOf(UUID.randomUUID()));

        EnumCharacteristicType characteristicType = DataDictionaryCharacterizedFactory.eINSTANCE.createEnumCharacteristicType();
        characteristicType.setId(String.valueOf(UUID.randomUUID()));
        characteristicType.setName(type);
        characteristicType.setType(enumeration);
        this.dataDictionary.getCharacteristicTypes().add(characteristicType);

        for(String value : values) {
            Literal literal = DataDictionaryCharacterizedFactory.eINSTANCE.createLiteral();
            literal.setId(String.valueOf(UUID.randomUUID()));
            literal.setName(value);
            literal.setEnum(enumeration);

            EnumCharacteristic characteristic = DataDictionaryCharacterizedFactory.eINSTANCE.createEnumCharacteristic();
            characteristic.setId(String.valueOf(UUID.randomUUID()));
            characteristic.setName(value);
            characteristic.setType(characteristicType);
            characteristic.getValues().add(literal);
        }
    }

    public void addUsageAssignment(UsageScenario usageScenario, String type, String value) {
        org.dataflowanalysis.pcm.extension.model.confidentiality.characteristics.EnumCharacteristic characteristic = org.dataflowanalysis.pcm.extension.model.confidentiality.characteristics.CharacteristicsFactory.eINSTANCE.createEnumCharacteristic();
        EnumCharacteristicType characteristicType = getEnumCharacteristicType(type);
        characteristic.setType(characteristicType);
        characteristic.getValues().add(getEnumCharacteristicValue(characteristicType, value));

        UsageAssignee usageAssignee = NodeCharacteristicsFactory.eINSTANCE.createUsageAssignee();
        usageAssignee.setUsagescenario(usageScenario);
        usageAssignee.getCharacteristics().add(characteristic);
        assignments.getAssignee().add(usageAssignee);
    }

    public void addAssemblyAssignment(AssemblyContext assemblyContext, String type, String value) {
        org.dataflowanalysis.pcm.extension.model.confidentiality.characteristics.EnumCharacteristic characteristic = org.dataflowanalysis.pcm.extension.model.confidentiality.characteristics.CharacteristicsFactory.eINSTANCE.createEnumCharacteristic();
        EnumCharacteristicType characteristicType = getEnumCharacteristicType(type);
        characteristic.setType(characteristicType);
        characteristic.getValues().add(getEnumCharacteristicValue(characteristicType, value));

        AssemblyAssignee assemblyAssignee = NodeCharacteristicsFactory.eINSTANCE.createAssemblyAssignee();
        assemblyAssignee.setAssemblycontext(assemblyContext);
        assemblyAssignee.getCharacteristics().add(characteristic);
        assignments.getAssignee().add(assemblyAssignee);
    }

    public void addResourceAssignment(ResourceContainer resourceContainer, String type, String value) {
        org.dataflowanalysis.pcm.extension.model.confidentiality.characteristics.EnumCharacteristic characteristic = org.dataflowanalysis.pcm.extension.model.confidentiality.characteristics.CharacteristicsFactory.eINSTANCE.createEnumCharacteristic();
        EnumCharacteristicType characteristicType = getEnumCharacteristicType(type);
        characteristic.setType(characteristicType);
        characteristic.getValues().add(getEnumCharacteristicValue(characteristicType, value));

        ResourceAssignee resourceAssignee = NodeCharacteristicsFactory.eINSTANCE.createResourceAssignee();
        resourceAssignee.setResourcecontainer(resourceContainer);
        resourceAssignee.getCharacteristics().add(characteristic);
        assignments.getAssignee().add(resourceAssignee);
    }

    public AssemblyContext addAssemblyContext(String name, ResourceContainer resourceContainer) {
        AssemblyContext assemblyContext = CompositionFactory.eINSTANCE.createAssemblyContext();
        assemblyContext.setEntityName(name);
        this.system.getAssemblyContexts__ComposedStructure().add(assemblyContext);

        AllocationContext allocationContext = AllocationFactory.eINSTANCE.createAllocationContext();
        allocationContext.setAssemblyContext_AllocationContext(assemblyContext);
        allocationContext.setResourceContainer_AllocationContext(resourceContainer);
        this.allocation.getAllocationContexts_Allocation().add(allocationContext);
        return assemblyContext;
    }

    private EnumCharacteristicType getEnumCharacteristicType(String type) {
        Enumeration enumeration = DataDictionaryCharacterizedFactory.eINSTANCE.createEnumeration();
        enumeration.setName(type);
        enumeration.setId(String.valueOf(UUID.randomUUID()));

        EnumCharacteristicType characteristicType = DataDictionaryCharacterizedFactory.eINSTANCE.createEnumCharacteristicType();
        characteristicType.setId(String.valueOf(UUID.randomUUID()));
        characteristicType.setName(type);
        characteristicType.setType(enumeration);
        return characteristicType;
    }

    private Literal getEnumCharacteristicValue(EnumCharacteristicType characteristicType, String value) {
        Literal literal = DataDictionaryCharacterizedFactory.eINSTANCE.createLiteral();
        literal.setId(String.valueOf(UUID.randomUUID()));
        literal.setName(value);
        literal.setEnum(characteristicType.getType());

        org.dataflowanalysis.pcm.extension.model.confidentiality.characteristics.EnumCharacteristic characteristic = CharacteristicsFactory.eINSTANCE.createEnumCharacteristic();
        characteristic.setId(String.valueOf(UUID.randomUUID()));
        characteristic.setEntityName(value);
        characteristic.setType(characteristicType);
        characteristic.getValues().add(literal);
        return literal;
    }

    public UsageScenario getUsageScenario(String name) {
        UsageScenario usageScenario = UsagemodelFactory.eINSTANCE.createUsageScenario();
        usageScenario.setEntityName(name);
        this.usageModel.getUsageScenario_UsageModel().add(usageScenario);
        return usageScenario;
    }

    public ResourceContainer getResourceContainer(String name) {
        ResourceContainer resourceContainer = ResourceenvironmentFactory.eINSTANCE.createResourceContainer();
        resourceContainer.setEntityName(name);
        this.resourceEnvironment.getResourceContainer_ResourceEnvironment().add(resourceContainer);
        return resourceContainer;
    }

    public Assignments getAssignments() {
        return assignments;
    }
}
