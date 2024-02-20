package org.dataflowanalysis.analysis.converter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
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

    public void store(DataFlowDiagramAndDictionary complete, String outputFile) {
        ResourceSet rs = new ResourceSetImpl();
        Resource dfdResource = createAndAddResource(outputFile + ".dataflowdiagram", new String[] {"dataflowdiagram"}, rs);
        Resource ddResource = createAndAddResource(outputFile + ".datadictionary", new String[] {"datadictionary"}, rs);

        dfdResource.getContents().add(complete.dataFlowDiagram());
        ddResource.getContents().add(complete.dataDictionary());

        saveResource(dfdResource);
        saveResource(ddResource);

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