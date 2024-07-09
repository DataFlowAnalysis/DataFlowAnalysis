package org.dataflowanalysis.analysis.tests.dfd;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.examplemodels.Activator;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

public class MicroSecEndTest {
    public static final String PROJECT_NAME = "org.dataflowanalysis.examplemodels";
    public static final String location = Paths.get("casestudies", "TUHH-Models")
            .toString();

    public static final Map<String, List<Integer>> TUHH_MODELS = ImmutableMap.<String, List<Integer>>builder()
            .put("anilallewar", List.of(0, 7, 8, 9, 11, 12, 18))
            .put("apssouza22", List.of(0, 2, 4, 6, 7, 8, 12, 18))
            .put("callistaenterprise", List.of(0, 2, 11, 18)) // 4,6 Faulty multiple flows from entrypoint to internal without
            //                                                  // transformed_identity/auth_server
            .put("ewolff", List.of(5, 10, 12, 18))
            .put("ewolff-kafka", List.of(0, 3, 4, 5, 6, 7, 8, 9, 18))
            .put("fernandoabcampos", List.of(18))
            .put("georgwittberger", List.of(0, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 18))
            .put("jferrater", List.of(0, 2, 3, 5, 6, 7, 8, 9, 18)) // 1,4 Faulty due to direct flow from entrypoint to internal without
            //                                                       // gateway/transformed_identity
            .put("koushikkothagal", List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 18))
            .put("mdeket", List.of(5))
            .put("mudigal-technologies", List.of(0, 2, 4, 5, 7, 8, 11, 18))
            .put("rohitghatol", List.of(10, 12, 18))
            .put("spring-petclinic", List.of(0, 2, 3, 5, 6, 7, 8, 9, 18)) // 4 Faulty direct flow from entrypoint to internal without
                                                                          // transformed_identity
            .put("sqshq", List.of(0, 7, 8, 9, 10, 11, 12, 18))
            .put("yidongnan", List.of(0, 2, 3, 4, 5, 6, 7, 8, 9, 18))
            .build();

    public DFDConfidentialityAnalysis buildAnalysis(String name) {
        var DataFlowDiagramPath = name + ".dataflowdiagram";
        var DataDictionaryPath = name + ".datadictionary";

        return new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(DataFlowDiagramPath)
                .useDataDictionary(DataDictionaryPath)
                .build();
    }

    public void performAnalysis(String model, int variant, List<Integer> violationsList) {
        var analysis = buildAnalysis(model);
        analysis.initializeAnalysis();
        var flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();

        assertFalse(flowGraph.getTransposeFlowGraphs()
                .isEmpty());

        Map<Integer, List<AbstractTransposeFlowGraph>> existenceViolations = new HashMap<>();

        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {

            hasLoggingServer(transposeFlowGraph, existenceViolations);

            hasSecretManager(transposeFlowGraph, existenceViolations);

            for (var vertex : transposeFlowGraph.getVertices()) {

                hasGateway(violationsList, variant, vertex);

                hasAuthenticatedRerquest(violationsList, variant, vertex);

                hasAuthorizedEntrypoint(violationsList, variant, vertex);

                hasTransformedEntryIdentity(violationsList, variant, vertex);

                hasTokenValidation(violationsList, variant, vertex);

                hasLoginAttemptsRegulation(violationsList, variant, vertex);

                hasEncryptedEntryConnection(violationsList, variant, vertex);

                hasEncrytedInternalConnection(violationsList, variant, vertex);

                hasLocalLogging(violationsList, variant, vertex);

                hasLogSanitization(violationsList, variant, vertex);

                hasMessageBroker(violationsList, variant, vertex);

            }
        }
        var numOfTransposeFlowGraphs = flowGraph.getTransposeFlowGraphs()
                .size();
        if (existenceViolations.get(9)
                .size() >= numOfTransposeFlowGraphs) {
            addToMap(violationsList, 9);
            addToMap(violationsList, 12);
        }

        if (existenceViolations.get(18)
                .size() >= numOfTransposeFlowGraphs) {
            addToMap(violationsList, 18);
        }
    }

    @Test
    void testConstraints() {
        var faultyModels = new ArrayList<String>();
        for (var model : TUHH_MODELS.keySet()) {
            for (int variant : TUHH_MODELS.get(model)) {
                List<Integer> violationList = new ArrayList<Integer>();
                String variationName = model + "_" + variant;
                performAnalysis(Paths.get(location, model, variationName)
                        .toString(), variant, violationList);
                System.out.println("Variant: " + variationName);
                Collections.sort(violationList);
                System.out.println("Violations: " + violationList);
                if (violationList.contains(variant))
                    faultyModels.add(model + "_" + variant);
                // assertFalse(violationList.contains(variant));

            }
        }
        System.out.println(faultyModels);
    }

    private boolean hasNodeWithCharacteristic(AbstractTransposeFlowGraph aTFG, String constraintRuleType, String constraintRule) {
        return aTFG.stream()
                .anyMatch(node -> hasNodeCharacteristic(node, constraintRuleType, constraintRule));
    }

    private boolean hasDataCharecteristicViolation(AbstractVertex<?> node, String constraintPrequisitType, String constraintPrequisit,
            String constraintRuleType, String constraintRule) {
        var DataCharecteristicsByVariableOfPrequisitType = node.getDataCharacteristicNamesMap(constraintPrequisitType);
        var DataCharecteristicsByVariableOfRuleType = node.getDataCharacteristicMap(constraintRuleType);

        for (var variable : DataCharecteristicsByVariableOfPrequisitType.keySet()) {
            if (DataCharecteristicsByVariableOfPrequisitType.get(variable)
                    .stream()
                    .anyMatch(n -> n.equals(constraintPrequisit))) {
                if (!DataCharecteristicsByVariableOfRuleType.get(variable)
                        .stream()
                        .anyMatch(n -> n.getValueName()
                                .equals(constraintRule)))
                    return true;
            }
        }

        return false;
    }

    // An API Gateway or similar facade should exist as a single entry point to the system and perform authorization
    // and authentication of external requests to avoid external entities directly accessing services.
    private void hasGateway(List<Integer> violationsList, int variant, AbstractVertex<?> node) {
        if ((hasNodeCharacteristic(node, "Stereotype", "internal")
                && hasDataCharecteristicViolation(node, "Stereotype", "entrypoint", "Stereotype", "gateway"))
                || (hasNodeCharacteristic(node, "Stereotype", "gateway") && hasNodeCharacteristic(node, "Stereotype", "internal"))) {
            addToMap(violationsList, 1);

        }
    }

    // Services should mutually authenticate and authorize requests from other services.
    private void hasAuthenticatedRerquest(List<Integer> violationsList, int variant, AbstractVertex<?> node) {
        if (hasDataCharecteristicViolation(node, "Stereotype", "internal", "Stereotype", "authenticated_request")) {
            addToMap(violationsList, 2);
        }
    }

    // Authorization and authentication processes should be decoupled from other services and should be implemented
    // at platform level to enable reuse by different services.
    private void hasAuthorizedEntrypoint(List<Integer> violationsList, int variant, AbstractVertex<?> node) {
        if (hasNodeCharacteristic(node, "Stereotype", "internal")
                && hasDataCharecteristicViolation(node, "Stereotype", "entrypoint", "Stereotype", "authorization_server")) {
            addToMap(violationsList, 3);
            addToMap(violationsList, 6);

        }
    }

    // All the external entity identity representations should be transformed into an extendable internal identity
    // representation. The internal identity representations should be secured with signatures and propagated but
    // not exposed outside. They should be used for authentication and authorization at all levels.
    private void hasTransformedEntryIdentity(List<Integer> violationsList, int variant, AbstractVertex<?> node) {
        if (hasNodeCharacteristic(node, "Stereotype", "internal")
                && hasDataCharecteristicViolation(node, "Stereotype", "entrypoint", "Stereotype", "transform_identity_representation")) {
            addToMap(violationsList, 4);
        }
    }

    // Authentication tokens should be validated
    private void hasTokenValidation(List<Integer> violationsList, int variant, AbstractVertex<?> node) {
        if (hasDataCharacteristicAcrossVariables(node, "Stereotype", "entrypoint") && hasNodeCharacteristic(node, "Stereotype", "internal")
                && !hasNodeCharacteristic(node, "Stereotype", "token_validation")) {
            addToMap(violationsList, 5);
        }
    }

    // A limit for the maximum number of login attempts before preventive measures are taken should exist.
    private void hasLoginAttemptsRegulation(List<Integer> violationsList, int variant, AbstractVertex<?> node) {
        if (hasNodeCharacteristic(node, "Stereotype", "authorization_server")
                && !hasNodeCharacteristic(node, "Stereotype", "login_attempts_regulation")) {
            addToMap(violationsList, 6);

        }
    }

    // All communication traffic from external users and entities should be encrypted using secure communication
    // protocols
    private void hasEncryptedEntryConnection(List<Integer> violationsList, int variant, AbstractVertex<?> node) {
        if (hasDataCharecteristicViolation(node, "Stereotype", "entrypoint", "Stereotype", "encrypted_connection")) {
            addToMap(violationsList, 7);
        }
    }

    // All communication between the services should be encrypted using secure communication protocols.
    private void hasEncrytedInternalConnection(List<Integer> violationsList, int variant, AbstractVertex<?> node) {
        if (hasDataCharecteristicViolation(node, "Stereotype", "internal", "Stereotype", "encrypted_connection")) {
            addToMap(violationsList, 8);
        }
    }

    // A central logging subsystem which includes a monitoring dashboard should exist.
    private void hasLoggingServer(AbstractTransposeFlowGraph aTFG, Map<Integer, List<AbstractTransposeFlowGraph>> existenceViolations) {
        existenceViolations.putIfAbsent(9, new ArrayList<AbstractTransposeFlowGraph>());
        if (!hasNodeWithCharacteristic(aTFG, "Stereotype", "logging_server")) {
            existenceViolations.get(9)
                    .add(aTFG);
        }
    }

    // For all microservices, there should exist a local logging agent decoupled from the microservice but deployed
    // on the same host. Log data from microservices should not be send to the central logging system directly, but
    // collected by the logging agent, written to a local file, and eventually send to the central system by it.
    private void hasLocalLogging(List<Integer> violationsList, int variant, AbstractVertex<?> node) {
        if (hasNodeCharacteristic(node, "Stereotype", "internal") && !hasNodeCharacteristic(node, "Stereotype", "local_logging")) {
            addToMap(violationsList, 10);
            addToMap(violationsList, 11);
        }
    }

    // The local logging agent should sanitize the log data and remove any PII, passwords, API keys, etc.
    private void hasLogSanitization(List<Integer> violationsList, int variant, AbstractVertex<?> node) {
        if (hasNodeCharacteristic(node, "Stereotype", "local_logging") && !hasNodeCharacteristic(node, "Stereotype", "log_sanitization")) {
            addToMap(violationsList, 11);
        }
    }

    // A message broker should be used to realize the communication between local logging agent and central logging
    // system. These two should use mutual authentication and encrypt all transmitted data and availability should
    // be ensured by providing periodic health and status data.
    private void hasMessageBroker(List<Integer> violationsList, int variant, AbstractVertex<?> node) {
        if (hasNodeCharacteristic(node, "Stereotype", "logging_server")
                && !hasDataCharacteristicAcrossVariables(node, "Stereotype", "message_broker")) {
            addToMap(violationsList, 12);
        }
    }

    // Secrets should be managed centrally following the Secret as a Service principle.
    private void hasSecretManager(AbstractTransposeFlowGraph aTFG, Map<Integer, List<AbstractTransposeFlowGraph>> existenceViolations) {
        existenceViolations.putIfAbsent(18, new ArrayList<AbstractTransposeFlowGraph>());
        if (!hasNodeWithCharacteristic(aTFG, "Stereotype", "secret_manager")) {
            existenceViolations.get(18)
                    .add(aTFG);
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

    private boolean hasDataCharacteristicAcrossVariables(AbstractVertex<?> node, String type, String value) {
        return node.getAllDataCharacteristics()
                .stream()
                .anyMatch(v -> v.getAllCharacteristics()
                        .stream()
                        .anyMatch(c -> c.getTypeName()
                                .equals(type)
                                && c.getValueName()
                                        .equals(value)));
    }

    private void addToMap(List<Integer> violationsList, int rule) {
        if (!violationsList.contains(rule))
            violationsList.add(rule);
    }
}
