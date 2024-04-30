package org.dataflowanalysis.analysis.tests.dsl;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.ConstraintVariable;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.analysis.dsl.constraint.Intersection;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.DataCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.selectors.NodeCharacteristicsSelector;
import org.dataflowanalysis.analysis.tests.BaseTest;
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
                .withLabel("DataSensitivity", List.of("Personal", "Public"))
                .ofNode()
                .neverFlows()
                .toVertex()
                .withCharacteristic("ServerLocation", "nonEU")
                .create();

        evaluateAnalysis(constraint, internationalOnlineShopAnalysis);
    }

    @Test
    public void testVariableDSL() {
        AnalysisConstraint constraint = new ConstraintDSL()
                .ofData()
                .withLabel("AssignedRoles", ConstraintVariable.of("assignedRoles"))
                .neverFlows()
                .toVertex()
                .withCharacteristic("GrantedRoles", ConstraintVariable.of("grantedRoles"))
                .where()
                .isEmpty(Intersection.of(ConstraintVariable.of("grantedRoles"), ConstraintVariable.of("assignedRoles")))
                .create();

        evaluateAnalysis(constraint, travelPlannerAnalysis);
    }

    @Test
    public void testDataObjects() {
        AnalysisConstraint constraint = new AnalysisConstraint();
        constraint.addFlowSource(new DataCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of("DataSensitivity")), new ConstraintVariable("constant", List.of("Personal")))));
        constraint.addFlowDestination(new NodeCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of("ServerLocation")), new ConstraintVariable("constant", List.of("nonEU")))));

        evaluateAnalysis(constraint, internationalOnlineShopAnalysis);
    }

    private void evaluateAnalysis(AnalysisConstraint constraint, DataFlowConfidentialityAnalysis analysis) {
        FlowGraphCollection flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();
        List<AbstractVertex<?>> results = flowGraph.getTransposeFlowGraphs().stream()
                .flatMap(tfg -> constraint.matchPartialFlowGraph(tfg).stream())
                .toList();
        logger.setLevel(Level.TRACE);
        results.forEach(vertex -> logger.trace(vertex.createPrintableNodeInformation()));
        assertEquals(ConstraintViolations.internationalOnlineShopViolations.size(), results.size());
    }
}