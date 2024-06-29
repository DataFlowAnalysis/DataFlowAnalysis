package org.dataflowanalysis.converter.tests;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

import org.apache.log4j.Logger;
import org.dataflowanalysis.converter.MicroSecEndConverter;

public class TUHHPipeline {

    public static Path converter;
    private final Logger logger = Logger.getLogger(TUHHPipeline.class);

    public static final List<Integer> OUT_OF_SCOPE_VARIANTS = List.of(13, 14, 15, 16, 17);

    public static final Map<String, List<Integer>> FAULTY_VARIANTS = ImmutableMap.<String, List<Integer>>builder()
            .put("anilallewar", List.of(6))
            .put("ewolff", List.of(3, 6))
            .put("fernandoabcampos", List.of(3, 6))
            .put("mdeket", List.of(3, 6))
            .put("mudigal-technologies", List.of(3, 6))
            .put("rohitghatol", List.of(11))
            .put("sqshq", List.of(6))
            .build();

    public static final Map<String, List<Integer>> CYCLIC_SINK_VARIANTS = ImmutableMap.<String, List<Integer>>builder()
            .put("anilallewar", List.of(10))
            .put("ewolff-kafka", List.of(12))
            .put("mdeket", List.of(0, 2, 4, 7, 8, 9, 10, 11, 12))
            .put("mudigal-technologies", List.of(10, 12))
            .put("piomin", List.of(0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 17, 18))
            .put("rohitghatol", List.of(0, 6, 7, 8, 9))
            .put("shabbirdwd53", List.of(0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 17, 18))
            .put("spring-petclinic", List.of(10, 11, 12))
            .put("yidongnan", List.of(10, 11, 12))
            .build();

    //@Disabled
    @Test
    public void runPipeline() throws IOException {
        var tuhhRepo = "microSecEnD-main";
        assertTrue(Files.isDirectory(Paths.get(tuhhRepo)));

        var converterRepo = Paths.get(tuhhRepo, "convert_model.py");
        assertTrue(Files.isRegularFile(converterRepo));

        Path datasetFolderRepo = Paths.get(tuhhRepo, "dataset");
        List<Path> datasetsRepo = new ArrayList<>();
        Files.list(datasetFolderRepo)
                .forEach(path -> {
                    if (Files.isDirectory(path)) {
                        datasetsRepo.add(path);
                    }
                });
        assertEquals(datasetsRepo.size(), 17);

        var tuhh = Paths.get("TUHH-Models");
        removeAndCreateDir(tuhh);

        converter = tuhh.resolve(converterRepo.getFileName());
        Files.copy(converterRepo, converter, StandardCopyOption.REPLACE_EXISTING);

        List<Path> datasets = new ArrayList<>();
        for (var dataset : datasetsRepo) {
            var datasetName = dataset.getFileName()
                    .toString();
            int underscoreIndex = datasetName.indexOf('_');
            if (datasetName.contains("kafka")) {
                datasetName = datasetName.substring(0, underscoreIndex) + "-kafka";
            } else {
                datasetName = datasetName.substring(0, underscoreIndex);
            }
            var newDataset = tuhh.resolve(datasetName);
            copyDir(dataset.toString(), newDataset.toString());
            datasets.add(newDataset);
        }

        assertTrue(FAULTY_VARIANTS.keySet()
                .stream()
                .allMatch(key -> datasets.stream()
                        .anyMatch(path -> path.getFileName()
                                .toString()
                                .equals(key))));
        assertTrue(CYCLIC_SINK_VARIANTS.keySet()
                .stream()
                .allMatch(key -> datasets.stream()
                        .anyMatch(path -> path.getFileName()
                                .toString()
                                .equals(key))));

        for (var dataset : datasets) {
            assertTrue(Files.isDirectory(dataset.resolve("model_variants")));
            cleanTopLevelOfDataset(dataset);
            renameTxtVariants(dataset.resolve("model_variants"));
            moveTxtVariantsUp(dataset.resolve("model_variants"));
            convertTxtToJson(dataset);
            convertJsonToDFD(dataset);
            filter(dataset);
        }

        Files.delete(converter);
    }

