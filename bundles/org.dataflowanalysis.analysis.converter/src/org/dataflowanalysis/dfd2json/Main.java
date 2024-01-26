package org.dataflowanalysis.dfd2json;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.dataflowanalysis.dfd2json.dfd.*;
import org.dataflowanalysis.json2dfd.microsecend.SystemConfiguration;


public class Main {
	private String name;
    private int age;
    
    

    public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public int getAge() {
		return age;
	}



	public void setAge(int age) {
		this.age = age;
	}



	public static void main(String[] args) {
        Main obj = new Main();
        obj.name="John";
        obj.age=25;

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Serialize object to JSON and write to a file
            objectMapper.writeValue(new File("output.json"), obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        new Parser().parse("dfds/anilallewar_microservices-basics-spring-boot.dataflowdiagram", "dfds/anilallewar_microservices-basics-spring-boot.datadictionary", null);
        
        objectMapper = new ObjectMapper();
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
