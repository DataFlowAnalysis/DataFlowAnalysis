package org.dataflowanalysis.analysis.tests.integration.dfd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.examplemodels.Activator;
import org.junit.jupiter.api.Test;

public class CyclicFinderTest {
    private static final String PROJECT_NAME = "org.dataflowanalysis.examplemodels";

    @Test
    void simpleLoopCheck() {
        String locationLoop = Paths.get("models", "dfd", "SimpleLoop")
                .toString();
        var model = Paths.get(locationLoop, "default")
                .toString();

        var analysis = buildAnalysis(model);
        analysis.initializeAnalysis();
        var flowGraph = analysis.findFlowGraphs();
        List<List<String>> flowGraphVertexNames = new ArrayList<>();

        for (var tfg : flowGraph.getTransposeFlowGraphs()) {
            var vertexNames = new ArrayList<String>();
            for (var vertex : tfg.getVertices()) {
                var dfdVertex = (DFDVertex) vertex;
                vertexNames.add(dfdVertex.getName());

            }
            flowGraphVertexNames.add(vertexNames);
        }
        var expectedVertexNames = List.of((List.of("A", "B", "C")), (List.of("A", "B", "D", "B", "C")));

        assertEquals(flowGraphVertexNames, expectedVertexNames);

    }

    @Test
    public void checkPseudoLoopNotDetected() {
        String locationLoop = Paths.get("models", "dfd", "ComplexPseudoCycle")
                .toString();
        var model = Paths.get(locationLoop, "default")
                .toString();

        var analysis = buildAnalysis(model);
        analysis.initializeAnalysis();
        var flowGraph = analysis.findFlowGraphs();

        assertTrue(!flowGraph.wasCyclic());
    }

    @Test
    public void checkIsCyclic() {
        String locationLoop = Paths.get("models", "dfd", "SimpleLoop")
                .toString();
        var model = Paths.get(locationLoop, "default")
                .toString();

        var analysis = buildAnalysis(model);
        analysis.initializeAnalysis();
        var flowGraph = analysis.findFlowGraphs();

        assertTrue(flowGraph.wasCyclic());
    }

    private DFDConfidentialityAnalysis buildAnalysis(String name) {
        var dataFlowDiagramPath = name + ".dataflowdiagram";
        var dataDictionaryPath = name + ".datadictionary";

        return new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(dataFlowDiagramPath)
                .useDataDictionary(dataDictionaryPath)
                .useTransposeFlowGraphFinder(DFDTransposeFlowGraphFinder.class)
                .build();
    }
}
