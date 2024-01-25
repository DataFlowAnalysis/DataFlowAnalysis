package org.dataflowanalysis.json2dfd;

import org.dataflowanalysis.dfd.datadictionary.*;
import org.dataflowanalysis.dfd.dataflowdiagram.*;
import org.dataflowanalysis.json2dfd.microsecend.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {

	public static void main(String[] args) {
		ObjectMapper objectMapper = new ObjectMapper();        
        File folder = new File("models");

        // Check if the provided path is a directory
        if (folder.isDirectory()) {
            // List all files in the directory
            File[] files = folder.listFiles();

            // Iterate through each file
            if (files != null) {
                for (File file : files) {
                    try {
                        // Parse each file into the SystemConfiguration class
                        SystemConfiguration systemConfiguration = objectMapper.readValue(file, SystemConfiguration.class);

                        // Process or print information from the parsed object as needed
                        System.out.println("Parsed JSON from file: " + file.getName());
                        // Perform additional processing if required
                        
                        List<Service> services = systemConfiguration.services();
                        System.out.println(systemConfiguration.informationFlows());

                    } catch (IOException e) {
                        System.err.println("Error parsing file: " + file.getName());
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.err.println("The provided path is not a directory.");
        }
	}
	
}
