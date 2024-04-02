package org.dataflowanalysis.analysis.tests.constraint.data;

import de.uka.ipd.sdq.identifier.Identifier;
import java.util.List;
import java.util.Map;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;

public class ConstraintData {
    private final String nodeID;
    private final List<CharacteristicValueData> vertexCharacteristics;
    private final Map<String, List<CharacteristicValueData>> dataCharacteristics;

    public ConstraintData(String nodeID, List<CharacteristicValueData> vertexCharacteristics,
            Map<String, List<CharacteristicValueData>> dataCharacteristics) {
        this.nodeID = nodeID;
        this.vertexCharacteristics = vertexCharacteristics;
        this.dataCharacteristics = dataCharacteristics;
    }

    public boolean matches(AbstractVertex<?> element) {
        if (!(element instanceof AbstractPCMVertex<?> sequenceElement)) {
            return false;
        }
        Identifier pcmElement = sequenceElement.getReferencedElement();
        return this.nodeID.equals(pcmElement.getId());
    }

    public boolean hasNodeCharacteristic(CharacteristicValue actualCharacteristicValue) {
        return hasCharacteristicValue(vertexCharacteristics, actualCharacteristicValue);
    }

    public boolean hasDataCharacteristics(DataCharacteristic actualDataCharacteristic) {
        List<CharacteristicValueData> expectedCharacteristicValues = this.dataCharacteristics.get(actualDataCharacteristic.variableName());
        return actualDataCharacteristic.characteristics().stream().allMatch(it -> hasCharacteristicValue(expectedCharacteristicValues, it));
    }

    private boolean hasCharacteristicValue(List<CharacteristicValueData> data, CharacteristicValue actualCharacteristicValue) {
        return data.stream()
                .filter(it -> actualCharacteristicValue.getTypeName()
                        .equals(it.characteristicType()))
                .anyMatch(it -> actualCharacteristicValue.getValueName()
                        .equals(it.characteristicLiteral()));
    }

    public int vertexCharacteristicsCount() {
        return this.vertexCharacteristics.size();
    }

    public int dataCharacteristicsCount() {
        return this.dataCharacteristics.size();
    }
}
