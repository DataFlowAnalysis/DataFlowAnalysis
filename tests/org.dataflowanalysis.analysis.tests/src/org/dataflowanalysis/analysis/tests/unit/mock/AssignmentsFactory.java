package org.dataflowanalysis.analysis.tests.unit.mock;

import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.CharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.DataDictionaryCharacterizedFactory;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Enumeration;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.dataflowanalysis.pcm.extension.model.confidentiality.characteristics.CharacteristicsFactory;
import org.dataflowanalysis.pcm.extension.model.confidentiality.characteristics.EnumCharacteristic;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.Assignments;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.NodeCharacteristicsFactory;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.UsageAssignee;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import java.util.UUID;

public class AssignmentsFactory {
    private final Assignments assignments;

    public AssignmentsFactory() {
        this.assignments = NodeCharacteristicsFactory.eINSTANCE.createAssignments();
    }


}
