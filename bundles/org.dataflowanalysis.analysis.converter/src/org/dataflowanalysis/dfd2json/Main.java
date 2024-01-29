package org.dataflowanalysis.dfd2json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.dataflowanalysis.dfd2json.dfd.*;


public class Main {

	public static void main(String[] args) {
        
        new Parser().parse("anilallewar_microservices-basics-spring-boot.dataflowdiagram", "anilallewar_microservices-basics-spring-boot.datadictionary", null);
        
        ObjectMapper objectMapper = new ObjectMapper();
        try {
        	DFD dfd = objectMapper.readValue(new File("minimal.json"), DFD.class);
        	List<Child> children =dfd.model().children();
        	List<Child> nodes = new ArrayList<>();
        	List<Child> edges = new ArrayList<>();
        	for (Child child : children) {
        		if(child.type().contains("node")) {
        			nodes.add(child);
        		}
        		else if(child.type().contains("edge")) {
        			edges.add(child);
        		}
        	}
        	System.out.println(nodes);
        	System.out.println(edges);
        	
        }
        catch (IOException e) {
            System.err.println("Error parsing minimal: ");
            e.printStackTrace();
        }
    }
}
