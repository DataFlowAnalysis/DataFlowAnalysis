package org.dataflowanalysis.analysis.tests.constraint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.log4j.Level;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.pcm.core.PCMFlowGraphCollection;
import org.dataflowanalysis.analysis.tests.constraint.data.ConstraintData;
import org.dataflowanalysis.analysis.tests.constraint.data.ConstraintViolations;
import org.junit.jupiter.api.Test;

public class ConstraintResultTest extends ConstraintTest {
    /**
     * Indicates whether an element in an action sequence violates the constraint of the travel planner model
     * @param node Element of the action sequence
     * @return Returns true, if the constraint is violated. Otherwise, the method returns false.
     */
    private boolean travelPlannerCondition(AbstractVertex<?> node) {
        List<String> assignedRoles = node.getVertexCharacteristics("AssignedRoles").stream()
                .map(CharacteristicValue::getValueName)
                .toList();
        Collection<List<CharacteristicValue>> grantedRoles = node.getDataCharacteristicMap("GrantedRoles").values();

        printNodeInformation(node);

        for (List<CharacteristicValue> dataFlowCharacteristics : grantedRoles) {
            if (!dataFlowCharacteristics.isEmpty() && dataFlowCharacteristics.stream()
                    .distinct()
                    .map(CharacteristicValue::getValueName)
                    .noneMatch(assignedRoles::contains)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indicates whether an element in an action sequence violates the constraint of the international online shop model
     * @param node Element of the action sequence
     * @return Returns true, if the constraint is violated. Otherwise, the method returns false.
     */
    private boolean internationalOnlineShopCondition(AbstractVertex<?> node) {
        List<String> serverLocation = node.getVertexCharacteristics("ServerLocation").stream()
                .map(CharacteristicValue::getValueName)
                .toList();
        List<String> dataSensitivity = node.getDataCharacteristicMap("DataSensitivity").values().stream()
                .flatMap(Collection::stream)
                .map(CharacteristicValue::getValueName)
                .toList();
        printNodeInformation(node);

        return dataSensitivity.stream()
                .anyMatch(l -> l.equals("Personal"))
                && serverLocation.stream()
                        .anyMatch(l -> l.equals("nonEU"));
    }

    /**
     * Indicates whether an element in an action sequence violates the constraint of the return test model
     * @param node Element of the action sequence
     * @return Returns true, if the constraint is violated. Otherwise, the method returns false.
     */
    private boolean returnCondition(AbstractVertex<?> node) {
        List<String> assignedNode = new ArrayList<>(node.getVertexCharacteristics("AssignedRole").stream()
                .map(CharacteristicValue::getValueName)
                .toList());
        List<String> assignedVariables = node.getDataCharacteristicMap("AssignedRole").values().stream()
                .flatMap(Collection::stream)
                .map(CharacteristicValue::getValueName)
                .toList();

        printNodeInformation(node);
        if (assignedNode.isEmpty() || assignedVariables.isEmpty()) {
            return false;
        }
        assignedNode.removeAll(assignedVariables);
        return !assignedNode.isEmpty();
    }

    /**
     * Tests, whether the analysis correctly identifies violations for the example models
     * <p>
     * Fails if the analysis does not propagate the correct characteristics for each ActionSequence
     */
    @Test
    public void travelPlannerTestConstraintResults() {
        travelPlannerAnalysis.setLoggerLevel(Level.TRACE);
        Predicate<AbstractVertex<?>> constraint = this::travelPlannerCondition;
        List<ConstraintData> constraintData = ConstraintViolations.travelPlannerViolations;
        testAnalysis(travelPlannerAnalysis, constraint, constraintData);
    }

    /**
     * Tests, whether the analysis correctly identifies violations for the example models
     * <p>
     * Fails if the analysis does not propagate the correct characteristics for each ActionSequence
     */
    @Test
    public void internationalOnlineShopTestConstraintResults() {
        internationalOnlineShopAnalysis.setLoggerLevel(Level.TRACE);
        Predicate<AbstractVertex<?>> constraint = this::internationalOnlineShopCondition;
        List<ConstraintData> constraintData = ConstraintViolations.internationalOnlineShopViolations;
        testAnalysis(internationalOnlineShopAnalysis, constraint, constraintData);
    }

    /**
     * Tests, whether the analysis correctly identifies violations for the example models
     * <p>
     * Fails if the analysis does not propagate the correct characteristics for each ActionSequence
     */
    @Test
    public void oneAssemblyMultipleResourceTestConstraintResults() {
        PCMDataFlowConfidentialityAnalysis analysis = super.initializeAnalysis(
                Paths.get("models", "OneAssemblyMultipleResourceContainerTest", "default.usagemodel"),
                Paths.get("models", "OneAssemblyMultipleResourceContainerTest", "default.allocation"),
                Paths.get("models", "OneAssemblyMultipleResourceContainerTest", "default.nodecharacteristics"));
        analysis.setLoggerLevel(Level.TRACE);
        Predicate<AbstractVertex<?>> constraint = this::internationalOnlineShopCondition;
        List<ConstraintData> constraintData = ConstraintViolations.multipleResourcesViolations;
        testAnalysis(analysis, constraint, constraintData);
    }

    /**
     * Tests, whether the analysis correctly identifies violations for the example models
     * <p>
     * Fails if the analysis does not propagate the correct characteristics for each ActionSequence
     */
    @Test
    public void returnTestConstraintResults() {
        PCMDataFlowConfidentialityAnalysis returnAnalysis = super.initializeAnalysis(Paths.get("models", "ReturnTestModel", "default.usagemodel"),
                Paths.get("models", "ReturnTestModel", "default.allocation"), Paths.get("models", "ReturnTestModel", "default.nodecharacteristics"));
        Predicate<AbstractVertex<?>> constraint = this::returnCondition;
        returnAnalysis.setLoggerLevel(Level.TRACE);
        List<ConstraintData> constraintData = ConstraintViolations.returnViolations;
        testAnalysis(returnAnalysis, constraint, constraintData);
    }

    public void testAnalysis(PCMDataFlowConfidentialityAnalysis analysis, Predicate<AbstractVertex<?>> constraint,
            List<ConstraintData> constraintData) {
        PCMFlowGraphCollection flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();
        List<AbstractVertex<?>> results = flowGraph.getTransposeFlowGraphs()
                .stream()
                .map(it -> analysis.queryDataFlow(it, constraint))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        assertEquals(constraintData.size(), results.size(), "Incorrect count of violations found");

        for (ConstraintData constraintNodeData : constraintData) {
            var violatingNode = results.stream()
                    .filter(constraintNodeData::matches)
                    .findFirst();

            if (violatingNode.isEmpty()) {
                fail("Could not find node for expected constraint violation");
            }

            List<CharacteristicValue> nodeCharacteristics = violatingNode.get().getAllVertexCharacteristics();
            List<DataFlowVariable> dataFlowVariables = violatingNode.get().getAllDataCharacteristics();

            assertEquals(constraintNodeData.nodeCharacteristicsCount(), nodeCharacteristics.size());
            assertEquals(constraintNodeData.dataFlowVariablesCount(), dataFlowVariables.size());

            for (CharacteristicValue characteristicValue : nodeCharacteristics) {
                assertTrue(constraintNodeData.hasNodeCharacteristic(characteristicValue));
            }

            for (DataFlowVariable dataFlowVariable : dataFlowVariables) {
                assertTrue(constraintNodeData.hasDataFlowVariable(dataFlowVariable));
            }
        }
    }
}
