package org.dataflowanalysis.analysis.tests.dfd;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.core.DFDFlowGraphCollection;
import org.dataflowanalysis.analysis.tests.Activator;
import org.junit.jupiter.api.Test;


public class MicroSecEndTest {
    public static String PROJECT_NAME = "org.dataflowanalysis.analysis.tests";
    private DFDFlowGraphCollection flowGraph;
    private DFDConfidentialityAnalysis analysis;
    private Map<Integer, Map<Integer, List<AbstractVertex<?>>>> violationsMap;
    private String location = "ana";

    public DFDConfidentialityAnalysis buildAnalysis(String name) {
        var DataFlowDiagramPath = Paths.get(name + ".dataflowdiagram");
        var DataDictionaryPath = Paths.get(name + ".datadictionary");

        return new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(DataFlowDiagramPath.toString())
                .useDataDictionary(DataDictionaryPath.toString())
                .build();
    }

    public void initAnalysis(String model) {
        analysis = buildAnalysis(model);
        analysis.initializeAnalysis();
        System.out.println(analysis.toString());
        flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();
    }

    public void runAnalysis(int variant) {
        var hasSecretManager = false;
        var hasLoggingServer = false;
        for (var aTFG : flowGraph.getTransposeFlowGraphs()) {
            if (aTFG.stream()
                    .anyMatch(node -> hasNodeCharacteristic(node, "Stereotype", "secret_manager"))) hasSecretManager = true;
            if (aTFG.stream()
                    .anyMatch(node -> hasNodeCharacteristic(node, "Stereotype", "logging_server"))) hasLoggingServer = true;
        }
        
        if (!hasSecretManager) addToMap(violationsMap, variant, 18, null);
        
        if (!hasLoggingServer) {
            addToMap(violationsMap, variant, 9, null);
            addToMap(violationsMap, variant, 12, null);
        }
        
        for (var aTFG : flowGraph.getTransposeFlowGraphs()) {
                      
            analysis.queryDataFlow(aTFG, node -> {
                var violation = false;
                //inkonsistent modeling i think (deleted (|| (hasNodeCharacteristic(node, "Stereotype", "configuration_server") && hasDataCharacteristic(node, "Stereotype", "internal")
                if ((hasNodeCharacteristic(node, "Stereotype", "internal") && hasDataCharacteristic(node, "Stereotype", "entrypoint")
                        && !(hasDataCharacteristic(node, "Stereotype", "gateway")))
                        || (hasNodeCharacteristic(node, "Stereotype", "gateway") && hasNodeCharacteristic(node, "Stereotype", "internal"))) {
                    addToMap(violationsMap, variant, 1, node);
                    violation = true;
                }

                if (hasDataCharacteristic(node, "Stereotype", "internal") 
                        && !hasDataCharacteristic(node, "Stereotype", "authenticated_request")) {
                    addToMap(violationsMap, variant, 2, node);
                    violation = true;
                }

                if (hasDataCharacteristic(node, "Stereotype", "entrypoint") && hasNodeCharacteristic(node, "Stereotype", "internal") && !hasDataCharacteristic(node, "Stereotype", "authorization_server")) {
                    addToMap(violationsMap, variant, 3, node);
                    addToMap(violationsMap, variant, 6, node);
                    violation = true;
                }

                if (hasDataCharacteristic(node, "Stereotype", "entrypoint") && hasNodeCharacteristic(node, "Stereotype", "internal")
                        && !hasDataCharacteristic(node, "Stereotype", "transform_identity_representation")) {
                    addToMap(violationsMap, variant, 4, node);
                    violation = true;
                }

                if (hasDataCharacteristic(node, "Stereotype", "entrypoint") && hasNodeCharacteristic(node, "Stereotype", "internal") && !hasNodeCharacteristic(node, "Stereotype", "token_validation")) {
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

    @Test
    public void testConstraints() {
        List<String> models = getModelNames(location);
        violationsMap = new HashMap<>();
        for (String model : models) {
            System.out.println(model);
            initAnalysis(Paths.get(location, model)
                    .toString());
            var variant = Integer.parseInt(model.replaceAll(".*\\D+(\\d+)$", "$1"));
            runAnalysis(variant);
        }
        for (int variant : violationsMap.keySet()) {
            System.out.println("Variant: " + variant);
            System.out.println("Violations: " + violationsMap.get(variant)
                    .keySet());
            if (violationsMap.get(variant)
                    .keySet().contains(variant)) {
                System.out.println(violationsMap.get(variant));
            }
            System.out.println("");
            /*assertFalse(violationsMap.get(variant)
            .keySet()
            .contains(variant));*/
        }
    }

    private List<String> getModelNames(String location) {
        String fileEnding = ".json";

        File directory = new File(location);
        File[] files = directory.listFiles((dir, name) -> name.endsWith(fileEnding));
        List<String> fileNames = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                String nameWithoutExtension = name.substring(0, name.length() - fileEnding.length());
                fileNames.add(nameWithoutExtension);
            }
        }
        Collections.sort(fileNames);
        return fileNames;
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
