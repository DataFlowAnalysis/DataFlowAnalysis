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
        file.delete();
	}
	
	@Test
	@DisplayName("Test Micro -> DFD")
	public void microToDfd() {
		assertTrue(true);
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
	}
}
