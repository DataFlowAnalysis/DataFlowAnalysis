package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;

import java.util.List;

public class EmptySetOperationConditionalSelector implements ConditionalSelector {
    private final SetOperation setOperation;

    public EmptySetOperationConditionalSelector(SetOperation setOperation) {
        this.setOperation = setOperation;
    }

    @Override
    public boolean matchesSelector(AbstractVertex<?> vertex, DSLContext context) {
        List<String> variableNames = vertex.getAllIncomingDataCharacteristics().stream()
                .map(DataCharacteristic::variableName)
                .toList();
        boolean result = true;
        for(String variableName : variableNames) {
            if(result) {
                result = !setOperation.match(vertex,  variableName, context).isEmpty();
            }
        }
        return !result;
    }
}
