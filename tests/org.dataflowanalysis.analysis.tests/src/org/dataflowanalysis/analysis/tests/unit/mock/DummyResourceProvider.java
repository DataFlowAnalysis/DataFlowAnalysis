package org.dataflowanalysis.analysis.tests.unit.mock;

import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.DataDictionaryCharacterizedFactory;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristic;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Enumeration;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.dataflowanalysis.pcm.extension.model.confidentiality.dictionary.DictionaryFactory;
import org.dataflowanalysis.pcm.extension.model.confidentiality.dictionary.PCMDataDictionary;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationFactory;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;

import java.util.List;
import java.util.UUID;

public class DummyResourceProvider extends PCMResourceProvider {
    private final PCMDataDictionary dataDictionary;

    public DummyResourceProvider() {
        dataDictionary = DictionaryFactory.eINSTANCE.createPCMDataDictionary();
        Resource resource = new XMIResourceImpl();
        resource.getContents().add(dataDictionary);
        this.resources.getResources().add(resource);
    }

    @Override
    public UsageModel getUsageModel() {
        return (UsageModel) this.lookupToplevelElement(UsagemodelFactory.eINSTANCE.eClass()).get(0);
    }

    @Override
    public Allocation getAllocation() {
        return (Allocation) this.lookupToplevelElement(AllocationFactory.eINSTANCE.eClass()).get(0);
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
}
