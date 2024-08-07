package org.dataflowanalysis.analysis.tests.dfd;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.core.DFDCyclicTransposeFlowGraphFinder;
import org.dataflowanalysis.examplemodels.Activator;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

public class MicroSecEndTest {
    public static final String PROJECT_NAME = "org.dataflowanalysis.examplemodels";
    public static final String location = Paths.get("casestudies", "TUHH-Models")
            .toString();
    private final Logger logger = Logger.getLogger(MicroSecEndTest.class);

    private static final Map<String, List<Integer>> TUHH_MODELS = ImmutableMap.<String, List<Integer>>builder()
            .put("anilallewar", List.of(0, 6, 7, 8, 9, 11, 12, 18))
            .put("apssouza22", List.of(0, 2, 4, 6, 7, 8, 12, 18))
            .put("callistaenterprise", List.of(0, 2, 6, 11, 18))
            .put("ewolff", List.of(5, 10, 12, 18))
            .put("ewolff-kafka", List.of(0, 3, 4, 5, 6, 7, 8, 9, 18))
            .put("fernandoabcampos", List.of(18))
            .put("georgwittberger", List.of(0, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 18))
            .put("jferrater", List.of(0, 2, 3, 5, 6, 7, 8, 9, 18))
            .put("koushikkothagal", List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 18))
            .put("mdeket", List.of(5))
            .put("mudigal-technologies", List.of(0, 2, 4, 5, 7, 8, 11, 18))
            .put("rohitghatol", List.of(10, 12, 18))
            .put("spring-petclinic", List.of(0, 2, 3, 5, 6, 7, 8, 9, 18))
            .put("sqshq", List.of(0, 6, 7, 8, 9, 10, 11, 12, 18))
            .put("yidongnan", List.of(0, 2, 3, 4, 5, 6, 7, 8, 9, 18))
            .build();

    private DFDConfidentialityAnalysis buildAnalysis(String name) {
        var dataFlowDiagramPath = name + ".dataflowdiagram";
        var dataDictionaryPath = name + ".datadictionary";

        return new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(dataFlowDiagramPath)
                .useDataDictionary(dataDictionaryPath)
                .useTransposeFlowGraphFinder(DFDCyclicTransposeFlowGraphFinder.class)
                .build();
    }

    private void performAnalysis(String model, int variant, Set<Integer> violationsSet) {
        var analysis = buildAnalysis(model);
        analysis.initializeAnalysis();
        var flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();

        assertFalse(flowGraph.getTransposeFlowGraphs()
                .isEmpty());
        Map<Integer, List<AbstractTransposeFlowGraph>> violatingTransposeFlowGraphs = new HashMap<>();
        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {

            hasLoggingServer(transposeFlowGraph, violatingTransposeFlowGraphs);

            hasSecretManager(transposeFlowGraph, violatingTransposeFlowGraphs);

            for (var vertex : transposeFlowGraph.getVertices()) {
                hasGateway(violationsSet, variant, vertex);

                hasAuthenticatedRerquest(violationsSet, variant, vertex);

                hasOptionalAuthorizedEntrypoint(violationsSet, variant, vertex, flowGraph.getTransposeFlowGraphs());

                hasTransformedEntryIdentity(violationsSet, variant, vertex);

                hasTokenValidation(violationsSet, variant, vertex);

                hasLoginAttemptsRegulation(violationsSet, variant, vertex);

                hasEncryptedEntryConnection(violationsSet, variant, vertex);

                hasEncrytedInternalConnection(violationsSet, variant, vertex);

                hasLocalLogging(violationsSet, variant, vertex);

                hasLogSanitization(violationsSet, variant, vertex);

                hasOptionalMessageBroker(violationsSet, variant, vertex, flowGraph.getTransposeFlowGraphs());

            }
        }
        checkCrossTransposeFlowGraphViolations(flowGraph, violatingTransposeFlowGraphs, violationsSet);

    }

    @Test
    void testConstraints() {
        for (var model : TUHH_MODELS.keySet()) {
            for (int variant : TUHH_MODELS.get(model)) {
                // Skip largest models to make build pipeline faster
                if (model.equals("sqshq") && Set.of(10, 11, 12, 18)
                        .contains(variant))
                    continue;

                Set<Integer> violationSet = new TreeSet<Integer>();
                String variationName = model + "_" + variant;
                performAnalysis(Paths.get(location, model, variationName)
                        .toString(), variant, violationSet);
                logger.info("Variant: " + variationName);
                logger.info("Violations: " + violationSet);
                assertFalse(violationSet.contains(variant));
            }
        }
    }

    @Test
    void testDetectCycles() {
        var model = Paths.get(location, "anilallewar", "anilallewar_0")
                .toString();

        var analysis = buildAnalysis(model);
        analysis.initializeAnalysis();
        var flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();

        assertTrue(flowGraph.wasCyclic());

    }

    @Test
    void testRamCopyIssue() {
        var model = Paths.get(location, "sqshq", "sqshq_18")
                .toString();
        Set<Integer> numbers = new HashSet<>();

        for (int i = 0; i < 15; i++) {
            var analysis = buildAnalysis(model);
            analysis.initializeAnalysis();
            var flowGraph = analysis.findFlowGraphs();
            flowGraph.evaluate();
            var numVertex = 0;
            for (var tfg : flowGraph.getTransposeFlowGraphs()) {
                numVertex += tfg.getVertices()
                        .size();
            }
            numbers.add(numVertex);

            logger.info("Num TFGS: " + flowGraph.getTransposeFlowGraphs()
                    .size() + " resulting in numVertex:" + numbers);

            assertEquals(numbers.size(), 1);
        }
    }

    @Test
    void caseStudyConsistencyCheck() {
        var model = Paths.get(location, "georgwittberger", "georgwittberger_2")
                .toString();

        var analysis = buildAnalysis(model);
        analysis.initializeAnalysis();
        var flowGraph = analysis.findFlowGraphs();
        List<List<String>> list = new ArrayList<>();

        for (var tfg : flowGraph.getTransposeFlowGraphs()) {
            var innerList = new ArrayList<String>();
            for (var vertex : tfg.getVertices()) {
                var dfdVertex = (DFDVertex) vertex;
                innerList.add(dfdVertex.getName());

            }
            list.add(innerList);
        }
        List<List<String>> compareList = List.of(List.of("user", "apache_server", "content_service"),
                List.of("user", "apache_server", "cart_service", "product_service"), List.of("user", "apache_server", "product_service"));

        assertEquals(list, compareList);
    }

    private void checkCrossTransposeFlowGraphViolations(FlowGraphCollection flowGraph,
            Map<Integer, List<AbstractTransposeFlowGraph>> violatingTransposeFlowGraphs, Set<Integer> violationsSet) {
        var numOfTransposeFlowGraphs = flowGraph.getTransposeFlowGraphs()
                .size();
        missingLoggingServer(violatingTransposeFlowGraphs, violationsSet, numOfTransposeFlowGraphs);

        missingSecretManager(violatingTransposeFlowGraphs, violationsSet, numOfTransposeFlowGraphs);
    }

    /**
     * If all TransposeFlowGraphs violate the constraint, the violation is valid --> if one TransposeFlowGraph has a secret
     * manager constraint is not triggered
     */
    private void missingSecretManager(Map<Integer, List<AbstractTransposeFlowGraph>> violatingTransposeFlowGraphs, Set<Integer> violationsSet,
            int numOfTransposeFlowGraphs) {
        if (violatingTransposeFlowGraphs.get(18)
                .size() >= numOfTransposeFlowGraphs) {
            violationsSet.add(18);
        }
    }

    /**
     * If all TransposeFlowGraphs violate the constraint, the violation is valid --> if one TransposeFlowGraph has a logging
     * server constraint is not triggered
     */
    private void missingLoggingServer(Map<Integer, List<AbstractTransposeFlowGraph>> violatingTransposeFlowGraphs, Set<Integer> violationsSet,
            int numOfTransposeFlowGraphs) {
        if (violatingTransposeFlowGraphs.get(9)
                .size() >= numOfTransposeFlowGraphs) {
            violationsSet.add(9);
            violationsSet.add(12);
        }
    }

    private boolean hasNodeWithCharacteristic(AbstractTransposeFlowGraph transposeFlowGraph, String constraintRuleType, String constraintRule) {
        return transposeFlowGraph.stream()
                .anyMatch(node -> hasNodeCharacteristic(node, constraintRuleType, constraintRule));
    }

    private boolean checkDataCharacteristicImplication(AbstractVertex<?> node, String constraintPrequisiteType, String constraintPrequisite,
            String constraintRuleType, String constraintRule) {
        var dataCharecteristicsByVariableOfPrequisitType = node.getDataCharacteristicNamesMap(constraintPrequisiteType);
        var dataCharecteristicsByVariableOfRuleType = node.getDataCharacteristicMap(constraintRuleType);

        for (var variable : dataCharecteristicsByVariableOfPrequisitType.keySet()) {
            if (dataCharecteristicsByVariableOfPrequisitType.get(variable)
                    .stream()
                    .anyMatch(n -> n.equals(constraintPrequisite))) {
                if (!dataCharecteristicsByVariableOfRuleType.get(variable)
                        .stream()
                        .anyMatch(n -> n.getValueName()
                                .equals(constraintRule)))
                    return true;
            }
        }

        return false;
    }

    /**
     * An API Gateway or similar facade should exist as a single entry point to the system and perform authorization and
     * authentication of external requests to avoid external entities directly accessing services.
     */
    private void hasGateway(Set<Integer> violationsSet, int variant, AbstractVertex<?> node) {
        if ((hasNodeCharacteristic(node, "Stereotype", "internal")
                && checkDataCharacteristicImplication(node, "Stereotype", "entrypoint", "Stereotype", "gateway"))
                || (hasNodeCharacteristic(node, "Stereotype", "gateway") && hasNodeCharacteristic(node, "Stereotype", "internal"))) {
            violationsSet.add(1);

        }
    }

    /**
     * Services should mutually authenticate and authorize requests from other services.
     */
    private void hasAuthenticatedRerquest(Set<Integer> violationsSet, int variant, AbstractVertex<?> node) {
        if (checkDataCharacteristicImplication(node, "Stereotype", "internal", "Stereotype", "authenticated_request")) {
            violationsSet.add(2);
        }
    }

    /**
     * Authorization and authentication processes should be decoupled from other services and should be implemented at
     * platform level to enable reuse by different services. Since 3 is parent of 6, we need to note the violation 6 as well
     */
    private void hasOptionalAuthorizedEntrypoint(Set<Integer> violationsSet, int variant, AbstractVertex<?> node,
            List<? extends AbstractTransposeFlowGraph> list) {
        if (hasNodeCharacteristic(node, "Stereotype", "internal")
                && checkDataCharacteristicImplication(node, "Stereotype", "entrypoint", "Stereotype", "authorization_server")) {

            var filteredTFGS = list.stream()
                    .filter(tfg -> tfg.getVertices()
                            .stream()
                            .anyMatch(vertex -> vertex.getReferencedElement()
                                    .equals(node.getReferencedElement())))
                    .collect(Collectors.toList());
            if (filteredTFGS.stream()
                    .anyMatch(tfg -> tfg.getVertices()
                            .stream()
                            .anyMatch(vertex -> hasDataCharacteristicInAnyVariable(vertex, "Stereotype", "authorization_server")))) {
                return;
            }

            violationsSet.add(3);
            violationsSet.add(6);

        }
    }

    /**
     * All the external entity identity representations should be transformed into an extendable internal identity
     * representation. The internal identity representations should be secured with signatures and propagated but not
     * exposed outside. They should be used for authentication and authorization at all levels.
     */
    private void hasTransformedEntryIdentity(Set<Integer> violationsSet, int variant, AbstractVertex<?> node) {
        if (hasNodeCharacteristic(node, "Stereotype", "internal")
                && checkDataCharacteristicImplication(node, "Stereotype", "entrypoint", "Stereotype", "transform_identity_representation")) {
            violationsSet.add(4);
        }
    }

    /**
     * Authentication tokens should be validated
     */
    private void hasTokenValidation(Set<Integer> violationsSet, int variant, AbstractVertex<?> node) {
        if (hasDataCharacteristicInAnyVariable(node, "Stereotype", "entrypoint") && hasNodeCharacteristic(node, "Stereotype", "internal")
                && !hasNodeCharacteristic(node, "Stereotype", "token_validation")) {
            violationsSet.add(5);
        }
    }

    /**
     * A limit for the maximum number of login attempts before preventive measures are taken should exist.
     */
    private void hasLoginAttemptsRegulation(Set<Integer> violationsSet, int variant, AbstractVertex<?> node) {
        if (hasNodeCharacteristic(node, "Stereotype", "authorization_server")
                && !hasNodeCharacteristic(node, "Stereotype", "login_attempts_regulation")) {
            violationsSet.add(6);

        }
    }

    /**
     * All communication traffic from external users and entities should be encrypted using secure communication protocols
     */
    private void hasEncryptedEntryConnection(Set<Integer> violationsSet, int variant, AbstractVertex<?> node) {
        if (checkDataCharacteristicImplication(node, "Stereotype", "entrypoint", "Stereotype", "encrypted_connection")) {
            violationsSet.add(7);
        }
    }

    /**
     * All communication between the services should be encrypted using secure communication protocols.
     */
    private void hasEncrytedInternalConnection(Set<Integer> violationsSet, int variant, AbstractVertex<?> node) {
        if (checkDataCharacteristicImplication(node, "Stereotype", "internal", "Stereotype", "encrypted_connection")) {
            violationsSet.add(8);
        }
    }

    /**
     * A central logging subsystem which includes a monitoring dashboard should exist.
     */
    private void hasLoggingServer(AbstractTransposeFlowGraph aTFG, Map<Integer, List<AbstractTransposeFlowGraph>> existenceViolations) {
        existenceViolations.putIfAbsent(9, new ArrayList<AbstractTransposeFlowGraph>());
        if (!hasNodeWithCharacteristic(aTFG, "Stereotype", "logging_server")) {
            existenceViolations.get(9)
                    .add(aTFG);
        }
    }

    /**
     * For all microservices, there should exist a local logging agent decoupled from the microservice but deployed on the
     * same host. Log data from microservices should not be send to the central logging system directly, but collected by
     * the logging agent, written to a local file, and eventually send to the central system by it. Since 10 is parent of
     * 11, we need to note the violation 11 as well
     */
    private void hasLocalLogging(Set<Integer> violationsSet, int variant, AbstractVertex<?> node) {
        if (hasNodeCharacteristic(node, "Stereotype", "internal") && !hasNodeCharacteristic(node, "Stereotype", "local_logging")) {
            violationsSet.add(10);
            violationsSet.add(11);
        }
    }

    /**
     * The local logging agent should sanitize the log data and remove any PII, passwords, API keys, etc.
     */
    private void hasLogSanitization(Set<Integer> violationsSet, int variant, AbstractVertex<?> node) {
        if (hasNodeCharacteristic(node, "Stereotype", "local_logging") && !hasNodeCharacteristic(node, "Stereotype", "log_sanitization")) {
            violationsSet.add(11);
        }
    }

    /**
     * A message broker should be used to realize the communication between local logging agent and central logging system.
     * These two should use mutual authentication and encrypt all transmitted data and availability should be ensured by
     * providing periodic health and status data.
     */
    private void hasOptionalMessageBroker(Set<Integer> violationsSet, int variant, AbstractVertex<?> node,
            List<? extends AbstractTransposeFlowGraph> list) {
        if (hasNodeCharacteristic(node, "Stereotype", "logging_server")
                && !hasDataCharacteristicInAnyVariable(node, "Stereotype", "message_broker")) {

            var filteredTFGS = list.stream()
                    .filter(tfg -> tfg.getVertices()
                            .stream()
                            .anyMatch(vertex -> vertex.getReferencedElement()
                                    .equals(node.getReferencedElement())))
                    .collect(Collectors.toList());
            if (filteredTFGS.stream()
                    .anyMatch(tfg -> tfg.getVertices()
                            .stream()
                            .anyMatch(vertex -> hasDataCharacteristicInAnyVariable(vertex, "Stereotype", "message_broker")))) {
                return;
            }
            violationsSet.add(12);
        }
    }

    /**
     * Secrets should be managed centrally following the Secret as a Service principle.
     */
    private void hasSecretManager(AbstractTransposeFlowGraph transposeFlowGraph,
            Map<Integer, List<AbstractTransposeFlowGraph>> violatingTransposeFlowGraphs) {
        violatingTransposeFlowGraphs.putIfAbsent(18, new ArrayList<AbstractTransposeFlowGraph>());
        if (!hasNodeWithCharacteristic(transposeFlowGraph, "Stereotype", "secret_manager")) {
            violatingTransposeFlowGraphs.get(18)
                    .add(transposeFlowGraph);
        }
    }

    private boolean hasNodeCharacteristic(AbstractVertex<?> node, String type, String value) {
        return node.getAllVertexCharacteristics()
                .stream()
                .anyMatch(n -> n.getTypeName()
                        .equals(type)
                        && n.getValueName()
                                .equals(value));
    }

    /**
     * checks if a node as a data Characteristic in any of the variables
     */
    private boolean hasDataCharacteristicInAnyVariable(AbstractVertex<?> node, String type, String value) {
        return node.getAllDataCharacteristics()
                .stream()
                .anyMatch(v -> v.getAllCharacteristics()
                        .stream()
                        .anyMatch(c -> c.getTypeName()
                                .equals(type)
                                && c.getValueName()
                                        .equals(value)));
    }
}
