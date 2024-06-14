package org.dataflowanalysis.analysis.dsl.selectors;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.dsl.DSLContext;
import org.dataflowanalysis.analysis.dsl.Intersection;

import java.util.List;

public class IntersectionConditionalSelector implements ConditionalSelector {
    private final Logger logger = Logger.getLogger(IntersectionConditionalSelector.class);
    private final Intersection intersection;

    public IntersectionConditionalSelector(Intersection intersection) {
        this.intersection = intersection;
    }

    @Override
    public boolean matchesSelector(AbstractVertex<?> vertex, DSLContext context) {
        List<String> variableNames = vertex.getAllIncomingDataCharacteristics().stream()
                .map(DataCharacteristic::variableName)
                .toList();
        boolean result = true;
        for(String variableName : variableNames) {
            if(result) {
                result = !intersection.match(vertex,  variableName, context).isEmpty();
            }
        }
        return !result;
    }
}
