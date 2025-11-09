package org.dataflowanalysis.analysis.dfd.interactive;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.analysis.utils.StringView;

/**
 * This class is responsible for the interaction with the analysis via a command line interface (CLI)
 */
public class DFDAnalysisCLI {
    private static final Logger logger = LoggerManager.getLogger(DFDAnalysisCLI.class);
    private static final String INPUT_INDICATOR = "> ";

    /**
     * Main entry point of the dfd analysis command line interface
     * <p/>
     * If the program is called without any arguments, the command line interface starts in interactive mode.
     * <p/>
     * If the program is called with arguments, the arguments must follow the following format: 1. Path to a
     * .dataflowdiagram file 2. Path to a .datadictionary file 3. Either a path to a .dfadsl file or a DSL constraint as a
     * string
     * @param args Arguments passed to the program via the command line call
     */
    public static void main(String[] args) {
        if (args.length != 0 && args.length != 3) {
            logger.error("Please provide either no arguments, or a path to a .dataflowdiagram and .datadictionary file!");
            System.exit(-1);
        }
        DFDConfidentialityAnalysis analysis;
        List<AnalysisConstraint> constraints;
        if (args.length == 0) {
            Scanner scanner = new Scanner(System.in);
            analysis = createAnalysisInteractive(scanner);
            constraints = createConstraintInteractive(scanner);
            scanner.close();
        } else {
            if (!args[0].endsWith(".dataflowdiagram")) {
                logger.error("The first argument should be a path to a .dataflowdiagram file");
                System.exit(-1);
            }
            if (!args[1].endsWith(".datadictionary")) {
                logger.error("The second argument should be a path to a .datadictionary file");
                System.exit(-1);
            }
            analysis = createAnalysis(args[0], args[1]);
            if (args[2].endsWith(".dfadsl")) {
                constraints = createConstraintsFromFile(args[2]);
            } else {
                constraints = List.of(createConstraint(args[2]));
            }
        }
        analysis.initializeAnalysis();
        FlowGraphCollection flowGraphs = analysis.findFlowGraphs();
        flowGraphs.evaluate();
        for (int i = 0; i < constraints.size(); i++) {
            AnalysisConstraint constraint = constraints.get(i);
            List<DSLResult> violations = constraint.findViolations(flowGraphs);
            for (DSLResult violation : violations) {
                logger.info("Violation for constraint " + constraint.getName() + ":");
                logger.info(violation.toString());
                logger.info("-------------------------");
            }
        }
        System.exit(0);
    }

    /**
     * Create a confidentiality analysis using the provided scanner input
     * @param scanner Scanner that provides the expected input file
     * @return Returns a confidentiality analysis with the dataflow diagram and data dictionary provided by the scanner
     */
    private static DFDConfidentialityAnalysis createAnalysisInteractive(Scanner scanner) {
        System.out.println("Please enter a path to a .dataflowdiagram file: ");
        System.out.print(INPUT_INDICATOR);
        String dataFlowDiagramPath = scanner.nextLine();

        System.out.println("Please enter a path to a .datadictionary file: ");
        System.out.print(INPUT_INDICATOR);
        String dataDictionaryPath = scanner.nextLine();

        return createAnalysis(dataFlowDiagramPath, dataDictionaryPath);
    }

    /**
     * Creates a confidentiality analysis using the provided dataflow diagram and data dictionary path
     * @param dataFlowDiagramPath Path to the dataflow diagram
     * @param dataDictionaryPath Path to the data dictionary
     * @return Returns a confidentiality analysis using the two provided paths
     */
    private static DFDConfidentialityAnalysis createAnalysis(String dataFlowDiagramPath, String dataDictionaryPath) {
        return new DFDDataFlowAnalysisBuilder().standalone()
                .useDataFlowDiagram(dataFlowDiagramPath)
                .useDataDictionary(dataDictionaryPath)
                .build();
    }

    /**
     * Creates a list of constraints from the provided strings on the scanner
     * @param scanner Scanner that provides constraints on each new line
     * @return Returns a list containing at least one analysis constraint
     */
    private static List<AnalysisConstraint> createConstraintInteractive(Scanner scanner) {
        List<AnalysisConstraint> constraints = new ArrayList<>();
        System.out.println("Please enter constraints: ");
        System.out.print(INPUT_INDICATOR);
        String constraintString = scanner.nextLine();
        while (!constraintString.isEmpty()) {
            constraints.add(createConstraint(constraintString));
            System.out.println("Please enter constraints (end with empty line): ");
            System.out.print(INPUT_INDICATOR);
            constraintString = scanner.nextLine();
        }
        return constraints;
    }

    /**
     * Creates a list of constraints from the provided file path
     * @param fileName Path to the file containing analysis constraints
     * @return Returns a list containing all constraints read from the provided input file
     */
    private static List<AnalysisConstraint> createConstraintsFromFile(String fileName) {
        List<AnalysisConstraint> constraints = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            List<String> lines = bufferedReader.lines()
                    .toList();
            for (int i = 0; i < lines.size(); i++) {
                var parseResult = AnalysisConstraint.fromString(new StringView(lines.get(i)));
                if (parseResult.failed()) {
                    logger.error("Invalid constraint in line" + i + ":");
                    logger.error(parseResult.getError());
                    System.exit(-1);
                }
                constraints.add(parseResult.getResult());
            }
        } catch (IOException e) {
            logger.error("Could not read file!", e);
            System.exit(-1);
        }
        return constraints;
    }

    /**
     * Creates a constraint using the given constraint in string form
     * @param constraintString Constraint in string form
     * @return Returns an analysis constraint parsed from the given string
     */
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
