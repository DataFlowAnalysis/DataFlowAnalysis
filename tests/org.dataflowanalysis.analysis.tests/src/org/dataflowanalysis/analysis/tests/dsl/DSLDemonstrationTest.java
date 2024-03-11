package org.dataflowanalysis.analysis.tests.dsl;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.DataCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.selectors.NodeCharacteristicsSelector;
import org.dataflowanalysis.analysis.pcm.core.PCMFlowGraph;
import org.dataflowanalysis.analysis.tests.BaseTest;
import org.dataflowanalysis.analysis.tests.constraint.data.ConstraintViolations;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

public class DSLDemonstrationTest extends BaseTest {
    private static final Logger logger = Logger.getLogger(DSLDemonstrationTest.class);

    @Test
    public void testDSL() {
        AnalysisConstraint constraint = new ConstraintDSL()
                .ofData()
                .withLabel("DataSensitivity", "Personal")
                .neverFlows()
                .toNode()
                .withCharacteristic("ServerLocation", "nonEU")
                .create();

        evaluateAnalysis(constraint);
    }

    @Test
    public void testDataObjects() {
        AnalysisConstraint constraint = new AnalysisConstraint();
        constraint.addFlowSource(new DataCharacteristicsSelector(new CharacteristicsSelectorData("DataSensitivity", "Personal")));
        constraint.addFlowDestination(new NodeCharacteristicsSelector(new CharacteristicsSelectorData("ServerLocation", "nonEU")));

        evaluateAnalysis(constraint);
    }

    private void evaluateAnalysis(AnalysisConstraint constraint) {
        PCMFlowGraph flowGraph = internationalOnlineShopAnalysis.findFlowGraph();
        flowGraph.evaluate();
        List<AbstractVertex<?>> results = flowGraph.getPartialFlowGraphs().stream()
                .flatMap(pfg -> constraint.matchPartialFlowGraph(pfg).stream())
                .toList();
        logger.setLevel(Level.TRACE);
        results.forEach(vertex -> logger.trace(vertex.createPrintableNodeInformation()));
        assertEquals(ConstraintViolations.internationalOnlineShopViolations.size(), results.size());
    }
}