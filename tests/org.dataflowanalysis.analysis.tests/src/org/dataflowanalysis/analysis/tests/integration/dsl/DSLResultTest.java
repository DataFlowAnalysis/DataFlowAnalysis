package org.dataflowanalysis.analysis.tests.integration.dsl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.AnalysisQuery;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.analysis.dsl.query.QueryDSL;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.DataCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.selectors.Intersection;
import org.dataflowanalysis.analysis.dsl.selectors.VertexCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariable;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;
import org.dataflowanalysis.analysis.pcm.core.user.UserPCMVertex;
import org.dataflowanalysis.analysis.pcm.dsl.PCMDSLContextProvider;
import org.dataflowanalysis.analysis.pcm.dsl.PCMVertexType;
import org.dataflowanalysis.analysis.tests.integration.BaseTest;
import org.dataflowanalysis.analysis.tests.integration.constraint.data.ConstraintData;
import org.dataflowanalysis.analysis.tests.integration.constraint.data.ConstraintViolations;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.api.Test;

public class DSLResultTest extends BaseTest {
    private static final Logger logger = Logger.getLogger(DSLResultTest.class);

    @Test
    public void testDSL() {
        var constraint = new ConstraintDSL().ofData()
                .withLabel("DataSensitivity", List.of("Personal"))
                .fromNode()
                .neverFlows()
                .toVertex()
                .withCharacteristic("ServerLocation", "nonEU")
                .create();

        evaluateAnalysis(constraint, internationalOnlineShopAnalysis, ConstraintViolations.internationalOnlineShopViolations);
    }

    @Test
    public void testVariableDSL() {
        AnalysisConstraint constraint = new ConstraintDSL().ofData()
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
    public void testQueryDSL() {
        AnalysisQuery query = new QueryDSL().ofNode()
                .withType(PCMVertexType.USER)
                .build();
        FlowGraphCollection flowGraphCollection = this.travelPlannerAnalysis.findFlowGraphs();
        List<DSLResult> results = query.query(flowGraphCollection);
        List<? extends AbstractVertex<?>> queriedVertices = results.stream()
                .map(DSLResult::getMatchedVertices)
                .flatMap(List::stream)
                .toList();
        assertEquals(14, queriedVertices.size(), "Flight planner contains 14 usage vertices");
        assertTrue(queriedVertices.get(0) instanceof UserPCMVertex<?>);
        var userPCMVertex = (UserPCMVertex<?>) queriedVertices.get(0);
        assertEquals("User", userPCMVertex.getReferencedElement()
                .getScenarioBehaviour_AbstractUserAction()
                .getUsageScenario_SenarioBehaviour()
                .getEntityName());
    }

    @Test
    public void testDataObjects() {
        AnalysisConstraint constraint = new AnalysisConstraint("default");
        constraint.addDataSourceSelector(new DataCharacteristicsSelector(constraint.getContext(), new CharacteristicsSelectorData(
                ConstraintVariableReference.ofConstant(List.of("DataSensitivity")), ConstraintVariableReference.ofConstant(List.of("Personal")))));
        constraint.addNodeDestinationSelector(new VertexCharacteristicsSelector(constraint.getContext(), new CharacteristicsSelectorData(
                ConstraintVariableReference.ofConstant(List.of("ServerLocation")), ConstraintVariableReference.ofConstant(List.of("nonEU")))));

        evaluateAnalysis(constraint, internationalOnlineShopAnalysis, ConstraintViolations.internationalOnlineShopViolations);
    }

    @Test
    public void testStringify() {
        AnalysisConstraint constraint = new AnalysisConstraint("testDSL");
        constraint.addDataSourceSelector(new DataCharacteristicsSelector(constraint.getContext(), new CharacteristicsSelectorData(
                ConstraintVariableReference.ofConstant(List.of("DataSensitivity")), ConstraintVariableReference.ofConstant(List.of("Personal")))));
        constraint.addNodeDestinationSelector(new VertexCharacteristicsSelector(constraint.getContext(), new CharacteristicsSelectorData(
                ConstraintVariableReference.ofConstant(List.of("ServerLocation")), ConstraintVariableReference.ofConstant(List.of("nonEU")))));
        assertEquals("- testDSL: data DataSensitivity.Personal neverFlows vertex ServerLocation.nonEU", constraint.toString());
    }

    private void evaluateAnalysis(AnalysisConstraint constraint, DataFlowConfidentialityAnalysis analysis, List<ConstraintData> expectedResults) {
        logger.info("DSL String: " + constraint.toString());
        ParseResult<AnalysisConstraint> constraintParsed = AnalysisConstraint.fromString(new StringView(constraint.toString()),
                new PCMDSLContextProvider());
        if (constraintParsed.failed()) {
            fail(System.lineSeparator() + constraintParsed.getError());
        }
        assertEquals(constraint.toString(), constraintParsed.getResult()
                .toString());

        FlowGraphCollection flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();
        List<DSLResult> result = constraint.findViolations(flowGraph);
        List<? extends AbstractVertex<?>> violations = result.stream()
                .map(DSLResult::getMatchedVertices)
                .flatMap(List::stream)
                .toList();
        logger.setLevel(Level.TRACE);
        violations.forEach(vertex -> logger.trace(vertex.createPrintableNodeInformation()));
        assertEquals(expectedResults.size(), violations.size());
    }
}