package org.dataflowanalysis.tests.wins;

import static org.dataflowanalysis.analysis.tests.integration.AnalysisUtils.TEST_MODEL_PROJECT_NAME;

import java.nio.file.Paths;
import java.util.List;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.analysis.dsl.result.DSLConstraintTrace;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.examplemodels.Activator;
import org.junit.jupiter.api.Test;

public class ViolationTest {
    private DataFlowConfidentialityAnalysis analysis;
    
    private static final Logger logger = LoggerManager.getLogger(ViolationTest.class);

    @Test
    public void testViolationOutput() {
        final var dfdPath = Paths.get("scenarios", "dfd", "OnlineShop", "default.dataflowdiagram")
                .toString();
        final var ddPath = Paths.get("scenarios", "dfd", "OnlineShop", "default.datadictionary")
                .toString();

        analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataDictionary(ddPath)
                .useDataFlowDiagram(dfdPath)
                .build();

        analysis.initializeAnalysis();
        
        var flowGraphCollection = analysis.findFlowGraphs();      
        flowGraphCollection.evaluate();
        
        AnalysisConstraint constraint = new ConstraintDSL().ofData()
                .withLabel("Sensitivity", List.of("Personal"))
                .fromNode()
                .neverFlows()
                .toVertex()
                .withCharacteristic("Location", "nonEU")
                .create();

        logger.info("Evaluating DSL constraint: \"%s\"".formatted(constraint.toString()));
        List<DSLResult> results = constraint.findViolations(flowGraphCollection);
        
        for (DSLResult result : results) {
            for (AbstractVertex<?> vertex : result.getMatchedVertices()) {
                logger.warn("Verletzender Knoten gefunden: " + vertex.toString());

                DSLConstraintTrace trace = result.getConstraintTrace();
                trace.getMissingSelectors(vertex).ifPresent(missing -> {
                    for (var s : missing) {
                        logger.warn("  -> Fehlende Eigenschaft: " + s.toString());
                    }
                });

                logger.info("Label am Knoten: " + vertex.getAllVertexCharacteristics());
                logger.info("Daten am Knoten: " + vertex.getAllDataCharacteristics());
            }
            logger.info("Violation im Teilgraph: " + result.getTransposeFlowGraph().toString());
        }

    }
    
}

