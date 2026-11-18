package org.dataflowanalysis.analysis.tests.integration.dsl.advanced;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

/**
 * Tests the evaluation results of the advanced {@link AnalysisConstraint} DSL against the standard OnlineShop example
 * model.
 */
public class OnlineShopAdvancedDSLTest extends AbstractAdvancedDSLTest {
    public OnlineShopAdvancedDSLTest() {
        super(Paths.get("scenarios", "dfd", "OnlineShop", "default.dataflowdiagram"),
                Paths.get("scenarios", "dfd", "OnlineShop", "default.datadictionary"));
    }

    public static Stream<Arguments> constraints() {
        return Stream.of(Arguments.of("* default: data Sensitivity.Public flows data Encryption.Encrypted"),
                Arguments.of("* default: data Sensitivity.Public alwaysFlows data Encryption.Encrypted"),
                Arguments.of("* default: data Sensitivity.Public notAlwaysFlows data Encryption.Encrypted"),
                Arguments.of("* default: vertex Location.EU or vertex Location.nonEU neverFlows any"),
                Arguments.of("* default: vertex Location.EU xor vertex Location.nonEU neverFlows any"),
                Arguments.of("* default: not vertex Location.nonEU neverFlows vertex Location.nonEU"),
                Arguments.of("* default: vertex Location.nonEU neverFlows any"),
                Arguments.of("* default: vertex Location.EU neverFlows vertex Location.nonEU"),
                Arguments.of("* default: vertex name contains User neverFlows vertex Location.nonEU"),
                Arguments.of("* default: data Sensitivity.Public neverFlows data Encryption.Encrypted"), Arguments.of(
                        "* default: vertex Location.EU and data !Encryption.Encrypted neverFlows vertex Location.nonEU"));
    }

    @Test
    public void testFlowsWithOnlineShop() {
        List<DSLResult> violations = evaluateModel(this.getConstraintAtIndex(constraints(), 0));
        assertEquals(3, violations.size());
    }

    @Test
    public void testAlwaysFlowsWithOnlineShop() {
        List<DSLResult> violations = evaluateModel(this.getConstraintAtIndex(constraints(), 1));
        assertEquals(2, violations.size());
    }

    @Test
    public void testNotAlwaysFlowsWithOnlineShop() {
        List<DSLResult> violations = evaluateModel(this.getConstraintAtIndex(constraints(), 2));
        assertEquals(3, violations.size());
    }

    @Test
    public void testOrLogicalOperatorWithOnlineShop() {
        List<DSLResult> violations = evaluateModel(this.getConstraintAtIndex(constraints(), 3));
        assertEquals(3, violations.size());
    }

    @Test
    public void testXorLogicalOperatorWithOnlineShop() {
        List<DSLResult> violations = evaluateModel(this.getConstraintAtIndex(constraints(), 4));
        assertEquals(3, violations.size());
    }

    @Test
    public void testNotLogicalOperatorWithOnlineShop() {
        List<DSLResult> violations = evaluateModel(this.getConstraintAtIndex(constraints(), 5));
        assertEquals(0, violations.size());
    }

    @Test
    public void testDefaultAnyDestinationWithOnlineShop() {
        List<DSLResult> violations = evaluateModel(this.getConstraintAtIndex(constraints(), 6));
        assertEquals(3, violations.size());
    }

    @Test
    public void testVertexToVertexWithOnlineShop() {
        List<DSLResult> violations = evaluateModel(this.getConstraintAtIndex(constraints(), 7));
        assertEquals(2, violations.size());
    }

    @Test
    public void testVertexNameToVertexWithOnlineShop() {
        List<DSLResult> violations = evaluateModel(this.getConstraintAtIndex(constraints(), 8));
        assertEquals(2, violations.size());
    }

    @Test
    public void testDataToDataWithOnlineShop() {
        List<DSLResult> violations = evaluateModel(this.getConstraintAtIndex(constraints(), 9));
        assertEquals(1, violations.size());
    }

    @Test
    public void testComplexWithOnlineShop() {
        List<DSLResult> violations = evaluateModel(this.getConstraintAtIndex(constraints(), 10));
        assertEquals(1, violations.size());
    }
}
