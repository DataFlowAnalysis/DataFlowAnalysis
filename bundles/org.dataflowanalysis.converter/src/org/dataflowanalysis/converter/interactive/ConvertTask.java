package org.dataflowanalysis.converter.interactive;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import org.apache.log4j.Logger;
import org.dataflowanalysis.converter.Converter;
import org.dataflowanalysis.converter.ConverterModel;
import org.dataflowanalysis.converter.ModelType;
import org.dataflowanalysis.converter.PersistableConverterModel;
import org.dataflowanalysis.converter.dfd2web.DataFlowDiagramAndDictionary;
import org.dataflowanalysis.converter.micro2dfd.MicroConverterModel;
import org.dataflowanalysis.converter.pcm2dfd.PCMConverterModel;
import org.dataflowanalysis.converter.plant2micro.PlantConverterModel;
import org.dataflowanalysis.converter.web2dfd.WebEditorConverterModel;

public class ConvertTask {
    private static final Logger logger = Logger.getLogger(ConvertTask.class);

    /**
     * Entry point of the interactive converter.
     * Can be run interactively without any command line parameter or directly via the command line parameters
     * <p/>
     * During interactive mode users will be prompted for each input
     * <p/>
     * In direct mode parameters will be read from the command line arguments:
     * 1. Conversion in the format ORIGIN2DESTINATION
     * 2. Paths to models required for the origin model
     * 3. Path to the folder the result should be saved in
     * 4. File name of the resulting model files
     * @param args Command line parameters
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            handleParameters(args);
            System.exit(0);
        }
        handleInteractive();
        System.exit(0);
    }

    private static void handleParameters(String[] parameters) {
        String[] conversionSplit = parameters[0].split("2");
        if (conversionSplit.length != 2) {
            System.err.println("First parameter must describe the desired conversion. Example: pcm2dfd");
            System.exit(-1);
        }
        Optional<ModelType> origin = ModelType.fromAbbreviation(conversionSplit[0]);
        if (origin.isEmpty()) {
            System.err.println("Invalid requested conversion origin: " + conversionSplit[0]);
            System.exit(-1);
        }
        Optional<ModelType> destination = ModelType.fromAbbreviation(conversionSplit[1]);
        if (destination.isEmpty()) {
            System.err.println("Invalid requested conversion destination: " + conversionSplit[1]);
            System.exit(-1);
        }
        List<String> parameterList = new ArrayList<>(Arrays.stream(parameters).toList());
        parameterList.remove(0);

        ConversionTable conversionTable = new ConversionTable();
        Converter converter = conversionTable.getConverter(ConversionKey.of(origin.get(), destination.get()));
        ConverterModel converterModel = getConverterModel(origin.get(), parameterList);

        PersistableConverterModel persistableConverterModel = converter.convert(converterModel);

        if (parameterList.size() != 2) {
            System.err.println("Need two additional parameter for saving the converted model, but got: " + parameterList.size());
            System.exit(-1);
        }
        String filePath = parameterList.remove(0);
        String fileName = parameterList.remove(0);
        persistableConverterModel.save(filePath, fileName);
    }

    private static void handleInteractive() {
        Scanner scanner = new Scanner(System.in);
        ModelType origin = getOrigin(scanner);
        ModelType goal = getGoal(scanner, origin);
        runConversion(origin, goal, scanner);
    }

    private static ModelType getOrigin(Scanner scanner) {
        System.out.println("Please enter the desired model: ");
        ConversionTable conversionTable = new ConversionTable();
        List<ModelType> origins = conversionTable.getPossibleOrigins()
                .stream()
                .toList();
        return getModelTypeFromSelection(scanner, origins);
    }

    private static ModelType getGoal(Scanner scanner, ModelType origin) {
        System.out.println("Please enter the desired model: ");
        ConversionTable conversionTable = new ConversionTable();
        List<ModelType> goals = conversionTable.getPossibleDestinations(origin)
                .stream()
                .toList();
        return getModelTypeFromSelection(scanner, goals);
    }

    private static ModelType getModelTypeFromSelection(Scanner scanner, List<ModelType> modelTypes) {
        for (int i = 0; i < modelTypes.size(); i++) {
            System.out.println("[" + (i + 1) + "] " + modelTypes.get(i)
                    .getName());
        }
        try {
            int index = Integer.parseInt(scanner.nextLine()) - 1;
            return modelTypes.get(index);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static void runConversion(ModelType origin, ModelType goal, Scanner scanner) {
        ConversionTable conversionTable = new ConversionTable();
        Converter converter = conversionTable.getConverter(ConversionKey.of(origin, goal));

        ConverterModel input = getConverterModelInteractive(origin, scanner);
        PersistableConverterModel output = converter.convert(input);

        System.out.println("Please enter a path to a folder where the files should be saved. Leave empty for current working directory");
        System.out.print("> ");
        String filePath = scanner.nextLine();
        if (filePath.isEmpty())
            filePath = ".";
        System.out.println("Please enter a filename for the model files. Leave empty for \"default\"");
        System.out.print("> ");
        String fileName = scanner.nextLine();
        if (fileName.isEmpty())
            fileName = "default";
        output.save(filePath, fileName);
    }

    private static ConverterModel getConverterModelInteractive(ModelType modelType, Scanner scanner) {
        switch (modelType) {
            case PCM -> {
                return new PCMConverterModel(scanner);
            }
            case DFD -> {
                return new DataFlowDiagramAndDictionary(scanner);
            }
            case MICRO -> {
                return new MicroConverterModel(scanner);
            }
            case WEB_DFD -> {
                return new WebEditorConverterModel(scanner);
            }
            case PLANT -> {
                return new PlantConverterModel(scanner);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    private static ConverterModel getConverterModel(ModelType modelType, List<String> args) {
        switch (modelType) {
            case PCM -> {
                if (args.size() < 3) {
                    System.err.println("PCM Converter Model needs 3 paths, got: " + args.size());
                    System.exit(-1);
                }
                String usageModelPath = args.remove(0);
                String allocationPath = args.remove(0);
                String nodeCharacteristicsPath = args.remove(0);
                return new PCMConverterModel(usageModelPath, allocationPath, nodeCharacteristicsPath);
            }
            case DFD -> {
                if (args.size() < 2) {
                    System.err.println("DFD Converter Model needs 2 paths, got: " + args.size());
                    System.exit(-1);
                }
                String dataFlowDiagramPath = args.remove(0);
                String dataDictionaryPath = args.remove(0);
                return new DataFlowDiagramAndDictionary(dataFlowDiagramPath, dataDictionaryPath);
            }
            case MICRO -> {
                if (args.isEmpty()) {
                    System.err.println("Micro Converter Model needs a path, but got none");
                    System.exit(-1);
                }
                String microPath = args.remove(0);
                return new MicroConverterModel(microPath);
            }
            case WEB_DFD -> {
                if (args.isEmpty()) {
                    System.err.println("Web DFD Converter Model needs a path, but got none");
                    System.exit(-1);
                }
                String webPath = args.remove(0);
                return new WebEditorConverterModel(webPath);
            }
            case PLANT -> {
                if (args.isEmpty()) {
                    System.err.println("Plant Converter Model needs a path, but got none");
                    System.exit(-1);
                }
                String plantPath = args.remove(0);
                Path path = Paths.get(plantPath).toAbsolutePath().normalize();
                return new PlantConverterModel(path.getParent().toString(), path.getFileName().toString());
            }
            default -> throw new IllegalArgumentException();
        }
    }
}
