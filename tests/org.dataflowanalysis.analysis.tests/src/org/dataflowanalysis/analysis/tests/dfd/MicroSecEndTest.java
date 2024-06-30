package org.dataflowanalysis.analysis.tests.dfd;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
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
import org.dataflowanalysis.analysis.dfd.resource.DFDURIResourceProvider;
import org.dataflowanalysis.analysis.utils.ResourceUtils;
import org.dataflowanalysis.examplemodels.Activator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;


public class MicroSecEndTest {
    public static String PROJECT_NAME = "org.dataflowanalysis.examplemodels";
    private DFDFlowGraphCollection flowGraph;
    private DFDConfidentialityAnalysis analysis;
    private Map<Integer, Map<Integer, List<AbstractVertex<?>>>> violationsMap;
    private String location = Paths.get("CaseStudies","TUHH-Models").toString();

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
    public void testConstraints() throws StandaloneInitializationException, IOException {
        List<String> models = getModelNames(location);
        violationsMap = new HashMap<>();
        for (String model : models) {
            System.out.println(model);
            initAnalysis(Paths.get(location, model)
                    .toString());
            var variant = Integer.parseInt(model.replaceAll(".*\\D+(\\d+)$", "$1"));
            System.out.println(variant);
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
            assertFalse(violationsMap.get(variant)
            .keySet()
            .contains(variant));
        }
    }

