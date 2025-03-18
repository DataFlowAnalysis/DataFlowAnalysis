package org.dataflowanalysis.converter.interactive;

import java.util.List;
import java.util.Scanner;
import org.apache.log4j.Logger;
import org.dataflowanalysis.converter.Converter;
import org.dataflowanalysis.converter.ConverterModel;
import org.dataflowanalysis.converter.dfd2web.DataFlowDiagramAndDictionary;
import org.dataflowanalysis.converter.pcm2dfd.PCMConverterModel;

public class ConvertTask {
    private static final Logger logger = Logger.getLogger(ConvertTask.class);

    public static void main(String[] args) {
        if (args.length > 0) {
            System.err.println("Conversion via arguments not implemented!");
            System.exit(-1);
        }
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
        for (int i = 0; i < origins.size(); i++) {
            System.out.println("[" + (i + 1) + "] " + origins.get(i)
                    .getName());
        }
        try {
            int index = Integer.parseInt(scanner.nextLine()) - 1;
            return origins.get(index);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static ModelType getGoal(Scanner scanner, ModelType origin) {
        System.out.println("Please enter the desired model: ");
        ConversionTable conversionTable = new ConversionTable();
        List<ModelType> goals = conversionTable.getPossibleDestinations(origin)
                .stream()
                .toList();
        for (int i = 0; i < goals.size(); i++) {
            System.out.println("[" + (i + 1) + "] " + goals.get(i)
                    .getName());
        }
        try {
            int index = Integer.parseInt(scanner.nextLine()) - 1;
            return goals.get(index);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static void runConversion(ModelType origin, ModelType goal, Scanner scanner) {
        ConversionTable conversionTable = new ConversionTable();
        Converter converter = conversionTable.getConverter(ConversionKey.of(origin, goal));

        ConverterModel input = getConverterModel(origin, scanner);
        ConverterModel output = converter.convert(input);

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

    private static ConverterModel getConverterModel(ModelType modelType, Scanner scanner) {
        switch (modelType) {
            case PCM -> {
                return new PCMConverterModel(scanner);
            }
            case DFD -> {
                return new DataFlowDiagramAndDictionary(scanner);
            }
            case MICRO, WEB_DFD -> {
                return null;
            }
            default -> throw new IllegalArgumentException();
        }
    }
}
