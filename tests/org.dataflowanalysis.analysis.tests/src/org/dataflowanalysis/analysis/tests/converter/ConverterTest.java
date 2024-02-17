package org.dataflowanalysis.analysis.tests.converter;

import org.junit.jupiter.api.*;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.converter.*;
import org.dataflowanalysis.analysis.converter.microsecend.*;
import org.dataflowanalysis.analysis.converter.webdfd.*;
import org.dataflowanalysis.analysis.core.*;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.core.seff.*;
import org.dataflowanalysis.analysis.pcm.core.user.*;
import org.dataflowanalysis.analysis.testmodels.Activator;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;

public class ConverterTest {
	private static final String packagePath="../org.dataflowanalysis.analysis.testmodels/models/ConverterTest/";
	private Converter converter;
	
	@BeforeEach
	public void setup() {
		converter=new Converter();
	}
	
	@Test
	@DisplayName("Test Web -> DFD -> Web")
	public void webToDfdToWeb() throws StreamReadException, DatabindException, IOException {
		CompleteDFD dfdBefore= converter.webToDfd(packagePath+"minimal");
		DFD webAfter = converter.dfdToWeb(dfdBefore);
		
		ObjectMapper objectMapper = new ObjectMapper();        
		File file = new File(packagePath+"minimal.json");
		DFD webBefore = objectMapper.readValue(file, DFD.class);
                        
        webBefore.labelTypes().sort(Comparator.comparing(WebLabelType::id));
        webAfter.labelTypes().sort(Comparator.comparing(WebLabelType::id));
        
        List<Child> childrenBefore = webBefore.model().children();
        List<Child> childrenAfter = webAfter.model().children();

        childrenBefore.sort(Comparator.comparing(Child::id));
        childrenAfter.sort(Comparator.comparing(Child::id));
                
        List<Child> combined=new ArrayList<>(childrenBefore);
        combined.addAll(childrenAfter);
        for(Child child: combined) {
        	if(child.labels()!=null) {
            	child.labels().sort(Comparator.comparing(WebLabel::labelTypeId).thenComparing(WebLabel::labelTypeValueId));
        	}
        	if(child.ports() != null) {
            	child.ports().sort(Comparator.comparing(Port::id));

        	}
        }
        
        assertEquals(webBefore,webAfter);
	}
	
	@Test
	@DisplayName("Test Micro -> DFD")
	public void microToDfd() throws StreamReadException, DatabindException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		File file = new File(packagePath+"anilallewar.json");
		MicroSecEnd micro = objectMapper.readValue(file, MicroSecEnd.class);
		CompleteDFD complete=new ProcessJSON().processMicro(micro);
        
				
		DataFlowDiagram dfd = complete.dataFlowDiagram();
		@SuppressWarnings("unused")
		DataDictionary dd = complete.dataDictionary();
		
		assertEquals(micro.externalEntities().size()+micro.services().size(),dfd.getNodes().size());
		
		assertEquals(micro.informationFlows().size(),dfd.getFlows().size());
		
		for(Service service : micro.services()) {
			for(Node node : dfd.getNodes()) {
				if(service.name().equals(node.getEntityName())) {
					assertEquals(service.stereotypes().size(),node.getProperties().size());
					for(int i=0;i<service.stereotypes().size();i++) {
						assertEquals(service.stereotypes().get(i),node.getProperties().get(i).getEntityName());
					}
				}
			}
		}
		
		for(ExternalEntity ee : micro.externalEntities()) {
			for(Node node : dfd.getNodes()) {
				if(ee.name().equals(node.getEntityName())) {
					assertEquals(ee.stereotypes().size(),node.getProperties().size());
					for(int i=0;i<ee.stereotypes().size();i++) {
						assertEquals(ee.stereotypes().get(i),node.getProperties().get(i).getEntityName());
					}
				}
			}
		}
		