    private void filter(Path dataset) throws IOException {
        Set<Integer> variants = new HashSet<>(OUT_OF_SCOPE_VARIANTS);
        var datasetName = dataset.getFileName()
                .toString();
        if (FAULTY_VARIANTS.containsKey(datasetName)) {
            variants.addAll(FAULTY_VARIANTS.get(datasetName));
        }
        if (CYCLIC_SINK_VARIANTS.containsKey(datasetName)) {
            variants.addAll(CYCLIC_SINK_VARIANTS.get(datasetName));
        }

        Files.list(dataset)
                .forEach(path -> {
                    if (Files.isRegularFile(path)) {
                        for (var variant : variants) {
                            if (path.getFileName()
                                    .toString()
                                    .contains("_" + variant + ".")) {
                                try {
                                    Files.delete(path);
                                } catch (IOException e) {
                                }
                            }
                        }
                    }
                });

        if (Files.list(dataset)
                .count() == 0) {
            Files.delete(dataset);
        }
    }

    private void convertJsonToDFD(Path dataset) throws IOException {
        var microConverter = new MicroSecEndConverter();
        Files.list(dataset)
                .forEach(path -> {
                    if (Files.isRegularFile(path) && path.toString()
                            .endsWith(".json")) {
                        logger.info(path);
                        var complete = microConverter.microToDfd(path.toString());
                        microConverter.storeDFD(complete, path.toString());
                    }
                });
    }

    private void convertTxtToJson(Path dataset) throws IOException {
        Files.list(dataset)
                .forEach(path -> {
                    if (Files.isRegularFile(path)) {
                        if (path.toString()
                                .endsWith(".txt")) {
                            logger.info(path);
                            try {
                                runPythonScript(converter.toString(), path.toString(), "json", path.toString()
                                        .replace(".txt", ".json"));
                                Files.delete(path);
                            } catch (InterruptedException | IOException e) {
                            }
                        }
                    }
                });
    }

    private void moveTxtVariantsUp(Path dir) throws IOException {
        Path parentDir = dir.getParent();

        Files.list(dir)
                .forEach(path -> {
                    if (Files.isRegularFile(path)) {
                        Path targetPath = parentDir.resolve(path.getFileName());
                        try {
                            Files.move(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                        }
                    }
                });

        Files.delete(dir);
    }

    private void cleanTopLevelOfDataset(Path dataset) throws IOException {
        Files.list(dataset)
                .forEach(path -> {
                    if (Files.isRegularFile(path)) {
                        if (!path.toString()
                                .endsWith(".json") || path.toString()
                                        .endsWith("traceability.json")) {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                            }
                        } else {
                            var renamedBaseModel = dataset.resolve(dataset.getFileName() + "_0.json");
                            try {
                                Files.move(path, renamedBaseModel, StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                            }
                        }
                    }
                });
    }

    private void removeAndCreateDir(Path dir) throws IOException {
        if (Files.exists(dir)) {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                        }
                    });
        }
        Files.createDirectories(dir);
    }

    private void copyDir(String sourceDirectoryLocation, String destinationDirectoryLocation) throws IOException {
        Files.walk(Paths.get(sourceDirectoryLocation))
                .forEach(source -> {
                    Path destination = Paths.get(destinationDirectoryLocation, source.toString()
                            .substring(sourceDirectoryLocation.length()));
                    try {
                        Files.copy(source, destination);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void renameTxtVariants(Path variants) throws IOException {
        var modelName = variants.getParent()
                .getFileName();
        Files.list(variants)
                .forEach(path -> {
                    if (Files.isRegularFile(path)) {
                        var renamedModel = variants.resolve(modelName + "_" + path.getFileName());
                        try {
                            Files.move(path, renamedModel, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                        }
                    }
                });
    }

    private int runPythonScript(String script, String in, String format, String out) throws InterruptedException, IOException {
        String[] command = {"python3", script, in, format, "-op", out};

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process;

        process = processBuilder.start();
        return process.waitFor();

    }
}
