package org.dataflowanalysis.analysis.tests.integration.dsl.advanced;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests the advanced {@link AnalysisConstraint} DSL against the VWCariad example mode
 */
public class VWCariadAdvancedDSLTest extends AbstractAdvancedDSLTest {

    public VWCariadAdvancedDSLTest() {
        super(Paths.get("scenarios", "dfd", "VWCariad", "diagram.dataflowdiagram"),
                Paths.get("scenarios", "dfd", "VWCariad", "diagram.datadictionary"));

    }

    public static Stream<Arguments> constraints() {
        return Stream.of(Arguments.of(
                "* confidential_data: data DataSensitivity.Confidential neverFlows vertex EndpointConfiguration.Public"),
                Arguments.of(
                        "* anonymized_db: data DataSensitivity.NonAnonymized neverFlows vertex DataBaseType.AWSBucket,DataBaseType.AzureDataLake"),
                Arguments.of(
                        "* confidential_request: data RequestSensitivity.Confidential neverFlows vertex EndpointConfiguration.Public"),
                Arguments.of(
                        "* credentials_public: data DataBaseToken.AWSToken,DataBaseToken.AzureToken neverFlows vertex EndpointConfiguration.Public"),
                Arguments.of(
                        "* encrypted_data: data DataEncryption.NonEncrypted neverFlows vertex DataBaseType.AWSBucket,DataBaseType.AzureDataLake"),
                Arguments.of(
                        "* authenticated_command: data UserClearance.NonAuthenticated neverFlows vertex CarControlUnits.ECU"),
                Arguments.of(
                        "* public_or_memory: vertex EndpointConfiguration.Public or vertex EndpointConfiguration.SpringMemory neverFlows vertex CarControlUnits.ECU"),
                Arguments.of(
                        "* not_authenticated: not vertex UserClearance.Authenticated neverFlows vertex CarControlUnits.ECU"));
    }

    @ParameterizedTest
    @MethodSource("constraints")
    public void shouldParseCorrectly(String dslString) {
        ParseResult<? extends AnalysisConstraint> constraint = AnalysisConstraint.fromString(new StringView(dslString));
        if (constraint.failed()) {
            fail(constraint.getError());
        }
        assertTrue(constraint.successful());
        assertEquals(dslString, constraint.toString());
    }

    @Test
    public void testConfidentialDataNeverFlowsPublicEndpoint() {

        List<DSLResult> violations = evaluateModel(this.getConstraintAtIndex(constraints(), 0));
        assertEquals(1, violations.size());
    }

    @Test
    public void testNonAnonymizedDataNeverFlowsDatabases() {
        List<DSLResult> violations = evaluateModel(this.getConstraintAtIndex(constraints(), 1));
        assertEquals(2, violations.size());
    }

    @Test
    public void testConfidentialRequestNeverFlowsPublicEndpoint() {
        List<DSLResult> violations = evaluateModel(this.getConstraintAtIndex(constraints(), 2));
        assertEquals(1, violations.size());
    }

    @Test
    public void testDatabaseTokensNeverFlowPublicEndpoint() {
        List<DSLResult> violations = evaluateModel(this.getConstraintAtIndex(constraints(), 3));
        assertEquals(1, violations.size());
    }

    @Test
    public void testNonEncryptedDataNeverFlowsDatabases() {
        List<DSLResult> violations = evaluateModel(this.getConstraintAtIndex(constraints(), 4));
        assertEquals(3, violations.size());
    }

    @Test
    public void testNonAuthenticatedUserNeverFlowsCarControlUnit() {
        List<DSLResult> violations = evaluateModel(this.getConstraintAtIndex(constraints(), 5));
        assertEquals(0, violations.size());
    }

    @Test
    public void testPublicOrSpringMemoryEndpointsNeverFlowCarControlUnit() {
        List<DSLResult> violations = evaluateModel(this.getConstraintAtIndex(constraints(), 6));
        assertEquals(0, violations.size());
    }

    @Test
    public void testNotAuthenticatedLogicalOperatorNeverFlowsCarControlUnit() {
        List<DSLResult> violations = evaluateModel(this.getConstraintAtIndex(constraints(), 7));
        assertEquals(9, violations.size());
    }
}
