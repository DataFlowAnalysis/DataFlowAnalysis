package org.dataflowanalysis.analysis.tests.dfd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.core.DFDCyclicTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.examplemodels.Activator;
import org.junit.jupiter.api.Test;

public class CyclicFinderTest {
    
    @Test
    void simpleLoopCheck() {
        String locationLoop = Paths.get("models", "simpleLoopDFD").toString();
        var model = Paths.get(locationLoop, "loopDFD").toString();
        
        var analysis = buildAnalysis(model);
        analysis.initializeAnalysis();
        var flowGraph = analysis.findFlowGraphs();
        List<List<String>> list = new ArrayList<>();
        
        for(var tfg : flowGraph.getTransposeFlowGraphs()) {
            var innerList = new ArrayList<String>();
            for(var vertex : tfg.getVertices()) {
                var dfdVertex = (DFDVertex) vertex;
                innerList.add(dfdVertex.getName());
                
            }
            list.add(innerList);
        }
        List<List<String>> compareList = new ArrayList<>();
        
        compareList.add(List.of("A", "B", "C"));
        
        compareList.add(List.of("A", "B", "D", "B", "C"));
        
        assertEquals(list, compareList);
        
    }
    
    
    public DFDConfidentialityAnalysis buildAnalysis(String name) {
        var DataFlowDiagramPath = name + ".dataflowdiagram";
        var DataDictionaryPath = name + ".datadictionary";

        return new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName("org.dataflowanalysis.examplemodels")
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(DataFlowDiagramPath)
                .useDataDictionary(DataDictionaryPath)
                .useTransposeFlowGraphFinder(DFDCyclicTransposeFlowGraphFinder.class)
                .build();
    }
}
