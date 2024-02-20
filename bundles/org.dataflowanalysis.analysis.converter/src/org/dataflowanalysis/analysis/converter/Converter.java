package org.dataflowanalysis.analysis.converter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.testmodels.Activator;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.dataflowdiagramPackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.dataflowanalysis.analysis.converter.microsecend.*;
import org.dataflowanalysis.analysis.converter.webdfd.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Converter {
    private ObjectMapper objectMapper;
    private File file;

    private final Logger logger = Logger.getLogger(Converter.class);

    public Converter() {
        objectMapper = new ObjectMapper();
    }

    public DataFlowDiagramAndDictionary microToDfd(String inputFile) {
        return microToDfd(loadMicro(inputFile));
    }

    public DataFlowDiagramAndDictionary microToDfd(MicroSecEnd inputFile) {
        return new ProcessJSON().processMicro(inputFile);
    }

    public DataFlowDiagramAndDictionary webToDfd(String inputFile) {
        return webToDfd(loadWeb(inputFile));
    }

    public DataFlowDiagramAndDictionary webToDfd(WebEditorDfd inputFile) {
        return new ProcessJSON().processWeb(inputFile);
    }

    public WebEditorDfd dfdToWeb(String inputFile) {
        DataFlowDiagramAndDictionary complete = loadDFD(inputFile);
        return new ProcessDFD().process(complete.dataFlowDiagram(), complete.dataDictionary());
    }

    public WebEditorDfd dfdToWeb(DataFlowDiagramAndDictionary complete) {
        return new ProcessDFD().process(complete.dataFlowDiagram(), complete.dataDictionary());
    }

    public DataFlowDiagramAndDictionary plantToDFD(String inputFile) {
        String name = inputFile.split("\\.")[0];
        int exitCode = runPythonScript(inputFile, "json", name + ".json");
        if (exitCode == 0) {
            return microToDfd(name + ".json");
        } else {
            logger.error("Make sure python3 is installed and set in PATH");
            return null;
        }

    }

    public DataFlowDiagramAndDictionary assToDFD(String inputModel, String inputFile, String modelLocation, String outputFile) {
        final var usageModelPath = Paths.get("models", inputModel, inputFile + ".usagemodel").toString();
        final var allocationPath = Paths.get("models", inputModel, inputFile + ".allocation").toString();
        final var nodeCharPath = Paths.get("models", inputModel, inputFile + ".nodecharacteristics").toString();

        DataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone().modelProjectName(modelLocation)
                .usePluginActivator(Activator.class).useUsageModel(usageModelPath).useAllocationModel(allocationPath)
                .useNodeCharacteristicsModel(nodeCharPath).build();

        analysis.initializeAnalysis();
        analysis.findAllSequences();
        var sequences = analysis.findAllSequences();
        var propagationResult = analysis.evaluateDataFlows(sequences);

        PalladioProcesser ass2dfd = new PalladioProcesser();

        return ass2dfd.process(propagationResult);
    }

    public int runPythonScript(String in, String format, String out) {
        String[] command = {"python3", "convert_model.py", in, format, "-op", out};

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process;
        try {
            process = processBuilder.start();
            return process.waitFor();
        } catch (IOException | InterruptedException e) {
            logger.error(e);
        }
        return -1;
    }

    public void store(WebEditorDfd web, String outputFile) {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            objectMapper.writeValue(new File(outputFile + ".json"), web);
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void store(DataFlowDiagramAndDictionary complete, String outputFile) {
        ResourceSet rs = new ResourceSetImpl();
        Resource dfdResource = createAndAddResource(outputFile + ".dataflowdiagram", new String[] {"dataflowdiagram"}, rs);
        Resource ddResource = createAndAddResource(outputFile + ".datadictionary", new String[] {"datadictionary"}, rs);

        dfdResource.getContents().add(complete.dataFlowDiagram());
        ddResource.getContents().add(complete.dataDictionary());

        saveResource(dfdResource);
        saveResource(ddResource);

    }

    public MicroSecEnd loadMicro(String inputFile) {
        objectMapper = new ObjectMapper();
        file = new File(inputFile + ".json");
        try {
            return objectMapper.readValue(file, MicroSecEnd.class);
        } catch (IOException e) {
            logger.error(e);
            return null;
        }
    }

    public WebEditorDfd loadWeb(String inputFile) {
        objectMapper = new ObjectMapper();
        file = new File(inputFile + ".json");
        try {
            return objectMapper.readValue(file, WebEditorDfd.class);
        } catch (IOException e) {
            logger.error(e);
            return null;
        }
    }

    public DataFlowDiagramAndDictionary loadDFD(String inputFile) {
        ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
        rs.getPackageRegistry().put(dataflowdiagramPackage.eNS_URI, dataflowdiagramPackage.eINSTANCE);

        Resource dfdResource = rs.getResource(URI.createFileURI(inputFile + ".dataflowdiagram"), true);
        Resource ddResource = rs.getResource(URI.createFileURI(inputFile + ".datadictionary"), true);

        DataFlowDiagram dfd = (DataFlowDiagram) dfdResource.getContents().get(0);
        DataDictionary dd = (DataDictionary) ddResource.getContents().get(0);

        return new DataFlowDiagramAndDictionary(dfd, dd);
    }

    private Resource createAndAddResource(String outputFile, String[] fileextensions, ResourceSet rs) {
        for (String fileext : fileextensions) {
            rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put(fileext, new XMLResourceFactoryImpl());
        }
        URI uri = URI.createFileURI(outputFile);
        Resource resource = rs.createResource(uri);
        ((ResourceImpl) resource).setIntrinsicIDToEObjectMap(new HashMap<>());
        return resource;
    }

    private void saveResource(Resource resource) {
        Map<Object, Object> saveOptions = ((XMLResource) resource).getDefaultSaveOptions();
        saveOptions.put(XMLResource.OPTION_CONFIGURATION_CACHE, Boolean.TRUE);
        saveOptions.put(XMLResource.OPTION_USE_CACHED_LOOKUP_TABLE, new ArrayList<>());
        try {
            resource.save(saveOptions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}