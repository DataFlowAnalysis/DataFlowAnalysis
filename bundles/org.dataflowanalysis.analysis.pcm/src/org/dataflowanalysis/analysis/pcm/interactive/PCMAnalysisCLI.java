package org.dataflowanalysis.analysis.pcm.interactive;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.utils.StringView;

import java.util.List;
import java.util.Scanner;

public class PCMAnalysisCLI {
    private static final Logger logger = Logger.getLogger(PCMAnalysisCLI.class);

    public static void main(String[] args) {
        if (args.length != 0 && args.length != 4) {
            logger.error("Please provide either no arguments, or a path to a .dataflowdiagram and .datadictionary file!");
            System.exit(-1);
        }
        PCMDataFlowConfidentialityAnalysis analysis;
        AnalysisConstraint constraint;
        if (args.length == 0) {
            Scanner scanner = new Scanner(System.in);
            analysis = createAnalysisInteractive(scanner);
            constraint = createConstraintInteractive(scanner);
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
            analysis = createAnalysis(args[0], args[1], args[2]);
            constraint = createConstraint(args[3]);
        }
        analysis.initializeAnalysis();

        FlowGraphCollection flowGraphs = analysis.findFlowGraphs();
        flowGraphs.evaluate();
        List<DSLResult> violations = constraint.findViolations(flowGraphs);
        for (DSLResult violation : violations) {
            logger.info(violation.toString());
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

    private static AnalysisConstraint createConstraintInteractive(Scanner scanner) {
        System.out.print("Please enter a constraint: ");
        String constraintString = scanner.nextLine();
        return createConstraint(constraintString);
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
