package org.palladiosimulator.dataflow.confidentiality.analysis;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;

public class ViolationTest extends AnalysisFeatureTest {
	/**
     * Tests, whether the analysis correctly identifies violations for the travel planner model
     * <p>
     * Fails if the analysis does not propagate the correct characteristics for each ActionSequence
     */
    @DisplayName("Find violations for the example model")
    @MethodSource("violationTestProvider")
    @ParameterizedTest
    public void violationTest(StandalonePCMDataFlowConfidentialtyAnalysis analysis, Predicate<AbstractActionSequenceElement<?>> contraint, boolean noViolations) {
        var sequences = analysis.findAllSequences();
        var propagationResult = analysis.evaluateDataFlows(sequences);

        var result = analysis.queryDataFlow(propagationResult.get(0), contraint);
        assertEquals(noViolations, result.isEmpty());
    }
    
    private Stream<Arguments> violationTestProvider() {
    	Predicate<AbstractActionSequenceElement<?>> travelPlannerContraint = node -> travelPlannerCondition(node);
    	Predicate<AbstractActionSequenceElement<?>> internationalOnlineShopContraint = node -> internationalOnlineShopCondition(node);
    	return Stream.of(
    			Arguments.of(travelPlannerAnalysis, travelPlannerContraint, true),
    			Arguments.of(internationalOnlineShopAnalysis, internationalOnlineShopContraint, false)
    			);
    }
    
    /**
     * Indicates whether an element in an action sequence violates the constraint of the travel planner model
     * @param node Element of the action sequence
     * @return Returns true, if the constraint is violated. Otherwise, the method returns false.
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
    
    /**
     * Indicates whether an element in an action sequence violates the constraint of the international online shop model
     * @param node Element of the action sequence
     * @return Returns true, if the constraint is violated. Otherwise, the method returns false.
     */
    private boolean internationalOnlineShopCondition(AbstractActionSequenceElement<?> node) {
    	var serverLocation = node.getAllNodeVariables().stream()
    			.filter(cv -> cv.characteristicType().getName().equals("ServerLocation"))
    			.map(cv -> cv.characteristicLiteral())
    			.collect(Collectors.toList());
    	var dataSensitivity = node.getAllDataFlowVariables().stream()
    			.flatMap(df -> df.getAllCharacteristics().stream())
    			.filter(cv -> cv.characteristicType().getName().equals("DataSensitivity"))
    			.map(cv -> cv.characteristicLiteral())
    			.collect(Collectors.toList());
    	return dataSensitivity.stream().anyMatch(l -> l.getName().equals("Personal")) && serverLocation.stream().anyMatch(l -> l.getName().equals("nonEU"));
    }
}
