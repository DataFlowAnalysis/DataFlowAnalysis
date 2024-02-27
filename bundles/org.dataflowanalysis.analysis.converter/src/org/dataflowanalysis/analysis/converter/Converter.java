package org.dataflowanalysis.analysis.converter;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class Converter {
    protected ObjectMapper objectMapper;
    protected File file;

    protected final Logger logger = Logger.getLogger(Converter.class);

    public Converter() {
        objectMapper = new ObjectMapper();
    }

    /**
     * Stores a DataFlowDiagramAndDictionary object into a specified output file.
     * @param complete The DataFlowDiagramAndDictionary object to store.
     * @param outputFile The path of the output file.
     */
    public void store(DataFlowDiagramAndDictionary complete, String outputFile) {
        String truncatedOutputFile;
        if (outputFile.endsWith(".json")) {
            truncatedOutputFile = outputFile.substring(0, outputFile.length() - 5);
        } else {
            truncatedOutputFile = outputFile;
        }
        ResourceSet resourceSet = new ResourceSetImpl();
        Resource dfdResource = createAndAddResource(truncatedOutputFile + ".dataflowdiagram", new String[] {"dataflowdiagram"}, resourceSet);
        Resource ddResource = createAndAddResource(truncatedOutputFile + ".datadictionary", new String[] {"datadictionary"}, resourceSet);

        dfdResource.getContents().add(complete.dataFlowDiagram());
        ddResource.getContents().add(complete.dataDictionary());

        saveResource(dfdResource);
        saveResource(ddResource);

    }

    private Resource createAndAddResource(String outputFile, String[] fileextensions, ResourceSet resourceSet) {
        for (String fileextension : fileextensions) {
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(fileextension, new XMLResourceFactoryImpl());
        }
        URI uri = URI.createFileURI(outputFile);
        return resourceSet.createResource(uri);
    }

    private void saveResource(Resource resource) {
        Map<Object, Object> saveOptions = ((XMLResource) resource).getDefaultSaveOptions();
        try {
            resource.save(saveOptions);
        } catch (IOException e) {
            logger.error("Error saving DataFlowDiagram");
            throw new RuntimeException(e);
        }
    }
}