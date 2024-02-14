package org.dataflowanalysis.analysis.converter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.testmodels.Activator;
import org.dataflowanalysis.analysis.converter.microsecend.*;
import org.dataflowanalysis.analysis.converter.webdfd.*;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
	public static void readMicro(String path) {
		readMicro(path,true);
	}
	
	public static void readMicro(String path, boolean direct) {
		
		ObjectMapper objectMapper = new ObjectMapper();        
		File file = new File(path);
        try {
            MicroSecEnd micro = objectMapper.readValue(file, MicroSecEnd.class);
            new ProcessJSON().processMicro(path.replaceAll("\\.json.*", ""),micro);
            if(direct) {
            	System.out.println("Micro->DFD: " + file.getName());
            } 
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
            new ProcessJSON().processWeb(path.replaceAll("\\.json.*", ""),dfd);
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
	
	public static void readPlant(String path) {
		String name = path.split("\\.")[0];
        int exitCode = runPython(path,"json",name+".json");
        if(exitCode == 0) {
        	readMicro(name+".json",false);
        	System.out.println("Plant->DFD: " + path);
        }
        else {
        	System.out.println("Check if python3 is set in PATH");
        }
        
	}
	
	public static void readAss(String name, String modelFileName) {
		String TEST_MODEL_PROJECT_NAME = "org.dataflowanalysis.analysis.testmodels";
		
		final var usageModelPath = Paths.get("models", name, modelFileName + ".usagemodel").toString();
		final var allocationPath = Paths.get("models", name, modelFileName + ".allocation").toString();
		final var nodeCharPath = Paths.get("models", name, modelFileName + ".nodecharacteristics").toString();
				
		DataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder()
		        .standalone()
		        .modelProjectName(TEST_MODEL_PROJECT_NAME)
		        .usePluginActivator(Activator.class)
		        .useUsageModel(usageModelPath)
		        .useAllocationModel(allocationPath)
		        .useNodeCharacteristicsModel(nodeCharPath)
		        .build();
		
		analysis.initializeAnalysis();
		analysis.findAllSequences();
		var sequences = analysis.findAllSequences();
		var propagationResult = analysis.evaluateDataFlows(sequences);
		
		ProcessASS ass2dfd = new ProcessASS();
		
		ass2dfd.transform(propagationResult);
		
		ass2dfd.saveModel(name + ".datadictionary", "datadictionary", ass2dfd.getDictionary());
		ass2dfd.saveModel(name + ".dataflowdiagram", "dataflowdiagram", ass2dfd.getDataFlowDiagram());
	}
	
	public static int runPython(String in, String format, String out){
		String[] command = {"python3", "convert_model.py", in , format, "-op", out};

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process;
		try {
			process = processBuilder.start();
			return process.waitFor();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	

	public static void main(String[] args) {
		File directory = new File(".");
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".json"));
        for(File file:files) {
        	readMicro(file.getName());
        	readDFD(file.getName().replaceAll("\\.json.*", ""),"web_"+file.getName());
        	try {
            	Thread.sleep(200);
            }catch(InterruptedException e) {}
        }
        
	} 
}