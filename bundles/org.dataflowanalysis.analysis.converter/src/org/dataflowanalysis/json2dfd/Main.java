package org.dataflowanalysis.json2dfd;

import java.io.File;
import java.io.IOException;

import org.dataflowanalysis.json2dfd.dfdwriter.Producer;
import org.dataflowanalysis.json2dfd.microsecend.*;
import org.dataflowanalysis.dfd2json.Parser;
import org.dataflowanalysis.dfd2json.dfd.*;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
	public static void readMicro(String path) {
		ObjectMapper objectMapper = new ObjectMapper();        
		File file = new File(path);
        try {
            SystemConfiguration systemConfiguration = objectMapper.readValue(file, SystemConfiguration.class);

            System.out.println("Parsed JSON from file: " + file.getName());
            
            new Producer().produceFromMicro(file.getName().replaceAll("\\.json.*", ""),systemConfiguration);
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
          
            new Producer().produceFromWeb(file.getName().replaceAll("\\.json.*", ""),dfd);
        } 
        catch (IOException e) {
            System.err.println("Error parsing file: " + file.getName());
            e.printStackTrace();
        }  
	}
	
	public static void readDFD(String name, String outfile){
		new Parser().parse(name+".dataflowdiagram", name+".datadictionary", outfile);
	}

	public static void main(String[] args) {
		readMicro("anilallewar_microservices-basics-spring-boot.json");
        readWeb("minimal.json");
        readDFD("anilallewar_microservices-basics-spring-boot", "test.json"); 
        readDFD("minimal", "test2.json");
	} 
}