		int match = 0;
		for(InformationFlow iflow : micro.informationFlows()) {
			for(Flow flow:dfd.getFlows()) {
				if(iflow.sender().equals(flow.getSourceNode().getEntityName())&&iflow.receiver().equals(flow.getDestinationNode().getEntityName())) {
					Pin outpin=flow.getSourcePin();
					List<Pin> outpins = flow.getSourceNode().getBehaviour().getOutPin();
					assertTrue(outpins.contains(outpin));
					Assignment assignment = (Assignment) flow.getSourceNode().getBehaviour().getAssignment().get(outpins.indexOf(outpin));
					assertEquals(assignment.getOutputLabels().size(),flow.getSourceNode().getProperties().size());
					match++;
				}
			}
		}
		assertEquals(match,micro.informationFlows().size());
	}
	
	@Test
	@DisplayName("Test JSON -> Plant -> JSON")
	public void jsonToPlantToJson() throws StreamReadException, DatabindException, IOException {
        converter.runPythonScript(packagePath+"anilallewar.json", "txt", packagePath+"toPlant.txt");
        ObjectMapper objectMapper = new ObjectMapper();        
		File file = new File(packagePath+"anilallewar.json");
		MicroSecEnd microBefore = objectMapper.readValue(file, MicroSecEnd.class);
    
        converter.runPythonScript(packagePath+"toPlant.txt", "json", packagePath+"fromPlant.json");
        objectMapper = new ObjectMapper();        
		file = new File(packagePath+"fromPlant.json");
		MicroSecEnd microAfter = objectMapper.readValue(file, MicroSecEnd.class);
		
		
		microBefore.services().sort(Comparator.comparing(Service::name));
        microAfter.services().sort(Comparator.comparing(Service::name));
        
        microBefore.externalEntities().sort(Comparator.comparing(ExternalEntity::name));
        microAfter.externalEntities().sort(Comparator.comparing(ExternalEntity::name));
        
        microBefore.informationFlows().sort(Comparator.comparing(InformationFlow::sender).thenComparing(InformationFlow::receiver));
    	microAfter.informationFlows().sort(Comparator.comparing(InformationFlow::sender).thenComparing(InformationFlow::receiver));
        
        List<List<String>> allStereotypes = new ArrayList<>();
        allStereotypes.add(microBefore.services().stream().flatMap(node -> node.stereotypes().stream()).collect(Collectors.toList()));
        allStereotypes.add(microAfter.services().stream().flatMap(node -> node.stereotypes().stream()).collect(Collectors.toList()));
        allStereotypes.add(microBefore.externalEntities().stream().flatMap(node -> node.stereotypes().stream()).collect(Collectors.toList()));
        allStereotypes.add(microAfter.externalEntities().stream().flatMap(node -> node.stereotypes().stream()).collect(Collectors.toList()));
        allStereotypes.add(microBefore.informationFlows().stream().flatMap(node -> node.stereotypes().stream()).collect(Collectors.toList()));
        allStereotypes.add(microAfter.informationFlows().stream().flatMap(node -> node.stereotypes().stream()).collect(Collectors.toList()));
        for(List<String> stereotypes : allStereotypes) {
        	Collections.sort(stereotypes);
        }
        
		assertEquals(microBefore,microAfter);
				
		cleanup(packagePath+"toPlant.txt");
		cleanup(packagePath+"FromPlant.json");
	}
	
	@Test
	@DisplayName("Test Ass to DFD")
	public void assToDfd() {
		String name="TravelPlanner";
		String modelFileName="travelPlanner";
		String TEST_MODEL_PROJECT_NAME = "org.dataflowanalysis.analysis.testmodels";
		
		final var usageModelPath = Paths.get("models", name, modelFileName + ".usagemodel").toString();
		final var allocationPath = Paths.get("models", name, modelFileName + ".allocation").toString();
		final var nodeCharPath = Paths.get("models", name, modelFileName + ".nodecharacteristics").toString();
				
		DataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder()
		        .standalone()
		        .modelProjectName(TEST_MODEL_PROJECT_NAME)
		        .usePluginActivator(Activator.class)
		        .useUsageModel(usageModelPath)
		        .useAllocationModel(allocationPath)
		        .useNodeCharacteristicsModel(nodeCharPath)
		        .build();
		
		analysis.initializeAnalysis();
		analysis.findAllSequences();
		var sequences = analysis.findAllSequences();
		var propagationResult = analysis.evaluateDataFlows(sequences);
		
		Map<String,String> assIdToName = new HashMap<>();
		for(ActionSequence as : propagationResult) {
			for(AbstractActionSequenceElement<?> ase: as.getElements()) {
				if(ase instanceof SEFFActionSequenceElement) {
					var cast=(SEFFActionSequenceElement<?>) ase;
					assIdToName.putIfAbsent(cast.getElement().getId(), cast.getElement().getEntityName());
				}
				else if(ase instanceof CallingSEFFActionSequenceElement) {
					var cast=(CallingSEFFActionSequenceElement) ase;
					assIdToName.putIfAbsent(cast.getElement().getId(), cast.getElement().getEntityName());
				}
				else if(ase instanceof CallingUserActionSequenceElement) {
					var cast=(CallingUserActionSequenceElement) ase;
					assIdToName.putIfAbsent(cast.getElement().getId(), cast.getElement().getEntityName());
				}
				else {
					var cast=(UserActionSequenceElement<?>) ase;
					assIdToName.putIfAbsent(cast.getElement().getId(), cast.getElement().getEntityName());
				}
			}
		}
		
		ProcessASS ass2dfd = new ProcessASS();
		
		ass2dfd.transform(propagationResult);
		
		DataFlowDiagram dfd = ass2dfd.getDataFlowDiagram();
		
		assertEquals(dfd.getNodes().size(),assIdToName.keySet().size());
		
		List<String> nodeIds = new ArrayList<>();
		for(Node node :dfd.getNodes()) {
			nodeIds.add(node.getId());
		}
		Collections.sort(nodeIds);
		List<String> assIds=new ArrayList<>(assIdToName.keySet());
		Collections.sort(assIds);
		
		assertEquals(assIds,nodeIds);
		
		for(Node node : dfd.getNodes()) {
			assertEquals(node.getEntityName(),assIdToName.get(node.getId()));
		}

		List<String> flowNames=new ArrayList<>();
		for(ActionSequence as : propagationResult) {
			for(AbstractActionSequenceElement<?> ase: as.getElements()) {
				List<DataFlowVariable> variables=ase.getAllDataFlowVariables();
				for(DataFlowVariable variable:variables) {
					flowNames.add(variable.variableName());
				}
			}
		}

		assertEquals(flowNames.size(),dfd.getFlows().size());
		
	}
	
	@Test
	@DisplayName("Test storing functionality")
	public void testStore() throws StreamReadException, DatabindException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();        
		File file = new File(packagePath+"minimal.json");
		
		DFD webBefore = objectMapper.readValue(file, DFD.class);

        CompleteDFD complete=converter.webToDfd(webBefore);
        converter.store(complete, packagePath+"minimal");
        DFD webAfter=converter.dfdToWeb(packagePath+"minimal");
        
        webBefore.labelTypes().sort(Comparator.comparing(WebLabelType::id));
        webAfter.labelTypes().sort(Comparator.comparing(WebLabelType::id));
        
        List<Child> childrenBefore = webBefore.model().children();
        List<Child> childrenAfter = webAfter.model().children();

        childrenBefore.sort(Comparator.comparing(Child::id));
        childrenAfter.sort(Comparator.comparing(Child::id));
                
        List<Child> combined=new ArrayList<>(childrenBefore);
        combined.addAll(childrenAfter);
        for(Child child: combined) {
        	if(child.labels()!=null) {
            	child.labels().sort(Comparator.comparing(WebLabel::labelTypeId).thenComparing(WebLabel::labelTypeValueId));
        	}
        	if(child.ports() != null) {
            	child.ports().sort(Comparator.comparing(Port::id));

        	}
        }
        
        assertEquals(webBefore,webAfter);
        
        cleanup(packagePath+"minimal.datadictionary");
        cleanup(packagePath+"minimal.dataflowdiagram");
		
	}
	public void cleanup(String path) {
		File file = new File(path);
		file.delete();
	}
}
