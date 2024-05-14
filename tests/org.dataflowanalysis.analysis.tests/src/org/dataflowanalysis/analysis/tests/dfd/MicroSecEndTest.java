package org.dataflowanalysis.analysis.tests.dfd;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.converter.DataFlowDiagramConverter;
import org.dataflowanalysis.analysis.converter.MicroSecEndConverter;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.core.DFDFlowGraphCollection;
import org.dataflowanalysis.analysis.tests.Activator;
import org.junit.jupiter.api.Test;

import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;

public class MicroSecEndTest {
    public static String PROJECT_NAME = "org.dataflowanalysis.analysis.tests";
    private DFDFlowGraphCollection flowGraph;
    private DFDConfidentialityAnalysis analysis;
    private Map<Integer, Map<Integer, List<AbstractVertex<?>>>> violationsMap;
    private String location = "anilallewar";

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
        for (var aTFG : flowGraph.getTransposeFlowGraphs()) {
            //rule 18 needs to check if Secret_manager is the sink --> needs to be done outside of query
            if (!aTFG.getSink().toString().contains("secret_manager")) {
                for (var node : aTFG.getVertices()) {
                    addToMap(violationsMap, variant, 18, node);
                }
            }
            analysis.queryDataFlow(aTFG, node -> {
                var violation = false;
                if ((hasNodeCharacteristic(node, "Stereotype", "internal") && hasDataCharacteristic(node, "Stereotype", "entrypoint")
                        && !(hasDataCharacteristic(node, "Stereotype", "gateway")))
                        || (hasNodeCharacteristic(node, "Stereotype", "gateway") && hasDataCharacteristic(node, "Stereotype", "internal"))
                        || (hasNodeCharacteristic(node, "Stereotype", "configuration_server")
                                && hasDataCharacteristic(node, "Stereotype", "internal"))) {
                    addToMap(violationsMap, variant, 1, node);
                    violation = true;
                }

                if (hasNodeCharacteristic(node, "Stereotype", "internal") 
                        && !hasDataCharacteristic(node, "Stereotype", "authenticated_request")) {
                    addToMap(violationsMap, variant, 2, node);
                    violation = true;
                }

                if ((hasNodeCharacteristic(node, "Stereotype", "authorization_server")
                        && hasNodeCharacteristic(node, "Stereotype", "authenticated_request"))
                        || (!hasDataCharacteristic(node, "Stereotype", "authorization_server")
                                && hasDataCharacteristic(node, "Stereotype", "internal"))) {
                    addToMap(violationsMap, variant, 3, node);
                    violation = true;
                }

                if (hasDataCharacteristic(node, "Stereotype", "internal")
                        && !hasDataCharacteristic(node, "Stereotype", "transform_identity_representation")) {
                    addToMap(violationsMap, variant, 4, node);
                    violation = true;
                }

                if (hasDataCharacteristic(node, "Stereotype", "internal") && !hasDataCharacteristic(node, "Stereotype", "token_validation")) {
                    addToMap(violationsMap, variant, 5, node);
                    violation = true;
                }

                if ((hasNodeCharacteristic(node, "Stereotype", "authorization_server")
                        && !hasNodeCharacteristic(node, "Stereotype", "login_attempts_regulation"))
                        || (!hasDataCharacteristic(node, "Stereotype", "authorization_server")
                                && hasDataCharacteristic(node, "Stereotype", "internal"))) {
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

                // currently unclear to find a solution here --> solution covered in 10
                /*if (hasNodeCharacteristic(node, "Stereotype", "logging_server")) {
                    addToMap(violationsMap, variant, 9, node);
                    violation = true;
                }*/

                if (hasNodeCharacteristic(node, "Stereotype", "internal") && !hasNodeCharacteristic(node, "Stereotype", "local_logging")) {
                    addToMap(violationsMap, variant, 10, node);
                    violation = true;
                }
                //add no local_logging
                if ((hasNodeCharacteristic(node, "Stereotype", "local_logging") && !hasNodeCharacteristic(node, "Stereotype", "log_sanitization"))
                        || (hasDataCharacteristic(node, "Stereotype", "internal")&&!hasNodeCharacteristic(node, "Stereotype", "local_logging"))) {
                    addToMap(violationsMap, variant, 11, node);
                    violation = true;
                }

                if ((hasNodeCharacteristic(node, "Stereotype", "logging_server") && !hasDataCharacteristic(node, "Stereotype", "message_broker"))
                    || (hasDataCharacteristic(node, "Stereotype", "internal")&&!hasNodeCharacteristic(node, "Stereotype", "local_logging"))) {
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
            System.out.println("");
            assertFalse(violationsMap.get(variant)
            .keySet()
            .contains(variant));
        }
    }

    @Test
    public void convertAllToWeb() throws StandaloneInitializationException {
        List<String> models = getModelNames(location);
        for (String model : models) {
            System.out.println(model);
            var converter = new DataFlowDiagramConverter();
            var web = converter.dfdToWeb(PROJECT_NAME, Paths.get(location, model + ".dataflowdiagram")
                    .toString(),
                    Paths.get(location, model + ".datadictionary")
                            .toString(),
                    Activator.class);
            converter.storeWeb(web, Paths.get(location, model + ".json")
                    .toString());
        }
    }

    @Test
    public void convertAllToDFD() {
        List<String> models = getModelNames(location);
        for (String model : models) {
            System.out.println(model);
            var converter = new DataFlowDiagramConverter();
            var dfd = converter.webToDfd(Paths.get(location, model + ".json")
                    .toString());
            converter.storeDFD(dfd, Paths.get(location, model)
                    .toString());
        }
    }

    @Test
    public void initialConvertAllToDFD() {
        List<String> models = getModelNames(location);
        for (String model : models) {
            System.out.println(model);
            var converter = new MicroSecEndConverter();
            var dfd = converter.microToDfd(Paths.get(location, model + ".json")
                    .toString());
            converter.storeDFD(dfd, Paths.get(location, model)
                    .toString());
        }
    }

    @Test
    public void initializeConvertion() {
        initialConvertAllToDFD();
        try {
            convertAllToWeb();
        } catch (Exception e) {
            return;
        }
        convertAllToDFD();
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
