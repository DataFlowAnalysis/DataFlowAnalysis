package org.dataflowanalysis.converter;

import com.fasterxml.jackson.databind.ObjectMapper;

import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.dfd.resource.DFDURIResourceProvider;
import org.dataflowanalysis.analysis.utils.ResourceUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;

public abstract class Converter {
    protected ObjectMapper objectMapper;
    protected File file;

    protected final Logger logger = Logger.getLogger(Converter.class);

    public Converter() {
        objectMapper = new ObjectMapper();
    }
    
    /**
     * Loads a data flow diagram and data dictionary from specified input files and returns them as a combined object.
     * @param inputDataFlowDiagram The path of the input data flow diagram file.
     * @param inputDataDictionary The path of the input data dictionary file.
     * @return DataFlowDiagramAndDictionary object representing the loaded data flow diagram and dictionary.
     */
    public DataFlowDiagramAndDictionary loadDFD(String project, String inputDataFlowDiagram, String inputDataDictionary, Class<?> activator)
            throws StandaloneInitializationException {
        StandaloneInitializerBuilder.builder()
                .registerProjectURI(activator, project)
                .build()
                .init();

        URI dfdURI = ResourceUtils.createRelativePluginURI(inputDataFlowDiagram, project);
        URI ddURI = ResourceUtils.createRelativePluginURI(inputDataDictionary, project);

        var provider = new DFDURIResourceProvider(dfdURI, ddURI);
        provider.loadRequiredResources();
        return new DataFlowDiagramAndDictionary(provider.getDataFlowDiagram(), provider.getDataDictionary());
    }

    /**
     * Stores a DataFlowDiagramAndDictionary object into a specified output file.
     * @param complete The DataFlowDiagramAndDictionary object to store.
     * @param outputFile The path of the output file.
     */
    public void storeDFD(DataFlowDiagramAndDictionary complete, String outputFile) {
        String fileEnding = ".json";
        String truncatedOutputFile = outputFile.endsWith(fileEnding) ? outputFile.substring(0, outputFile.length() - fileEnding.length())
                : outputFile;

        ResourceSet resourceSet = new ResourceSetImpl();
        Resource dfdResource = createAndAddResource(truncatedOutputFile + ".dataflowdiagram", new String[] {"dataflowdiagram"}, resourceSet);
        Resource ddResource = createAndAddResource(truncatedOutputFile + ".datadictionary", new String[] {"datadictionary"}, resourceSet);

        dfdResource.getContents()
                .add(complete.dataFlowDiagram());
        ddResource.getContents()
                .add(complete.dataDictionary());

        saveResource(dfdResource);
        saveResource(ddResource);
    }

    private Resource createAndAddResource(String outputFile, String[] fileextensions, ResourceSet resourceSet) {
        for (String fileextension : fileextensions) {
            resourceSet.getResourceFactoryRegistry()
                    .getExtensionToFactoryMap()
                    .put(fileextension, new XMLResourceFactoryImpl());
        }
        URI uri = URI.createFileURI(outputFile);
        return resourceSet.createResource(uri);
    }

    private void saveResource(Resource resource) {
        Map<Object, Object> saveOptions = ((XMLResource) resource).getDefaultSaveOptions();
        saveOptions.put(XMLResource.OPTION_URI_HANDLER, new FileNameOnlyURIHandler());
        try {
            resource.save(saveOptions);
        } catch (IOException e) {
            logger.error("Error saving DataFlowDiagram");
            throw new RuntimeException(e);
        }
    }
}