package org.dataflowanalysis.converter.task;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysis;
import org.dataflowanalysis.converter.DataFlowDiagramAndDictionary;
import org.dataflowanalysis.converter.DataFlowDiagramConverter;
import org.dataflowanalysis.converter.PCMConverter;
import org.dataflowanalysis.converter.WebEditorConverter;
import org.dataflowanalysis.converter.webdfd.WebEditorDfd;
import org.dataflowanalysis.pcm.extension.dddsl.DDDslStandaloneSetup;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;

import java.util.Scanner;

public class ConvertTask {
    private static final Logger logger = Logger.getLogger(ConvertTask.class);

    public static void main(String[] args) {
        if (args.length > 0 ) {
            System.err.println("Conversion via arguments not implemented!");
            System.exit(-1);
        }
        Scanner scanner = new Scanner(System.in);
        ConversionOrigin origin = getOrigin(scanner);
        ConversionGoal goal = getGoal(scanner);
        runConversion(origin, goal, scanner);
    }

    private static ConversionOrigin getOrigin(Scanner scanner) {
        System.out.println("Please enter the desired model: ");
        ConversionOrigin[] origins = ConversionOrigin.values();
        for (int i = 0; i < origins.length; i++) {
            System.out.println("[" + (i + 1) + "] " + origins[i].getName());
        }
        return origins[scanner.nextInt() - 1];
    }


    private static ConversionGoal getGoal(Scanner scanner) {
        System.out.println("Please enter the desired model: ");
        ConversionGoal[] goals = ConversionGoal.values();
        for (int i = 0; i < goals.length; i++) {
            System.out.println("[" + (i + 1) + "] " + goals[i].getName());
        }
        return goals[scanner.nextInt() - 1];
    }

    private static void runConversion(ConversionOrigin origin, ConversionGoal goal, Scanner scanner) {
        switch (origin) {
            case PCM -> runPCMConversion(goal, scanner);
            case DFD -> runDFDConversion(goal, scanner);
            //case WEB_DFD -> runWebDFDConversion(goal, scanner);
            //case MICRO -> runMicroConversion(goal, scanner);
            default -> {
                System.err.println("Unknown conversion origin");
                System.exit(-1);
            }
        }
    }

    private static void runPCMConversion(ConversionGoal goal, Scanner scanner) {
        switch (goal) {
            case DFD -> convertPCM2DFD(scanner);
            //case WEB_EDITOR -> convertPCM2Web(scanner);
            default -> {
                System.err.println("Unknown conversion goal for a pcm model!");
                System.exit(-1);
            }
        }
    }

    private static void convertPCM2DFD(Scanner scanner) {
        System.out.println("Please enter a allocation model location relative to the converter project");
        String allocationModelLocation = scanner.next();
        System.out.println("Please enter a usage model location relative to the converter project");
        String usageModelLocation = scanner.next();
        System.out.println("Please enter a node characteristics model location relative to the converter project");
        String nodecharacteristicsModelLocation = scanner.next();

        System.out.println("----- Running Conversion -----");
        PCMConverter converter = new PCMConverter();
        DataFlowDiagramAndDictionary result;
        try {
            result = converter.pcmToDFD("org.dataflowanalysis.converter", usageModelLocation, allocationModelLocation, nodecharacteristicsModelLocation, Activator.class);
        } catch (Exception e) {
            System.err.println("Analysis run failed! Please check the following exception:");
            throw e;
            // System.err.println(e.getMessage());
            //System.exit(-1);
            //return;
        }
        System.out.println("Please enter a path for the resulting DFD");
        String fileName = scanner.next();
        converter.storeDFD(result, fileName);
        System.exit(0);
    }

    private static void runDFDConversion(ConversionGoal goal, Scanner scanner) {
        switch (goal) {
            case DFD -> System.exit(0);
            case WEB_EDITOR -> convertDFD2Web(scanner);
        }
    }

    private static void convertDFD2Web(Scanner scanner) {
        System.out.println("Please enter a data flow diagram model location");
        String dataFlowDiagramLocation = scanner.next();
        System.out.println("Please enter a data dictionary model location");
        String dataDictionaryLocation = scanner.next();


        System.out.println("----- Running Conversion -----");
        DataFlowDiagramConverter converter = new DataFlowDiagramConverter();
        WebEditorDfd result;
        try {
            result = converter.dfdToWeb("org.dataflowanalysis.converter", dataFlowDiagramLocation, dataDictionaryLocation, Activator.class);
        } catch (Exception e) {
            System.err.println("Analysis run failed! Please check the following exception:");
            System.err.println(e.getMessage());
            System.exit(-1);
            return;
        }
        System.out.println("Please enter a path for the resulting DFD");
        String fileName = scanner.next();
        converter.storeWeb(result, fileName);
        System.exit(0);
    }
}
