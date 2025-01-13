package org.dataflowanalysis.analysis.dfd.interactive;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.utils.StringView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DFDAnalysisCLI {
    private static final Logger logger = Logger.getLogger(DFDAnalysisCLI.class);

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
                logger.info("Violation for constraint " + i + ":");
                logger.info(violation.toString());
                logger.info("-------------------------");
            }
        }
        System.exit(0);
    }

    private static DFDConfidentialityAnalysis createAnalysisInteractive(Scanner scanner) {
        System.out.print("Please enter a path to a .dataflowdiagram file: ");
        String dataFlowDiagramPath = scanner.nextLine();
        System.out.print("Please enter a path to a .datadictionary file: ");
        String dataDictionaryPath = scanner.nextLine();
        return createAnalysis(dataFlowDiagramPath, dataDictionaryPath);
    }

    private static DFDConfidentialityAnalysis createAnalysis(String dataFlowDiagramPath, String dataDictionaryPath) {
        return new DFDDataFlowAnalysisBuilder()
                .standalone()
                .useDataFlowDiagram(dataFlowDiagramPath)
                .useDataDictionary(dataDictionaryPath)
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
