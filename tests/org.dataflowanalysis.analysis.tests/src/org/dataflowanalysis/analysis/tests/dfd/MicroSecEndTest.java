package org.dataflowanalysis.analysis.tests.dfd;

import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;


import org.dataflowanalysis.analysis.core.*;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.converter.Activator;
import org.dataflowanalysis.analysis.converter.*;
import org.junit.jupiter.api.Test;

import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;

public class MicroSecEndTest {
    public static String PROJECT_NAME = "org.dataflowanalysis.analysis.converter";
    private FlowGraph flowGraph;
    private DFDConfidentialityAnalysis analysis;
	
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
	
	public void initAnalysis(String model) {
	    analysis=buildAnalysis(model);
        analysis.initializeAnalysis();
        System.out.println(analysis.toString());
        flowGraph = analysis.findFlowGraph();
        flowGraph.evaluate();
	}
	
	public void runAnalysis() {
		for (AbstractPartialFlowGraph aPFG : flowGraph.getPartialFlowGraphs()) {
            List<? extends AbstractVertex<?>> violations = analysis.queryDataFlow(aPFG,node -> {
                //Rule0
                if(hasNodeCharacteristic(node, "annotation", "infrastructural") && hasDataCharacteristic(node, "annotation", "internal")) {
                    System.out.println(node.createPrintableNodeInformation());
                    return true;
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
	public void testConstraints() {
		List<String> models = getModelNames();
        for(String model : models) {
            System.out.println(model);
            initAnalysis(model);
        	runAnalysis();
        }
	}
	
	@Test
    public void convertAllToWeb() throws StandaloneInitializationException {
        List<String> models = getModelNames();
        for(String model : models) {
            System.out.println(model);
            var converter = new DataFlowDiagramConverter();
            var web = converter.dfdToWeb(PROJECT_NAME, model+".dataflowdiagram", model+".datadictionary", Activator.class);
            var web3 = converter.dfdToWeb(converter.webToDfd(web));
            converter.storeWeb(web, "../../bundles/org.dataflowanalysis.analysis.converter/"+model+".json");
            var web2=converter.loadWeb("../../bundles/org.dataflowanalysis.analysis.converter/"+model+".json").get();
            web.sort();
            web2.sort();
            web3.sort();
            assertEquals(web,web2);
            assertEquals(web,web3);
            assertEquals(web2,web3);;
        }
    }
	
	@Test
    public void convertAllToDFD() {
        List<String> models = getModelNames();
        for(String model : models) {
            System.out.println(model);
            var converter = new DataFlowDiagramConverter();
            var dfd = converter.webToDfd("../../bundles/org.dataflowanalysis.analysis.converter/"+model+".json");
            converter.dfdToWeb(dfd);
            converter.webToDfd(converter.dfdToWeb(dfd));
            converter.storeDFD(dfd, "../../bundles/org.dataflowanalysis.analysis.converter/"+model);
        }
    }

    private List<String> getModelNames() {
        String fileEnding = ".json";
        
        File directory = new File("../../bundles/org.dataflowanalysis.analysis.converter");
        File[] files = directory.listFiles((dir, name) -> name.endsWith(fileEnding));
        List<String> fileNames = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                String nameWithoutExtension = name.substring(0, name.length() - fileEnding.length());
                fileNames.add(nameWithoutExtension);
            }
        }
        return fileNames;
    }
	
	private boolean hasNodeCharacteristic(AbstractVertex<?> node, String type, String value) {
		if(node.getAllNodeCharacteristics().stream().anyMatch(n -> n.getTypeName().equals(type) && n.getValueName().equals(value))) {
				return true;
		}
		return false;
	}
	
	private boolean hasDataCharacteristic(AbstractVertex<?> node, String type, String value) {
		if(node.getAllDataFlowVariables().stream().anyMatch(v -> v.getAllCharacteristics().stream().anyMatch(c -> c.getTypeName().equals(type) && c.getValueName().equals(value)))){
			return true;
		}
		return false;
	}
}