    private List<String> getModelNames(String location) throws StandaloneInitializationException, IOException {
        /*StandaloneInitializerBuilder.builder()
                .registerProjectURI(Activator.class, PROJECT_NAME)
                .build()
                .init();
        
        URI tuhhURI = ResourceUtils.createRelativePluginURI(location+"/koushikkothagal/koushikkothagal_18.json", PROJECT_NAME);
        System.out.println(tuhhURI.toFileString());
        
        String pluginId = "org.dataflowanalysis.examplemodels";
        String relativePath = "CaseStudies/TUHH-Models";
        


        // Get the bundle for the plugin
        Bundle bundle = FrameworkUtil.getBundle(Activator.class) ;
        if (bundle != null) {
            // Get the URL for the specified path within the plugin
            URL url = bundle.getEntry(relativePath);
            try {
                // Resolve the URL to a file system path
                URL fileURL = FileLocator.toFileURL(url);
                File directory = new File(fileURL.toString());

                // Check if the path is a directory
                if (directory.isDirectory()) {
                    // List the contents of the directory
                    File[] files = directory.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            System.out.println(file.getName());
                        }
                    }
                } else {
                    System.out.println("The specified path is not a directory.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Plugin not found: " + pluginId);
        }
        
        
        
        String fileEnding = ".json";

                
        File directory = new File(tuhhURI.toString());
        System.out.println(directory.toString());
        System.out.println(directory.exists());
        //var tuhhDir=Paths.get(tuhhURI.path());
        //System.out.println(Files.list(tuhhDir));
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
        System.out.println(fileNames);
        //var provider = new DFDURIResourceProvider(dfdURI, ddURI);
        //provider.loadRequiredResources();
        //return new DataFlowDiagramAndDictionary(provider.getDataFlowDiagram(), provider.getDataDictionary());*/
        return List.of(
                
                "koushikkothagal/koushikkothagal_18",
                "koushikkothagal/koushikkothagal_6",
                "koushikkothagal/koushikkothagal_7",
                "koushikkothagal/koushikkothagal_12",
                "koushikkothagal/koushikkothagal_0",
                "koushikkothagal/koushikkothagal_1",
                "koushikkothagal/koushikkothagal_2",
                "koushikkothagal/koushikkothagal_10",
                "koushikkothagal/koushikkothagal_11",
                "koushikkothagal/koushikkothagal_3",
                "koushikkothagal/koushikkothagal_8",
                "koushikkothagal/koushikkothagal_4",
                "koushikkothagal/koushikkothagal_5",
                "koushikkothagal/koushikkothagal_9",
                "mudigal-technologies/mudigal-technologies_5",
                "mudigal-technologies/mudigal-technologies_18",
                "mudigal-technologies/mudigal-technologies_4",
                "mudigal-technologies/mudigal-technologies_8",
                "mudigal-technologies/mudigal-technologies_2",
                "mudigal-technologies/mudigal-technologies_11",
                "mudigal-technologies/mudigal-technologies_0",
                "mudigal-technologies/mudigal-technologies_7",
                "sqshq/sqshq_10",
                "sqshq/sqshq_8",
                "sqshq/sqshq_9",
                "sqshq/sqshq_11",
                "sqshq/sqshq_12",
                "sqshq/sqshq_7",
                "sqshq/sqshq_18",
                "sqshq/sqshq_0",
                "ewolff/ewolff_5",
                "ewolff/ewolff_11",
                "ewolff/ewolff_10",
                "ewolff/ewolff_8",
                "ewolff/ewolff_4",
                "ewolff/ewolff_2",
                "ewolff/ewolff_0",
                "ewolff/ewolff_18",
                "ewolff/ewolff_7",
                "ewolff/ewolff_12",
                "fernandoabcampos/fernandoabcampos_2",
                "fernandoabcampos/fernandoabcampos_18",
                "fernandoabcampos/fernandoabcampos_4",
                "fernandoabcampos/fernandoabcampos_8",
                "fernandoabcampos/fernandoabcampos_12",
                "fernandoabcampos/fernandoabcampos_9",
                "fernandoabcampos/fernandoabcampos_5",
                "fernandoabcampos/fernandoabcampos_10",
                "fernandoabcampos/fernandoabcampos_11",
                "fernandoabcampos/fernandoabcampos_7",
                "fernandoabcampos/fernandoabcampos_0",
                "fernandoabcampos/fernandoabcampos_1",
                "rohitghatol/rohitghatol_10",
                "rohitghatol/rohitghatol_12",
                "rohitghatol/rohitghatol_18",
                "mdeket/mdeket_5",
                "anilallewar/anilallewar_8",
                "anilallewar/anilallewar_12",
                "anilallewar/anilallewar_9",
                "anilallewar/anilallewar_18",
                "anilallewar/anilallewar_0",
                "anilallewar/anilallewar_7",
                "anilallewar/anilallewar_11",
                "yidongnan/yidongnan_0",
                "yidongnan/yidongnan_6",
                "yidongnan/yidongnan_18",
                "yidongnan/yidongnan_7",
                "yidongnan/yidongnan_4",
                "yidongnan/yidongnan_8",
                "yidongnan/yidongnan_9",
                "yidongnan/yidongnan_5",
                "yidongnan/yidongnan_2",
                "yidongnan/yidongnan_3",
                "spring-petclinic/spring-petclinic_6",
                "spring-petclinic/spring-petclinic_7",
                "spring-petclinic/spring-petclinic_0",
                "spring-petclinic/spring-petclinic_2",
                "spring-petclinic/spring-petclinic_18",
                "spring-petclinic/spring-petclinic_3",
                "spring-petclinic/spring-petclinic_8",
                "spring-petclinic/spring-petclinic_4",
                "spring-petclinic/spring-petclinic_5",
                "spring-petclinic/spring-petclinic_9",
                "apssouza22/apssouza22_6",
                "apssouza22/apssouza22_7",
                "apssouza22/apssouza22_0",
                "apssouza22/apssouza22_12",
                "apssouza22/apssouza22_2",
                "apssouza22/apssouza22_4",
                "apssouza22/apssouza22_18",
                "apssouza22/apssouza22_8",
                "jferrater/jferrater_5",
                "jferrater/jferrater_9",
                "jferrater/jferrater_8",
                "jferrater/jferrater_4",
                "jferrater/jferrater_3",
                "jferrater/jferrater_11",
                "jferrater/jferrater_10",
                "jferrater/jferrater_2",
                "jferrater/jferrater_1",
                "jferrater/jferrater_0",
                "jferrater/jferrater_12",
                "jferrater/jferrater_7",
                "jferrater/jferrater_18",
                "jferrater/jferrater_6",
                "callistaenterprise/callistaenterprise_18",
                "callistaenterprise/callistaenterprise_2",
                "callistaenterprise/callistaenterprise_4",
                "callistaenterprise/callistaenterprise_6",
                "callistaenterprise/callistaenterprise_11",
                "callistaenterprise/callistaenterprise_0",
                "georgwittberger/georgwittberger_11",
                "georgwittberger/georgwittberger_0",
                "georgwittberger/georgwittberger_10",
                "georgwittberger/georgwittberger_6",
                "georgwittberger/georgwittberger_7",
                "georgwittberger/georgwittberger_4",
                "georgwittberger/georgwittberger_8",
                "georgwittberger/georgwittberger_9",
                "georgwittberger/georgwittberger_18",
                "georgwittberger/georgwittberger_5",
                "georgwittberger/georgwittberger_2",
                "georgwittberger/georgwittberger_3",
                "georgwittberger/georgwittberger_12",
                "ewolff-kafka/ewolff-kafka_6",
                "ewolff-kafka/ewolff-kafka_18",
                "ewolff-kafka/ewolff-kafka_7",
                "ewolff-kafka/ewolff-kafka_0",
                "ewolff-kafka/ewolff-kafka_10",
                "ewolff-kafka/ewolff-kafka_3",
                "ewolff-kafka/ewolff-kafka_11",
                "ewolff-kafka/ewolff-kafka_4",
                "ewolff-kafka/ewolff-kafka_8",
                "ewolff-kafka/ewolff-kafka_9",
                "ewolff-kafka/ewolff-kafka_5"
        );
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
