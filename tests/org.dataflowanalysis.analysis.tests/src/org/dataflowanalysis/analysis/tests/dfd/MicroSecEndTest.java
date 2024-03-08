package org.dataflowanalysis.analysis.tests.dfd;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.io.File;

import org.dataflowanalysis.analysis.core.*;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.tests.Activator;
import org.dataflowanalysis.analysis.converter.*;
import org.junit.jupiter.api.Test;

import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;

public class MicroSecEndTest {
    public static String PROJECT_NAME = "org.dataflowanalysis.analysis.tests";
    private FlowGraph flowGraph;
    private DFDConfidentialityAnalysis analysis;
    private Map<Integer,Map<Integer,List<AbstractVertex<?>>>> violationsMap;
	
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
	
	public void runAnalysis(int variant) {
		for (AbstractPartialFlowGraph aPFG : flowGraph.getPartialFlowGraphs()) {
            analysis.queryDataFlow(aPFG,node -> {
                var violation=false;
                
                if((hasNodeCharacteristic(node, "Stereotype", "internal") && hasDataCharacteristic(node, "Stereotype", "entrypoint") && !(hasDataCharacteristic(node, "Stereotype", "gateway")))
                        || (hasNodeCharacteristic(node, "Stereotype", "gateway") && hasDataCharacteristic(node, "Stereotype", "internal"))
                        || (hasNodeCharacteristic(node, "Stereotype", "configuration_server") && hasDataCharacteristic(node, "Stereotype", "internal"))){
                    addToMap(violationsMap,variant,1,node);
                    violation=true;
                }
                
                if(hasDataCharacteristic(node, "Stereotype", "internal")&&!hasDataCharacteristic(node,"Stereotype","authenticated_request")){
                    addToMap(violationsMap,variant,2,node);
                    violation=true;
                }
                
            return violation;
        }
      );
        }
	}
	
	@Test
	public void testConstraints() {
		List<String> models = getModelNames();
		violationsMap=new HashMap<>();
        for(String model : models) {
            System.out.println(model);
            initAnalysis(model);
            var variant = Integer.parseInt(model.replaceAll(".*\\D+(\\d+)$", "$1"));
        	runAnalysis(variant);
        }
        for(int variant : violationsMap.keySet()) {
            System.out.println("Variant: "+variant);
            System.out.println("Broken rules: "+violationsMap.get(variant).keySet());
            System.out.println("");
        }
	}
	
	@Test
    public void convertAllToWeb() throws StandaloneInitializationException {
        List<String> models = getModelNames();
        for(String model : models) {
            System.out.println(model);
            var converter = new DataFlowDiagramConverter();
            var web = converter.dfdToWeb(PROJECT_NAME, model+".dataflowdiagram", model+".datadictionary", Activator.class);
            converter.storeWeb(web, model+".json");
        }
    }
	
	@Test
    public void convertAllToDFD() {
        List<String> models = getModelNames();
        for(String model : models) {
            System.out.println(model);
            var converter = new DataFlowDiagramConverter();
            var dfd = converter.webToDfd(model+".json");
            converter.storeDFD(dfd,model);
        }
    }

    private List<String> getModelNames() {
        String fileEnding = ".json";
        
        File directory = new File(".");
        File[] files = directory.listFiles((dir, name) -> name.endsWith(fileEnding));
        List<String> fileNames = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                String nameWithoutExtension = name.substring(0, name.length() - fileEnding.length());
                fileNames.add(nameWithoutExtension);
            }
        }
        Collections.sort(fileNames);
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
	
	private void addToMap(Map<Integer, Map<Integer, List<AbstractVertex<?>>>> map, int variant, int rule, AbstractVertex<?> node) {
	    map.putIfAbsent(variant, new HashMap<>());
	    
	    Map<Integer, List<AbstractVertex<?>>> secondaryMap = map.get(variant);
	    
	    if (!secondaryMap.containsKey(rule)) {
	        List<AbstractVertex<?>> list = new ArrayList<>();
	        list.add(node);
	        secondaryMap.put(rule, list);
	    } else {
	        secondaryMap.get(rule).add(node);
	    }
	}
}
