package org.dataflowanalysis.analysis.pcm.informationflow.tests.evaluation;

import java.util.List;
import java.util.Optional;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataFlowVariable;

public class EvaluationModelConditionUtils {

    private EvaluationModelConditionUtils() {
    }

    public static boolean highFlowsToLow(AbstractVertex<?> vertex) {
        return flowsTo(vertex, "High", "Low");
    }

    public static boolean flowsTo(AbstractVertex<?> vertex, String dataName, String nodeName) {
        return flowsTo(vertex, Optional.empty(), dataName, Optional.empty(), nodeName);
    }

    public static boolean flowsTo(AbstractVertex<?> vertex, String dataCharacteristicType, String dataName, String nodeCharacterisiticType,
            String nodeName) {
        return flowsTo(vertex, Optional.of(dataCharacteristicType), dataName, Optional.of(nodeCharacterisiticType), nodeName);
    }

    private static boolean flowsTo(AbstractVertex<?> vertex, Optional<String> dataCharacteristicType, String dataName,
            Optional<String> nodeCharacterisiticType, String nodeName) {
        var dataFlowVariables = vertex.getAllDataFlowVariables();
        var nodeCharacteristics = vertex.getAllNodeCharacteristics();

        boolean isUntrustedNode = containsCharacteristicValue(nodeCharacterisiticType, nodeName, nodeCharacteristics);
        if (!isUntrustedNode) {
            return false;
        }

        for (DataFlowVariable dataFlowVariable : dataFlowVariables) {
            if (containsCharacteristicValue(dataCharacteristicType, dataName, dataFlowVariable.getAllCharacteristics())) {
                return true;
            }
        }

        return false;
    }

    public static boolean flowsToLower(AbstractVertex<?> vertex, String... latticeNames) {
        for (int level = 1; level < latticeNames.length; level++) {
            for (int lowerLevel = 0; lowerLevel < level; lowerLevel++) {
                if (flowsTo(vertex, latticeNames[level], latticeNames[lowerLevel])) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean containsCharacteristicValue(Optional<String> untrustedTypeName, String untrustedValueName,
            List<CharacteristicValue> nodeCharacteristics) {
        boolean isUntrustedNode = false;
        for (CharacteristicValue nodeCharacteristic : nodeCharacteristics) {
            String nodeCharacteristicValueName = nodeCharacteristic.getValueName();
            String nodeCharacteristicTypeName = nodeCharacteristic.getTypeName();

            if (!nodeCharacteristicValueName.equals(untrustedValueName)) {
                continue;
            }

            if (untrustedTypeName.isPresent() && !nodeCharacteristicTypeName.equals(untrustedTypeName.get())) {
                continue;
            }
            isUntrustedNode = true;
        }
        return isUntrustedNode;
    }

}
