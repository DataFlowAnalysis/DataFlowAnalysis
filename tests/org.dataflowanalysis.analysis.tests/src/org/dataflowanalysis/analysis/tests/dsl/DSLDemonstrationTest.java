package org.dataflowanalysis.analysis.tests.dsl;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariable;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.analysis.dsl.Intersection;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.DataCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.selectors.VertexCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;
import org.dataflowanalysis.analysis.tests.BaseTest;
import org.dataflowanalysis.analysis.tests.constraint.data.ConstraintData;
import org.dataflowanalysis.analysis.tests.constraint.data.ConstraintViolations;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

public class DSLDemonstrationTest extends BaseTest {
    private static final Logger logger = Logger.getLogger(DSLDemonstrationTest.class);

    @Test
    public void testDSL() {
        var constraint = new ConstraintDSL()
                .ofData()
                .withLabel("DataSensitivity", List.of("Personal"))
                .ofNode()
                .neverFlows()
                .toVertex()
                .withCharacteristic("ServerLocation", "nonEU")
                .create();

        evaluateAnalysis(constraint, internationalOnlineShopAnalysis, ConstraintViolations.internationalOnlineShopViolations);
    }

    @Test
    public void testVariableDSL() {
        AnalysisConstraint constraint = new ConstraintDSL()
                .ofData()
                .withLabel("GrantedRoles", ConstraintVariable.of("grantedRoles"))
                .neverFlows()
                .toVertex()
                .withCharacteristic("AssignedRoles", ConstraintVariable.of("assignedRoles"))
                .where()
                .isNotEmpty(ConstraintVariable.of("grantedRoles"))
                .isNotEmpty(ConstraintVariable.of("assignedRoles"))
                .isEmpty(Intersection.of(ConstraintVariable.of("grantedRoles"), ConstraintVariable.of("assignedRoles")))
                .create();

        evaluateAnalysis(constraint, travelPlannerAnalysis, ConstraintViolations.travelPlannerViolations);
    }

    @Test
    public void testDataObjects() {
        AnalysisConstraint constraint = new AnalysisConstraint();
        constraint.addFlowSource(new DataCharacteristicsSelector(constraint.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of("DataSensitivity")), ConstraintVariableReference.ofConstant( List.of("Personal")))));
        constraint.addFlowDestination(new VertexCharacteristicsSelector(constraint.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of("ServerLocation")), ConstraintVariableReference.ofConstant(List.of("nonEU")))));

        evaluateAnalysis(constraint, internationalOnlineShopAnalysis, ConstraintViolations.internationalOnlineShopViolations);
    }

    private void evaluateAnalysis(AnalysisConstraint constraint, DataFlowConfidentialityAnalysis analysis, List<ConstraintData> expectedResults) {
        FlowGraphCollection flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();
        List<AbstractVertex<?>> results = flowGraph.getTransposeFlowGraphs().stream()
                .flatMap(tfg -> constraint.matchPartialFlowGraph(tfg).stream())
                .toList();
        logger.setLevel(Level.TRACE);
        results.forEach(vertex -> logger.trace(vertex.createPrintableNodeInformation()));
        assertEquals(expectedResults.size(), results.size());
    }
}