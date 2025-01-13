package org.dataflowanalysis.analysis.pcm.interactive;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.utils.StringView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PCMAnalysisCLI {
    private static final Logger logger = Logger.getLogger(PCMAnalysisCLI.class);

    public static void main(String[] args) {
        if (args.length != 0 && args.length != 4) {
            logger.error("Please provide either no arguments, or a path to a .usagemodel, .allocation and .nodecharacteristics file and a constraint!");
            System.exit(-1);
        }
        PCMDataFlowConfidentialityAnalysis analysis;
        List<AnalysisConstraint> constraints;
        if (args.length == 0) {
            Scanner scanner = new Scanner(System.in);
            analysis = createAnalysisInteractive(scanner);
            constraints = createConstraintInteractive(scanner);
            scanner.close();
        } else {
            if (!args[0].endsWith(".usagemodel")) {
                logger.error("The first argument should be a path to a .usagemodel file");
                System.exit(-1);
            }
            if (!args[1].endsWith(".allocation")) {
                logger.error("The second argument should be a path to a .allocation file");
                System.exit(-1);
            }
            if (!args[2].endsWith(".nodecharacteristics")) {
                logger.error("The second argument should be a path to a .nodecharacteristics file");
                System.exit(-1);
            }
            analysis = createAnalysis(args[0], args[1], args[2]);
            if(args[3].endsWith(".dfadsl")) {
                constraints = createConstraintsFromFile(args[3]);
            } else {
                constraints = List.of(createConstraint(args[3]));
            }
        }
        analysis.initializeAnalysis();

        FlowGraphCollection flowGraphs = analysis.findFlowGraphs();
        flowGraphs.evaluate();
        for (int i = 0; i < constraints.size(); i++) {
            AnalysisConstraint constraint = constraints.get(i);
            List<DSLResult> violations = constraint.findViolations(flowGraphs);
            for (DSLResult violation : violations) {
                logger.info("Violation for constraint " + i + ":");
                logger.info(violation.toString());
                logger.info("-------------------------");
            }
        }
        System.exit(0);
    }

    private static PCMDataFlowConfidentialityAnalysis createAnalysisInteractive(Scanner scanner) {
        System.out.print("Please enter a path to a .usagemodel file: ");
        String usageModelPath = scanner.nextLine();
        System.out.print("Please enter a path to a .allocation file: ");
        String allocationModelPath = scanner.nextLine();
        System.out.print("Please enter a path to a .nodecharacteristics file: ");
        String nodecharacteristicsModelPath = scanner.nextLine();
        return createAnalysis(usageModelPath, allocationModelPath, nodecharacteristicsModelPath);
    }

    private static PCMDataFlowConfidentialityAnalysis createAnalysis(String usageModelPath, String allocationModelPath, String nodecharacteristicsModelPath) {
        return new PCMDataFlowConfidentialityAnalysisBuilder()
                .standalone()
                .useUsageModel(usageModelPath)
                .useAllocationModel(allocationModelPath)
                .useNodeCharacteristicsModel(nodecharacteristicsModelPath)
                .build();
    }

    private static List<AnalysisConstraint> createConstraintInteractive(Scanner scanner) {
        List<AnalysisConstraint> constraints = new ArrayList<>();
        System.out.print("Please enter constraints: ");
        String constraintString = scanner.nextLine();
        while (!constraintString.isEmpty()) {
            constraints.add(createConstraint(constraintString));
            System.out.print("Please enter constraints (end with empty line): ");
            constraintString = scanner.nextLine();
        }
        return constraints;
    }

    private static List<AnalysisConstraint> createConstraintsFromFile(String fileName) {
        List<AnalysisConstraint> constraints = new ArrayList<>();
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            List<String> lines = bufferedReader.lines().toList();
            for (int i = 0; i < lines.size(); i++) {
                var parseResult = AnalysisConstraint.fromString(new StringView(lines.get(i)));
                if (parseResult.failed()) {
                    logger.error("Invalid constraint in line" + i + ":");
                    logger.error(parseResult.getError());
                    System.exit(-1);
                }
                constraints.add(parseResult.getResult());
            }
        }
        catch (IOException e) {
            logger.error("Could not read file!", e);
            System.exit(-1);
        }
        return constraints;
    }

    private static AnalysisConstraint createConstraint(String constraintString) {
        var parseResult = AnalysisConstraint.fromString(new StringView(constraintString));
        if (parseResult.failed()) {
            logger.error("Invalid constraint:");
            logger.error(parseResult.getError());
            System.exit(-1);
        }
        return parseResult.getResult();
    }
}
