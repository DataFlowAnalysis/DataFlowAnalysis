package org.dataflowanalysis.converter.tests;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.dataflowanalysis.converter.micro2dfd.Micro2DFDConverter;
import org.dataflowanalysis.converter.micro2dfd.MicroConverterModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TUHHPipelineTest {
    public static Path converter;
    private final Logger logger = Logger.getLogger(TUHHPipelineTest.class);

    public static final List<Integer> OUT_OF_SCOPE_VARIANTS = List.of(13, 14, 15, 16, 17);

    public static final Map<String, List<Integer>> FAULTY_VARIANTS = Map.ofEntries(entry("callistaenterprise", List.of(4)),
            entry("ewolff", List.of(3, 6)), entry("fernandoabcampos", List.of(3, 6)), entry("jferrater", List.of(1, 4)),
            entry("mdeket", List.of(3, 6)), entry("mudigal-technologies", List.of(3, 6)), entry("rohitghatol", List.of(11)),
            entry("spring-petclinic", List.of(4)), entry("georgwittberger", List.of(9)));

    public static final Map<String, List<Integer>> CYCLIC_SINK_VARIANTS = Map.ofEntries(entry("anilallewar", List.of(10)),
            entry("ewolff", List.of(0, 2, 4, 7, 8, 11)), entry("ewolff-kafka", List.of(10, 11, 12)), entry("jferrater", List.of(10, 11, 12)),
            entry("mdeket", List.of(0, 2, 4, 7, 8, 9, 10, 11, 12)), entry("mudigal-technologies", List.of(10, 12)),
            entry("piomin", List.of(0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 17, 18)), entry("rohitghatol", List.of(0, 6, 7, 8, 9)),
            entry("shabbirdwd53", List.of(0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 17, 18)), entry("spring-petclinic", List.of(10, 11, 12)),
            entry("yidongnan", List.of(10, 11, 12)), entry("fernandoabcampos", List.of(0, 1, 2, 4, 5, 7, 8, 9, 10, 11, 12)));

    @Disabled
    @Test
    public void runPipeline() throws IOException, InterruptedException {
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

        for (var path : Files.newDirectoryStream(dataset)) {
            if (Files.isRegularFile(path)) {
                for (var variant : variants) {
                    if (path.getFileName()
                            .toString()
                            .contains("_" + variant + ".")) {
                        Files.delete(path);
                    }
                }
            }
        }

        if (Files.list(dataset)
                .count() == 0) {
            Files.delete(dataset);
        }
    }

    private void convertJsonToDFD(Path dataset) throws IOException {
        var microConverter = new Micro2DFDConverter();
        Files.list(dataset)
                .forEach(path -> {
                    if (Files.isRegularFile(path) && path.toString()
                            .endsWith(".json")) {
                        logger.info(path);
                        MicroConverterModel microConverterModel = new MicroConverterModel(path.toString());
                        var complete = microConverter.convert(microConverterModel);
                        complete.save(path.getParent()
                                .toString(),
                                path.getFileName()
                                        .toString());
                    }
                });
    }

    private void convertTxtToJson(Path dataset) throws IOException, InterruptedException {
        for (var path : Files.newDirectoryStream(dataset)) {
            if (Files.isRegularFile(path)) {
                if (path.toString()
                        .endsWith(".txt")) {
                    logger.info(path);
                    runPythonScript(converter.toString(), path.toString(), "json", path.toString()
                            .replace(".txt", ".json"));
                    Files.delete(path);

                }
            }
        }
    }

    private void moveTxtVariantsUp(Path dir) throws IOException {
        Path parentDir = dir.getParent();
        for (var path : Files.newDirectoryStream(dir)) {
            if (Files.isRegularFile(path)) {
                Path targetPath = parentDir.resolve(path.getFileName());
                Files.move(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        Files.delete(dir);
    }

    private void cleanTopLevelOfDataset(Path dataset) throws IOException {
        for (var path : Files.newDirectoryStream(dataset)) {
            if (Files.isRegularFile(path)) {
                if (!path.toString()
                        .endsWith(".json") || path.toString()
                                .endsWith("traceability.json")) {
                    Files.delete(path);
                } else {
                    var renamedBaseModel = dataset.resolve(dataset.getFileName() + "_0.json");
                    Files.move(path, renamedBaseModel, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private void removeAndCreateDir(Path dir) throws IOException {
        if (Files.exists(dir)) {
            for (Path path : Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList())) {
                Files.delete(path);
            }
        }
        Files.createDirectories(dir);
    }

    private void copyDir(String sourceDirectoryLocation, String destinationDirectoryLocation) throws IOException {
        for (var source : Files.walk(Paths.get(sourceDirectoryLocation))
                .collect(Collectors.toList())) {
            Path destination = Paths.get(destinationDirectoryLocation, source.toString()
                    .substring(sourceDirectoryLocation.length()));
            Files.copy(source, destination);

        }
    }

    private void renameTxtVariants(Path variants) throws IOException {
        var modelName = variants.getParent()
                .getFileName();
        for (var path : Files.newDirectoryStream(variants)) {
            if (Files.isRegularFile(path)) {
                var renamedModel = variants.resolve(modelName + "_" + path.getFileName());
                Files.move(path, renamedModel, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private int runPythonScript(String script, String in, String format, String out) throws InterruptedException, IOException {
        String[] command = {"python3", script, in, format, "-op", out};

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process;

        process = processBuilder.start();
        return process.waitFor();

    }
}
