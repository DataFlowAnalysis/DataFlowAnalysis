package org.palladiosimulator.dataflow.confidentiality.analysis;


import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;

public class ViolationTest extends AnalysisFeatureTest {
	/**
     * Tests the present characteristics of the found action sequences
     * <p>
     * Fails if the analysis does not propagate the correct characteristics to each ActionSequence
     */
    @DisplayName("Example violation for travel planner model")
    @Test
    public void travelPlannerViolation() {
        var sequences = travelPlannerAnalysis.findAllSequences();
        var propagationResult = travelPlannerAnalysis.evaluateDataFlows(sequences);

        var result = travelPlannerAnalysis.queryDataFlow(propagationResult.get(0), node -> travelPlannerCondition(node));
        assertTrue(result.isEmpty());
    }
    
    /**
     * Provides the vio
     * @param node
     * @return
     */
    private boolean travelPlannerCondition(AbstractActionSequenceElement<?> node) {
    	var assignedRoles = node.getAllNodeVariables().stream()
    			.filter(cv -> cv.characteristicType().getName().equals("AssignedRoles"))
    			.map(cv -> cv.characteristicLiteral())
    			.collect(Collectors.toList());
    	var grantedRoles = node.getAllDataFlowVariables().stream()
    			.flatMap(df -> df.getAllCharacteristics().stream())
    			.filter(cv -> cv.characteristicType().getName().equals("GrantedRoles"))
    			.map(cv -> cv.characteristicLiteral())
    			.collect(Collectors.toList());
    	return assignedRoles.stream().noneMatch(ar -> grantedRoles.contains(ar));
    }
}
