package org.dataflowanalysis.analysis.pcm.informationflow.tests.evaluation;

import java.util.List;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataFlowVariable;

public class EvaluationModelConditionUtils {

    private EvaluationModelConditionUtils() {
    }

    public static boolean highFlowsToLow(AbstractVertex<?> vertex) {
        return flowsTo(vertex, "High", "Low");
    }

    public static boolean highFlowsToUntrusted(AbstractVertex<?> vertex) {
        return flowsTo(vertex, "High", "Untrusted");
    }

    private static boolean flowsTo(AbstractVertex<?> vertex, String dataName, String nodeName) {
        var dataFlowVariables = vertex.getAllDataFlowVariables();
        var nodeCharacteristics = vertex.getAllNodeCharacteristics();

        boolean isUntrustedNode = containsCharacteristicValueWithName(nodeName, nodeCharacteristics);
        if (!isUntrustedNode) {
            return false;
        }

        for (DataFlowVariable dataFlowVariable : dataFlowVariables) {
            if (containsCharacteristicValueWithName(dataName, dataFlowVariable.getAllCharacteristics())) {
                return true;
            }
        }

        return false;
    }

    private static boolean containsCharacteristicValueWithName(String untrusted, List<CharacteristicValue> nodeCharacteristics) {
        boolean isUntrustedNode = false;
        for (CharacteristicValue nodeCharacteristic : nodeCharacteristics) {
            String nodeCharacteristicName = nodeCharacteristic.getValueName();
            if (nodeCharacteristicName.equals(untrusted)) {
                isUntrustedNode = true;
            }
        }
        return isUntrustedNode;
    }

}
