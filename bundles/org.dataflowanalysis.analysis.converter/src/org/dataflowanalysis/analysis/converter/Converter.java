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

public class Converter {
	private ObjectMapper objectMapper;
	private File file;
	
	public Converter() {
		objectMapper = new ObjectMapper();
	}
	
	public void microToDfd(String path) {
		file = new File(path);
        try {
            MicroSecEnd micro = objectMapper.readValue(file, MicroSecEnd.class);
            new ProcessJSON().processMicro(path.replaceAll("\\.json.*", ""),micro);
        	System.out.println("Micro->DFD: " + file.getName());
        } 
        catch (IOException e) {
            System.err.println("Error parsing file: " + file.getName());
            e.printStackTrace();
        }
	}
	
	public void webToDfd(String path) {
		file = new File(path);
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
	
	public void dfdToWeb(String inputFile, String outputFile){
		new ProcessDFD().parse(inputFile+".dataflowdiagram", inputFile+".datadictionary", outputFile);
		System.out.println("DFD->Web: " + inputFile);
	}
	
	public void plantToMicro(String path) {
		String name = path.split("\\.")[0];
        int exitCode = runPythonScript(path,"json",name+".json");
        if(exitCode == 0) {
        	microToDfd(name+".json");
        	System.out.println("Plant->DFD: " + path);
        }
        else {
        	System.out.println("Check if python3 is set in PATH");
        }
        
	}
	
	public void assToDFD(String name, String modelFileName, String modelProjectName) {		
		final var usageModelPath = Paths.get("models", name, modelFileName + ".usagemodel").toString();
		final var allocationPath = Paths.get("models", name, modelFileName + ".allocation").toString();
		final var nodeCharPath = Paths.get("models", name, modelFileName + ".nodecharacteristics").toString();
				
		DataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder()
		        .standalone()
		        .modelProjectName(modelProjectName)
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
	
	public int runPythonScript(String in, String format, String out){
		String[] command = {"python3", "convert_model.py", in , format, "-op", out};

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process;
		try {
			process = processBuilder.start();
			return process.waitFor();
		} 
		catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return -1;
	}
}