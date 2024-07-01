package org.dataflowanalysis.analysis.tests.dfd;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.examplemodels.Activator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.collect.ImmutableMap;

public class MicroSecEndTest {
    public static final String PROJECT_NAME = "org.dataflowanalysis.examplemodels";
    public static final String location = Paths.get("casestudies", "TUHH-Models")
            .toString();

    public static final Map<String, List<Integer>> TUHH_MODELS = ImmutableMap.<String, List<Integer>>builder()
            .put("anilallewar", List.of(0, 7, 8, 9, 11, 12, 18))
            .put("apssouza22", List.of(0, 2, 4, 6, 7, 8, 12, 18))
            .put("callistaenterprise", List.of(0, 2, 4, 6, 11, 18))
            .put("ewolff", List.of(5, 10, 12, 18))
            .put("ewolff-kafka", List.of(0, 3, 4, 5, 6, 7, 8, 9, 18))
            .put("fernandoabcampos", List.of(18))
            .put("georgwittberger", List.of(0, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 18))
            .put("jferrater", List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 18))
            .put("koushikkothagal", List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 18))
            .put("mdeket", List.of(5))
            .put("mudigal-technologies", List.of(0, 2, 4, 5, 7, 8, 11, 18))
            .put("rohitghatol", List.of(10, 12, 18))
            .put("spring-petclinic", List.of(0, 2, 3, 4, 5, 6, 7, 8, 9, 18))
            .put("sqshq", List.of(0, 7, 8, 9, 10, 11, 12, 18))
            .put("yidongnan", List.of(0, 2, 3, 4, 5, 6, 7, 8, 9, 18))
            .build();
    
    public static Stream<Arguments> provideTUHHModels() {
        return TUHH_MODELS.entrySet().stream().map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
    }

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


    public void analysis(String model,int variant,Map<Integer, Map<Integer, List<AbstractVertex<?>>>> violationsMap) {
        var analysis = buildAnalysis(model);
        analysis.initializeAnalysis();
        System.out.println(analysis.toString());
        var flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();
        
        assertFalse(flowGraph.getTransposeFlowGraphs()
                .isEmpty());

        var hasSecretManager = false;
        var hasLoggingServer = false;
        for (var aTFG : flowGraph.getTransposeFlowGraphs()) {
            if (aTFG.stream()
                    .anyMatch(node -> hasNodeCharacteristic(node, "Stereotype", "secret_manager")))
                hasSecretManager = true;
            if (aTFG.stream()
                    .anyMatch(node -> hasNodeCharacteristic(node, "Stereotype", "logging_server")))
                hasLoggingServer = true;
        }

        if (!hasSecretManager)
            addToMap(violationsMap, variant, 18, null);

        if (!hasLoggingServer) {
            addToMap(violationsMap, variant, 9, null);
            addToMap(violationsMap, variant, 12, null);
        }

        for (var aTFG : flowGraph.getTransposeFlowGraphs()) {
            analysis.queryDataFlow(aTFG, node -> {
                var violation = false;
                if ((hasNodeCharacteristic(node, "Stereotype", "internal") && hasDataCharacteristic(node, "Stereotype", "entrypoint")
                        && !(hasDataCharacteristic(node, "Stereotype", "gateway")))
                        || (hasNodeCharacteristic(node, "Stereotype", "gateway") && hasNodeCharacteristic(node, "Stereotype", "internal"))) {
                    addToMap(violationsMap, variant, 1, node);
                    violation = true;
                }

                if (hasDataCharacteristic(node, "Stereotype", "internal") && !hasDataCharacteristic(node, "Stereotype", "authenticated_request")) {
                    addToMap(violationsMap, variant, 2, node);
                    violation = true;
                }

                if (hasDataCharacteristic(node, "Stereotype", "entrypoint") && hasNodeCharacteristic(node, "Stereotype", "internal")
                        && !hasDataCharacteristic(node, "Stereotype", "authorization_server")) {
                    addToMap(violationsMap, variant, 3, node);
                    addToMap(violationsMap, variant, 6, node);
                    violation = true;
                }

                if (hasDataCharacteristic(node, "Stereotype", "entrypoint") && hasNodeCharacteristic(node, "Stereotype", "internal")
                        && !hasDataCharacteristic(node, "Stereotype", "transform_identity_representation")) {
                    addToMap(violationsMap, variant, 4, node);
                    violation = true;
                }

                if (hasDataCharacteristic(node, "Stereotype", "entrypoint") && hasNodeCharacteristic(node, "Stereotype", "internal")
                        && !hasNodeCharacteristic(node, "Stereotype", "token_validation")) {
                    addToMap(violationsMap, variant, 5, node);
                    violation = true;
                }

                if (hasNodeCharacteristic(node, "Stereotype", "authorization_server")
                        && !hasNodeCharacteristic(node, "Stereotype", "login_attempts_regulation")) {
                    addToMap(violationsMap, variant, 6, node);
                    violation = true;
                }

                if (hasDataCharacteristic(node, "Stereotype", "entrypoint") && !hasDataCharacteristic(node, "Stereotype", "encrypted_connection")) {
                    addToMap(violationsMap, variant, 7, node);
                    violation = true;
                }

                if (hasDataCharacteristic(node, "Stereotype", "internal") && !hasDataCharacteristic(node, "Stereotype", "encrypted_connection")) {
                    addToMap(violationsMap, variant, 8, node);
                    violation = true;
                }

                if (hasNodeCharacteristic(node, "Stereotype", "internal") && !hasNodeCharacteristic(node, "Stereotype", "local_logging")) {
                    addToMap(violationsMap, variant, 10, node);
                    addToMap(violationsMap, variant, 11, node);
                    violation = true;
                }

                if (hasNodeCharacteristic(node, "Stereotype", "local_logging") && !hasNodeCharacteristic(node, "Stereotype", "log_sanitization")) {
                    addToMap(violationsMap, variant, 11, node);
                    violation = true;
                }

                if (hasNodeCharacteristic(node, "Stereotype", "logging_server") && !hasDataCharacteristic(node, "Stereotype", "message_broker")) {
                    addToMap(violationsMap, variant, 12, node);
                    violation = true;
                }
                return violation;
            });
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideTUHHModels")
    void testConstraints(String model, List<Integer> variants) {
        Map<Integer, Map<Integer, List<AbstractVertex<?>>>> violationsMap = new HashMap<>();
        for (int variant : variants) {
            String variationName = model + "_" + variant;
            System.out.println(variationName);
            analysis(Paths.get(location, model, variationName)
                    .toString(),variant,violationsMap);
        }
        for (int variant : violationsMap.keySet()) {
            System.out.println("Variant: " + variant);
            System.out.println("Violations: " + violationsMap.get(variant)
                    .keySet());
            assertFalse(violationsMap.get(variant)
                    .keySet()
                    .contains(variant));
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

    private boolean hasDataCharacteristic(AbstractVertex<?> node, String type, String value) {
        return node.getAllDataCharacteristics()
                .stream()
                .anyMatch(v -> v.getAllCharacteristics()
                        .stream()
                        .anyMatch(c -> c.getTypeName()
                                .equals(type)
                                && c.getValueName()
                                        .equals(value)));
    }

    private void addToMap(Map<Integer, Map<Integer, List<AbstractVertex<?>>>> map, int variant, int rule, AbstractVertex<?> node) {
        map.putIfAbsent(variant, new HashMap<>());

        Map<Integer, List<AbstractVertex<?>>> secondaryMap = map.get(variant);

        if (!secondaryMap.containsKey(rule)) {
            List<AbstractVertex<?>> list = new ArrayList<>();
            list.add(node);
            secondaryMap.put(rule, list);
        } else {
            secondaryMap.get(rule)
                    .add(node);
        }
    }
}
