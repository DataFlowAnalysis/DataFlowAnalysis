package org.dataflowanalysis.analysis.tests.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.converter.DataFlowDiagramAndDictionary;
import org.dataflowanalysis.analysis.converter.DataFlowDiagramConverter;
import org.dataflowanalysis.analysis.converter.PCMConverter;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.core.FlowGraph;
import org.dataflowanalysis.analysis.core.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.testmodels.Activator;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;

public class PCMTest {
    @Test
    @DisplayName("Test Palladio to DFD")
    public void palladioToDfd() {
        String modelLocation = "org.dataflowanalysis.analysis.testmodels";

        //testSpecificModel("CoronaWarnApp", "default", modelLocation,"cwa.json", null);
        testSpecificModel("TravelPlanner", "travelPlanner", modelLocation,"tp.json", null);
    }

    @Test
    @Disabled("There is currently no manually converted pcm model")
    @DisplayName("Test manual Palladio to DFD")
    public void manualPCMToDfd() throws StandaloneInitializationException {
        String modelLocation = "org.dataflowanalysis.analysis.testmodels";

        String inputModel = "InternationalOnlineShop";
        String inputFile = "default";
        String dataflowdiagram = Paths.get("models", "OnlineShopDFD", "onlineshop.dataflowdiagram").toString();
        String datadictionary = Paths.get("models", "OnlineShopDFD", "onlineshop.datadictionary").toString();
        testSpecificModel(inputModel, inputFile, modelLocation,null,
                new DataFlowDiagramConverter().loadDFD(modelLocation, dataflowdiagram, datadictionary, Activator.class));

    }

    private void testSpecificModel(String inputModel, String inputFile, String modelLocation,String webTarget, DataFlowDiagramAndDictionary complete) {
        final var usageModelPath = Paths.get("models", inputModel, inputFile + ".usagemodel").toString();
        final var allocationPath = Paths.get("models", inputModel, inputFile + ".allocation").toString();
        final var nodeCharPath = Paths.get("models", inputModel, inputFile + ".nodecharacteristics").toString();

        DataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone().modelProjectName(modelLocation)
                .usePluginActivator(Activator.class).useUsageModel(usageModelPath).useAllocationModel(allocationPath)
                .useNodeCharacteristicsModel(nodeCharPath).build();

        analysis.initializeAnalysis();
        var flowGraph = analysis.findFlowGraph();
        flowGraph.evaluate();

        Map<String, String> assIdToName = new HashMap<>();
        for (AbstractPartialFlowGraph aPFG : flowGraph.getPartialFlowGraphs()) {
            for (AbstractVertex<?> abstractVertex : aPFG.getVertices()) {
                var cast = (AbstractPCMVertex<?>) abstractVertex;
                assIdToName.putIfAbsent(cast.getReferencedElement().getId(), PCMConverter.computeCompleteName(cast));
            }
        }

        if (complete == null) {
            complete = new PCMConverter().pcmToDFD(modelLocation,usageModelPath,allocationPath,nodeCharPath,Activator.class);
        }
        
        if(webTarget!=null) {
            var dfdConverter = new DataFlowDiagramConverter();
            var web = dfdConverter.dfdToWeb(complete);
            dfdConverter.storeWeb(web, webTarget);
        }
        
        DataFlowDiagram dfd=complete.dataFlowDiagram();

        assertEquals(dfd.getNodes().size(), assIdToName.keySet().size());

        List<String> nodeIds = new ArrayList<>();
        for (Node node : dfd.getNodes()) {
            nodeIds.add(node.getId());
        }
        Collections.sort(nodeIds);
        List<String> assIds = new ArrayList<>(assIdToName.keySet());
        Collections.sort(assIds);

        assertEquals(assIds, nodeIds);

        for (Node node : dfd.getNodes()) {
            assertEquals(node.getEntityName(), assIdToName.get(node.getId()));
        }

        List<String> flowNames = new ArrayList<>();
        for (AbstractPartialFlowGraph as : flowGraph.getPartialFlowGraphs()) {
            for (AbstractVertex<?> ase : as.getVertices()) {
                List<DataFlowVariable> variables = ase.getAllDataFlowVariables();
                for (DataFlowVariable variable : variables) {
                    System.out.println(variable.variableName());
                    flowNames.add(variable.variableName());
                }
            }
        }

        assertEquals(flowNames.size(), dfd.getFlows().size());
        
        checkLabels(complete.dataDictionary(), flowGraph);
    }
    
    private void checkLabels(DataDictionary dd, FlowGraph flowGraph) {
        Map<String,CharacteristicValue> chars = new HashMap<>();
        for(var pfg : flowGraph.getPartialFlowGraphs()) {
            for(var vertex : pfg.getVertices()) {
                for(var nodeChar : vertex.getAllNodeCharacteristics()) {
                    chars.putIfAbsent(nodeChar.getValueId(),nodeChar);   
                }
                for(var dataVariable : vertex.getAllDataFlowVariables()) {
                    for(var dataChar : dataVariable.getAllCharacteristics()) {
                        chars.putIfAbsent(dataChar.getValueId(),dataChar); 
                    }
                }
            }
        }
        
        List<String> labelsPCM=chars.values().stream().map(c -> c.getTypeName()+"."+c.getValueName()).collect(Collectors.toList());
        List<String> labelsDFD=new ArrayList<>();
        
        Map<String,List<String>> labelMap= new HashMap<>();
        for(var labelType : dd.getLabelTypes()) {
            labelMap.put(labelType.getEntityName(), new ArrayList<>());
            for(var label : labelType.getLabel()) {
                var labels=labelMap.get(labelType.getEntityName());
                //prevent duplicate labels
                assertTrue(!labels.contains(label.getEntityName()));
                labels.add(label.getEntityName());
                labelMap.put(labelType.getEntityName(),labels); 
                labelsDFD.add(labelType.getEntityName()+"."+label.getEntityName());
            }
        }
        
        Collections.sort(labelsPCM);
        Collections.sort(labelsDFD);
        
        assertEquals(labelsPCM,labelsDFD);
    }
}