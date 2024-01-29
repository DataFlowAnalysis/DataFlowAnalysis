package org.dataflowanalysis.json2dfd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dataflowanalysis.json2dfd.dfdwriter.Producer;
import org.dataflowanalysis.json2dfd.microsecend.*;

import org.dataflowanalysis.dfd2json.dfd.*;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {

	public static void main(String[] args) {
		ObjectMapper objectMapper = new ObjectMapper();        
		File file = new File("anilallewar_microservices-basics-spring-boot.json");
        	List<String> externalEntities = new ArrayList<>();
            List<String> services = new ArrayList<>();
            List<Flow> flows = new ArrayList<>();
            try {
                SystemConfiguration systemConfiguration = objectMapper.readValue(file, SystemConfiguration.class);

                System.out.println("Parsed JSON from file: " + file.getName());
                
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
              
                new Producer().produceFromMicro(file.getName().replaceAll("\\.json.*", ""),externalEntities,services,flows);
            } 
        catch (IOException e) {
            System.err.println("Error parsing file: " + file.getName());
            e.printStackTrace();
        }
            
            objectMapper = new ObjectMapper();        
    		file = new File("minimal.json");
            try {
                DFD dfd = objectMapper.readValue(file, DFD.class);

                System.out.println("Parsed JSON from file: " + file.getName());
              
                new Producer().produceFromWeb(file.getName().replaceAll("\\.json.*", ""),dfd);
            } 
            catch (IOException e) {
                System.err.println("Error parsing file: " + file.getName());
                e.printStackTrace();
            }    
	} 
}