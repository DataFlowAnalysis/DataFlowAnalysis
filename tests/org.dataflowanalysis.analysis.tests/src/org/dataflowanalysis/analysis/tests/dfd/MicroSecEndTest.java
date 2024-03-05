package org.dataflowanalysis.analysis.tests.dfd;

import java.nio.file.Paths;
import java.util.List;
import java.io.File;


import org.dataflowanalysis.analysis.core.*;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.utils.ResourceUtils;
import org.dataflowanalysis.analysis.converter.Activator;
import org.junit.jupiter.api.Test;

public class MicroSecEndTest extends BaseTest {
    public static String PROJECT_NAME = "org.dataflowanalysis.analysis.converter";
	
	public DFDConfidentialityAnalysis buildAnalysis(String name) {
	var DataFlowDiagramPath = Paths.get(name+".dataflowdiagram");
	var DataDictionaryPath = Paths.get(name+".datadictionary");
	
	return new DFDDataFlowAnalysisBuilder()
			.standalone()
			.modelProjectName(PROJECT_NAME)
			.usePluginActivator(Activator.class)
			.useDataFlowDiagram(DataFlowDiagramPath.toString())
			.useDataDictionary(DataDictionaryPath.toString())
			.build();
	}
	
	public void runAnalysis(String model) {
		var analysis=buildAnalysis(model);
		analysis.initializeAnalysis();
		System.out.println(analysis.toString());
		var flowGraph = analysis.findFlowGraph();
		flowGraph.evaluate();
		
		for (AbstractPartialFlowGraph aPFG : flowGraph.getPartialFlowGraphs()) {
            List<? extends AbstractVertex<?>> violations = analysis.queryDataFlow(aPFG,node -> {
                if(hasNodeCharacteristic(node, "annotation", "infrastructural") && hasDataCharacteristic(node, "annotation", "internal")) {
                    System.out.println(node.createPrintableNodeInformation());

                }
            return false;
        }
      );
            if(!violations.isEmpty()) {
                System.out.println("Violations: " + violations);
            }
        }
	}
	
	@Test
	public void test() {
		File directory = new File("../../bundles/org.dataflowanalysis.analysis.converter");
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".json"));
        for(File file:files) {
        	runAnalysis(file.getName().replaceAll("\\.json.*", ""));
        }
		//CreateURI
		//ResourceUtils.createRelativePluginURI("jferrater.json", PROJECT_NAME);
	}
	
	public boolean hasNodeCharacteristic(AbstractVertex<?> node, String type, String value) {
		if(node.getAllNodeCharacteristics().stream().anyMatch(n -> n.getTypeName().equals(type) && n.getValueName().equals(value))) {
				return true;
		}
		return false;
	}
	
	public boolean hasDataCharacteristic(AbstractVertex<?> node, String type, String value) {
		if(node.getAllDataFlowVariables().stream().anyMatch(v -> v.getAllCharacteristics().stream().anyMatch(c -> c.getTypeName().equals(type) && c.getValueName().equals(value)))){
			return true;
		}
		return false;
	}
}
