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
            new ProcessJSON().processMicro(file.getName().replaceAll("\\.json.*", ""),micro);
            System.out.println("Micro->DFD: " + file.getName());
        } 
        catch (IOException e) {
            System.err.println("Error parsing file: " + file.getName());
            e.printStackTrace();
        }
	}
	
	public static void readWeb(String path) {
		ObjectMapper objectMapper = new ObjectMapper();        
		File file = new File(path);
        try {
            DFD dfd = objectMapper.readValue(file, DFD.class);
            new ProcessJSON().processWeb(file.getName().replaceAll("\\.json.*", ""),dfd);
            System.out.println("Web->DFD: " + file.getName());
        } 
        catch (IOException e) {
            System.err.println("Error parsing file: " + file.getName());
            e.printStackTrace();
        }  
	}
	
	public static void readDFD(String name, String outfile){
		new ProcessDFD().parse(name+".dataflowdiagram", name+".datadictionary", outfile);
		System.out.println("DFD->Web: " + name);
	}

	public static void main(String[] args) {
		readMicro("anilallewar_microservices-basics-spring-boot.json");
        readWeb("minimal.json");
        readDFD("anilallewar_microservices-basics-spring-boot", "test.json"); 
        readDFD("minimal", "test2.json");
        readWeb("fullweb.json");
        readDFD("fullweb","test3.json");
	} 
}