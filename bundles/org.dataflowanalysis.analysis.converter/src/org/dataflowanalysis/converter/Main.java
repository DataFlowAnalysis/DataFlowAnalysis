package org.dataflowanalysis.converter;

import java.io.File;
import java.io.IOException;

import org.dataflowanalysis.converter.microsecend.*;
import org.dataflowanalysis.converter.webdfd.*;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
	public static void readMicro(String path) {
		ObjectMapper objectMapper = new ObjectMapper();        
		File file = new File(path);
        try {
            MicroSecEnd micro = objectMapper.readValue(file, MicroSecEnd.class);

            System.out.println("Parsed JSON from file: " + file.getName());
            
            new ProcessJSON().processMicro(file.getName().replaceAll("\\.json.*", ""),micro);
        } 
        catch (IOException e) {
            System.err.println("Error parsing file: " + file.getName());
            e.printStackTrace();
        }
	}
	
	public static void readWeb(String path) {
		ObjectMapper objectMapper = new ObjectMapper();        
		File file = new File("minimal.json");
        try {
            DFD dfd = objectMapper.readValue(file, DFD.class);

            System.out.println("Parsed JSON from file: " + file.getName());
          
            new ProcessJSON().processWeb(file.getName().replaceAll("\\.json.*", ""),dfd);
        } 
        catch (IOException e) {
            System.err.println("Error parsing file: " + file.getName());
            e.printStackTrace();
        }  
	}
	
	public static void readDFD(String name, String outfile){
		new ProcessDFD().parse(name+".dataflowdiagram", name+".datadictionary", outfile);
	}

	public static void main(String[] args) {
		readMicro("anilallewar_microservices-basics-spring-boot.json");
        readWeb("minimal.json");
        readDFD("anilallewar_microservices-basics-spring-boot", "test.json"); 
        readDFD("minimal", "test2.json");
	} 
}