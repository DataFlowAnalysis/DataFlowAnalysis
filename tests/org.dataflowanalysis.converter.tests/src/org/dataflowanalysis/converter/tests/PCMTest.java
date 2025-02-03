package org.dataflowanalysis.converter.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.pcm.core.CallReturnBehavior;
import org.dataflowanalysis.converter.DataFlowDiagramConverter;
import org.dataflowanalysis.converter.PCMConverter;
import org.dataflowanalysis.examplemodels.Activator;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.simple.DFDSimpleTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.dfd.datadictionary.AND;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.ForwardingAssignment;
import org.dataflowanalysis.dfd.datadictionary.LabelReference;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PCMTest extends ConverterTest{
    @Test
    @DisplayName("Test PCM2DFD TravelPlanner")
    public void travelToDfd() {
        testSpecificModel("TravelPlanner", "travelPlanner", TEST_MODELS, "tp.json", this::travelPlannerCondition);
    }
	
	@Test
    @DisplayName("Test PCM2DFD MaaS")
    public void maasToDfd() {
        testSpecificModel("MaaS_Ticket_System_base", "MaaS", TEST_MODELS, "maas.json", this::maasCondition);
    }
	
	@Test
    @DisplayName("Test PCM2DFD CWA")
    public void cwaToDfd() {
        testSpecificModel("CoronaWarnApp", "default", TEST_MODELS, "cwa.json", null);
    }
	
	@Test
	@DisplayName("Test PCM2DFD TravelPlanner Behavior")
	public void testTravelPlannerBehavior() {
		final var usageModelPath = Paths.get("casestudies", "TravelPlanner", "travelPlanner.usagemodel")
                .toString();
        final var allocationPath = Paths.get("casestudies", "TravelPlanner", "travelPlanner.allocation")
                .toString();
        final var nodeCharPath = Paths.get("casestudies", "TravelPlanner",  "travelPlanner.nodecharacteristics")
                .toString();

        DataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODELS)
                .usePluginActivator(Activator.class)
                .useUsageModel(usageModelPath)
                .useAllocationModel(allocationPath)
                .useNodeCharacteristicsModel(nodeCharPath)
                .build();
        
        analysis.setLoggerLevel(Level.ALL);

        analysis.initializeAnalysis();
        var flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();

       	List<AbstractPCMVertex<?>> vertices = new ArrayList<>();
        for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            for (AbstractVertex<?> abstractVertex : transposeFlowGraph.getVertices()) {
            	AbstractPCMVertex<?> v = (AbstractPCMVertex<?>) abstractVertex;
               vertices.add(v);
            }
        }
        
        var dfd = new PCMConverter().pcmToDFD(TEST_MODELS, usageModelPath, allocationPath, nodeCharPath, Activator.class);
        
        // Assignment: flights.*.* := RETURN.*.*
        var readFlightsFromDB = dfd.dataFlowDiagram().getNodes().stream()
        		.filter(it -> it.getId().equals("_x32bcPViEeuMKba1Qn68bg_1"))
        		.findAny().orElseThrow();
        assertTrue(readFlightsFromDB.getBehavior().getAssignment().stream()
        		.filter(ForwardingAssignment.class::isInstance)
        		.filter(it -> ((ForwardingAssignment)it).getInputPins().size() == 1)
        		.filter(it -> ((ForwardingAssignment)it).getInputPins().get(0).getEntityName().equals("RETURN"))
        		.anyMatch(it -> it.getOutputPin().getEntityName().equals("flights")));
        
        // Assignment: RETURN.GrantedRoles.* := query.GrantedRoles.* & flight.GrantedRoles.*
        var selectFlightsBasedOnQuery = dfd.dataFlowDiagram().getNodes().stream()
        		.filter(it -> it.getId().equals("_2AAjoPViEeuMKba1Qn68bg"))
        		.findAny().orElseThrow();
        assertEquals(2, selectFlightsBasedOnQuery.getBehavior().getAssignment().stream()
        		.filter(Assignment.class::isInstance)
        		.map(Assignment.class::cast)
        		.filter(it -> it.getOutputPin().getEntityName().equals("RETURN"))
        		.filter(it -> it.getOutputLabels().size() == 1)
        		.filter(it -> ((LabelType) it.getOutputLabels().get(0).eContainer()).getEntityName().equals("GrantedRoles"))
        		.map(it -> it.getTerm())
        		.filter(AND.class::isInstance)
        		.map(AND.class::cast)
        		.filter(it -> it.getTerms().size() == 2)
        		.filter(it -> ((LabelType)((LabelReference) it.getTerms().get(0)).getLabel().eContainer()).getEntityName().equals("GrantedRoles"))
        		.filter(it -> ((LabelType)((LabelReference) it.getTerms().get(1)).getLabel().eContainer()).getEntityName().equals("GrantedRoles"))
        		.toList().size());
	}
    
    private void testSpecificModel(String inputModel, String inputFile, String modelLocation, String webTarget, Predicate<AbstractVertex<?>> constraint) {
        final var usageModelPath = Paths.get("casestudies", inputModel, inputFile + ".usagemodel")
                .toString();
        final var allocationPath = Paths.get("casestudies", inputModel, inputFile + ".allocation")
                .toString();
        final var nodeCharPath = Paths.get("casestudies", inputModel, inputFile + ".nodecharacteristics")
                .toString();

        DataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone()
                .modelProjectName(modelLocation)
                .usePluginActivator(Activator.class)
                .useUsageModel(usageModelPath)
                .useAllocationModel(allocationPath)
                .useNodeCharacteristicsModel(nodeCharPath)
                .build();
        
        analysis.setLoggerLevel(Level.ALL);

        analysis.initializeAnalysis();
        var flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();

       	List<AbstractPCMVertex<?>> vertices = new ArrayList<>();
        for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            for (AbstractVertex<?> abstractVertex : transposeFlowGraph.getVertices()) {
            	AbstractPCMVertex<?> v = (AbstractPCMVertex<?>) abstractVertex;
               vertices.add(v);
            }
        }

        
        var complete = new PCMConverter().pcmToDFD(modelLocation, usageModelPath, allocationPath, nodeCharPath, Activator.class);

        var dfdConverter = new DataFlowDiagramConverter();
        List<Predicate<? super AbstractVertex<?>>> constraints = new ArrayList<>();
        constraints.add(constraint);
        var web = dfdConverter.dfdToWebAndAnalyzeAndAnnotateWithCustomTFGFinder(complete, constraints, DFDSimpleTransposeFlowGraphFinder.class); //Replace null with simpleFinder once Analysis PR merged
        dfdConverter.storeWeb(web, webTarget);

        var dfd = complete.dataFlowDiagram();
        var dd = complete.dataDictionary();
        

        assertEquals(dfd.getNodes().size(), vertices.size());
        
        if (constraint != null) {
        	DFDSimpleTransposeFlowGraphFinder dfdTransposeFlowGraphFinder = new DFDSimpleTransposeFlowGraphFinder(dd, dfd);
            var dfdTFGCollection = dfdTransposeFlowGraphFinder.findTransposeFlowGraphs().
            		stream().map(it -> {return it.evaluate();}
            ).toList();
	        List<String> nodeIds = new ArrayList<>();
	        for (Node node : dfd.getNodes()) {
	            nodeIds.add(node.getId());
	        }        
       
        	List<AbstractVertex<?>> results = flowGraph.getTransposeFlowGraphs()
                    .stream()
                    .map(it -> analysis.queryDataFlow(it, constraint))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        	
    	
        	DFDConfidentialityAnalysis dfdAnalysis = new DFDConfidentialityAnalysis(null, null, TEST_JSONS);
        	List<AbstractVertex<?>> dfdResults = dfdTFGCollection
                .stream()
                .map(it -> dfdAnalysis.queryDataFlow(it, constraint))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        	
        	assertEquals(results.size(), dfdResults.size());
            checkTFGs(flowGraph, dfdTFGCollection);
        }
          
        checkLabels(dd, flowGraph);
        checkIDPreserving(flowGraph, dfd);
        checkNames(flowGraph, dfd);
    }
    
    private void checkIDPreserving(FlowGraphCollection pcmFlowGraphs, DataFlowDiagram dfd) {
    	List<String> ids = pcmFlowGraphs.getTransposeFlowGraphs().stream()
    			.map(AbstractTransposeFlowGraph::getVertices)
    			.flatMap(List::stream)
    			.filter(it -> it instanceof AbstractPCMVertex<?>)
    			.map(it -> (AbstractPCMVertex<?>) it)
    			.map(it -> it.getReferencedElement().getId())
    			.toList();
    	List<Node> nodes = dfd.getNodes();
    	for (Node node : nodes) {
    		String dfdId = node.getId();
    		if (ids.contains(dfdId)) {
    			continue;
    		}
    		int suffixIndex = dfdId.lastIndexOf('_');
    		String strippedId = dfdId.substring(0, suffixIndex);
    		assertTrue(ids.contains(strippedId), "Could not find PCM Vertex with ID: " + dfdId + " / " + strippedId);
    	}
    }
        
    private void checkNames(FlowGraphCollection pcmFlowGraphs, DataFlowDiagram dfd) {
        var vertices = pcmFlowGraphs.getTransposeFlowGraphs()
                .stream()
                .map(AbstractTransposeFlowGraph::getVertices)
                .flatMap(List::stream)
                .filter(it -> it instanceof AbstractPCMVertex<?>)
                .map(it -> (AbstractPCMVertex<?>) it)
                .toList();

        Map<String, String> nameMapping = new HashMap<>();

        for (var vertex : vertices) {
			nameMapping.putIfAbsent(vertex.getReferencedElement().getId(),
						vertex.getReferencedElement().getEntityName());
        }
        
    	List<Node> nodes = dfd.getNodes();
    	for (Node node : nodes) {
    		String dfdId = node.getId();
			String nodeNameStripped = node.getEntityName().replace("Calling ", "").replace("Returning ", "");
    		if (nameMapping.containsKey(dfdId)) {
    			if (nameMapping.get(dfdId).equals("aName")) {
    				continue;
    			}
    			assertEquals(nameMapping.get(dfdId), nodeNameStripped);
    			continue;
    		}
    		int suffixIndex = dfdId.lastIndexOf('_');
    		String strippedId = dfdId.substring(0, suffixIndex);
    		
    		if (!nameMapping.containsKey(strippedId)) {
    			fail("Could not find PCM Vertex with the transformed DFD IDs: " + dfdId + " / " + strippedId);
    		}
			if (nameMapping.get(strippedId).equals("aName")) {
				continue;
			}
            assertEquals(nodeNameStripped, nameMapping.get(strippedId), "Could not find PCM Vertex with ID: " + dfdId + " / " + strippedId);
    	}
    }
    
    private void checkTFGs(FlowGraphCollection pcmFlowGraphs, List<AbstractTransposeFlowGraph> dfdFlowGraphs) {
    	assertEquals(pcmFlowGraphs.getTransposeFlowGraphs().size(), dfdFlowGraphs.size());
    }
        
    private void checkLabels(DataDictionary dd, FlowGraphCollection flowGraph) {
       Set<CharacteristicValue> values = new HashSet<>();
        for (var pfg : flowGraph.getTransposeFlowGraphs()) {
            for (var vertex : pfg.getVertices()) {
                for (var nodeChar : vertex.getAllVertexCharacteristics()) {
                	values.add(nodeChar);
                }
                for (var dataChar : vertex.getAllIncomingDataCharacteristics()) {
                	for (var charValue : dataChar.getAllCharacteristics()) {
                		values.add(charValue);
                	}
                }
            }
        }

        List<String> labelsPCM = values
                .stream()
                .map(c -> c.getTypeName() + "." + c.getValueName())
                .collect(Collectors.toList());
        List<String> labelsDFD = new ArrayList<>();

        Map<String, List<String>> labelMap = new HashMap<>();
        for (var labelType : dd.getLabelTypes()) {
            labelMap.put(labelType.getEntityName(), new ArrayList<>());
            for (var label : labelType.getLabel()) {
                var labels = labelMap.get(labelType.getEntityName());
                // prevent duplicate labels
                assertTrue(!labels.contains(label.getEntityName()));
                labels.add(label.getEntityName());
                labelMap.put(labelType.getEntityName(), labels);
                labelsDFD.add(labelType.getEntityName() + "." + label.getEntityName());
            }
        }

        Collections.sort(labelsPCM);
        Collections.sort(labelsDFD);

        assertEquals(labelsPCM, labelsDFD);
    }
    
    private boolean travelPlannerCondition(AbstractVertex<?> node) {
        List<String> assignedRoles = node.getVertexCharacteristics("AssignedRoles")
                .stream()
                .map(CharacteristicValue::getValueName)
                .toList();
        Collection<List<CharacteristicValue>> grantedRoles = node.getDataCharacteristicMap("GrantedRoles")
                .values();


        for (List<CharacteristicValue> dataFlowCharacteristics : grantedRoles) {
            if (!dataFlowCharacteristics.isEmpty() && dataFlowCharacteristics.stream()
                    .distinct()
                    .map(CharacteristicValue::getValueName)
                    .noneMatch(assignedRoles::contains)) {
                return true;
            }
        }
        return false;
    }     
    
    private boolean maasCondition(AbstractVertex<?> vertex) {        
        var nodeLabels = retrieveNodeLabels(vertex);
        var dataLabels = retrieveDataLabels(vertex);
        var dataLabelTypes = retrieveDataLabelsTypes(vertex);

        if ((!dataLabels.contains("Encrypted") || dataLabels.contains("FineGranular")) &&
    		dataLabels.contains("Customer") &&
    		(dataLabels.contains("STS") || dataLabels.contains("LTS") || dataLabels.contains("TripData")) &&
    		!nodeLabels.contains("Customer")) 
    		return true;
            

        // Constraint 2: Ticket inspectors must not be able to trace past trips of customers c        
		if (dataLabels.contains("Vehicle") &&
    		dataLabels.contains("VehicleData") &&
    		dataLabels.contains("Customer") &&
    		dataLabels.contains("TripData") &&
    		nodeLabels.contains("Inspector"))
			return true;
         

        // Constraint 3: No granular information about individual trips of customer must be visible to the company
        
		if (dataLabels.contains("FineGranular") &&
			dataLabels.contains("Customer") &&
			(dataLabels.contains("STS") || dataLabels.contains("LTS") || dataLabels.contains("TripData")) &&
			nodeLabels.contains("MobilityProvider"))
			return true;
            

        
        if (dataLabels.contains("FineGranular") &&
    		dataLabels.contains("Customer") &&
    		dataLabels.contains("TripData") &&
    		(nodeLabels.contains("SupportStaff") || nodeLabels.contains("BillingStaff") 
				|| nodeLabels.contains("AnalysisStaff") || nodeLabels.contains("Administrators")))
    		return true;   

        

        return 	dataLabelTypes.contains("writeActions") &&
        		nodeLabels.contains("Write");
           
    
    }   

	private List<String> retrieveNodeLabels(AbstractVertex<?> vertex) {
	    return vertex.getAllVertexCharacteristics()
	            .stream()
	            .map(CharacteristicValue.class::cast)
	            .map(CharacteristicValue::getValueName)
	            .toList();
	}

	private List<String> retrieveDataLabels(AbstractVertex<?> vertex) {
	    return vertex.getAllDataCharacteristics()
	            .stream()
	            .map(DataCharacteristic::getAllCharacteristics)
	            .flatMap(List::stream)
	            .map(CharacteristicValue.class::cast)
	            .map(CharacteristicValue::getValueName)
	            .toList();
	}
	
	private List<String> retrieveDataLabelsTypes(AbstractVertex<?> vertex) {
        return vertex.getAllDataCharacteristics()
                .stream()
                .map(DataCharacteristic::getAllCharacteristics)
                .flatMap(List::stream)
                .map(CharacteristicValue.class::cast)
                .map(CharacteristicValue::getTypeName)
                .toList();
    }
}
