package org.dataflowanalysis.json2dfd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dataflowanalysis.json2dfd.dfdwriter.Producer;
import org.dataflowanalysis.json2dfd.microsecend.*;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {

	public static void main(String[] args) {
		ObjectMapper objectMapper = new ObjectMapper();        
		File file = new File("anilallewar_microservices-basics-spring-boot.json");
   	                	List<String> externalEntities = new ArrayList<>();
	                    List<String> services = new ArrayList<>();
	                    List<Flow> flows = new ArrayList<>();
	                    try {
	                        // Parse each file into the SystemConfiguration class
	                        SystemConfiguration systemConfiguration = objectMapper.readValue(file, SystemConfiguration.class);
	
	                        // Process or print information from the parsed object as needed
	                        System.out.println("Parsed JSON from file: " + file.getName());
	                        // Perform additional processing if required
	                        
	                        List<InformationFlow> iflows = systemConfiguration.informationFlows();
	                        for(InformationFlow flow : iflows) {
	                        	flows.add(new Flow(flow.sender(),flow.receiver()));
	                        }
	
	                        for(ExternalEntity ee : systemConfiguration.externalEntities()) {
	                        	externalEntities.add(ee.name());
	                        }
	                        for (Service service:systemConfiguration.services()) {
	                        	services.add(service.name());
	                        }
	                        
	                        new Producer().produce(file.getName().replaceAll("\\.json.*", ""),externalEntities,services,flows);
	
	                    } 
	                    catch (IOException e) {
	                        System.err.println("Error parsing file: " + file.getName());
	                        e.printStackTrace();
	                    }
                	}
    
            }
