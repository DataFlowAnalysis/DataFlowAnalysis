package org.dataflowanalysis.analysis.dfd.interactive;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.utils.StringView;

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
        AnalysisConstraint constraint;
        if (args.length == 0) {
            Scanner scanner = new Scanner(System.in);
            analysis = createAnalysisInteractive(scanner);
            constraint = createConstraintInteractive(scanner);
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
            constraint = createConstraint(args[2]);
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
