package org.dataflowanalysis.converter.tests;

import org.junit.jupiter.api.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.dataflowanalysis.converter.*;
import org.dataflowanalysis.converter.microsecend.*;
import org.dataflowanalysis.converter.webdfd.*;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.dataflowanalysis.dfd.dataflowdiagram.dataflowdiagramPackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class ConverterTests {
	
	@Test
	@DisplayName("Test Web -> DFD -> Web")
	public void webToDfdToWeb() {
		Main.readWeb("minimal.json");
		Main.readDFD("minimal","test5.json");
		
		ObjectMapper objectMapper = new ObjectMapper();        
		File file = new File("minimal.json");
		DFD webBefore = null;
        try {
            webBefore = objectMapper.readValue(file, DFD.class);
        } 
        catch (IOException e) {}
        
        objectMapper = new ObjectMapper();        
		file = new File("test5.json");
		DFD webAfter = null;
        try {
            webAfter = objectMapper.readValue(file, DFD.class);
        } 
        catch (IOException e) {}
                
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
        cleanup("minimal.datadictionary");
        cleanup("minimal.dataflowdiagram");
        cleanup("test5.json");
	}
	
	@Test
	@DisplayName("Test Micro -> DFD")
	public void microToDfd() {
		MicroSecEnd micro = null;
		ObjectMapper objectMapper = new ObjectMapper();
		File file = new File("anilallewar_microservices-basics-spring-boot.json");
        try {
            micro = objectMapper.readValue(file, MicroSecEnd.class);
            new ProcessJSON().processMicro(file.getName().replaceAll("\\.json.*", ""),micro);
        } 
        catch (IOException e) {
            System.err.println("Error parsing file: " + file.getName());
            e.printStackTrace();
        }
		
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		rs.getPackageRegistry().put(dataflowdiagramPackage.eNS_URI, dataflowdiagramPackage.eINSTANCE);
		
		
		Resource dfdResource = rs.getResource(URI.createFileURI("anilallewar_microservices-basics-spring-boot.dataflowdiagram"), true);
		Resource ddResource = rs.getResource(URI.createFileURI("anilallewar_microservices-basics-spring-boot.datadictionary"), true);		
		
		DataFlowDiagram dfd = (DataFlowDiagram) dfdResource.getContents().get(0);
		DataDictionary dd = (DataDictionary) ddResource.getContents().get(0);
		
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
		
		cleanup("anilallewar_microservices-basics-spring-boot.dataflowdiagram");
		cleanup("anilallewar_microservices-basics-spring-boot.datadictionary");
	}
	
	@Test
	@DisplayName("Test JSON -> Plant -> JSON")
	public void jsonToPlantToJson() {
		MicroSecEnd microBefore = null;
		MicroSecEnd microAfter = null;

		try {
            String[] command = {"python3", "convert_model.py", "anilallewar_microservices-basics-spring-boot.json" , "txt", "-op", "toPlant.txt"};
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            process.waitFor();
            ObjectMapper objectMapper = new ObjectMapper();        
    		File file = new File("anilallewar_microservices-basics-spring-boot.json");
    		microBefore = objectMapper.readValue(file, MicroSecEnd.class);
        } 
		catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
		
		try {
			String[] command = {"python3", "convert_model.py", "toPlant.txt" , "json", "-op", "fromPlant.json"};
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            process.waitFor();
            ObjectMapper objectMapper = new ObjectMapper();        
    		File file = new File("fromPlant.json");
    		microAfter = objectMapper.readValue(file, MicroSecEnd.class);
		}catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
		
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
		
		cleanup("toPlant.txt");
		cleanup("FromPlant.json");
	}
	
	public static void cleanup(String path) {
		File file = new File(path);
		file.delete();
	}
}
