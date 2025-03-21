package org.dataflowanalysis.converter.dfd2web;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import org.dataflowanalysis.analysis.dfd.resource.DFDURIResourceProvider;
import org.dataflowanalysis.analysis.utils.ResourceUtils;
import org.dataflowanalysis.converter.ModelType;
import org.dataflowanalysis.converter.PersistableConverterModel;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;

/**
 * This class represents the required models to run the DFD data flow analysis and the two models in the DFD Metamodel
 */
public final class DataFlowDiagramAndDictionary extends PersistableConverterModel {
    private static final String FILE_EXTENSION_DFD = ".dataflowdiagram";
    private static final String FILE_EXTENSION_DD = ".datadictionary";

    private final DataFlowDiagram dataFlowDiagram;
    private final DataDictionary dataDictionary;

    /**
     * Create a new {@link DataFlowDiagramAndDictionary} with the given data flow diagram and data dictionary
     * @param dataFlowDiagram Given data flow diagram
     * @param dataDictionary Given data dictionary
     */
    public DataFlowDiagramAndDictionary(DataFlowDiagram dataFlowDiagram, DataDictionary dataDictionary) {
        super(ModelType.DFD);
        this.dataFlowDiagram = dataFlowDiagram;
        this.dataDictionary = dataDictionary;
    }

    /**
     * Loads a {@link DataFlowDiagramAndDictionary} using the provided paths to a data flow diagram and dictionary.
     * <p/>
     * This method uses eclipse platform URIs and cannot be run standalone
     * @param modelProjectName Name of the modelling project the files are contained in
     * @param inputDataFlowDiagram Relative path to the data flow diagram starting at the modelling project root
     * @param inputDataDictionary Relative path to the data dictionary starting at the modelling project root
     * @param activator Plugin activator that used to register the modelling project
     * @throws StandaloneInitializationException Thrown when initialization of the project via the model project name and activator fails
     */
    public DataFlowDiagramAndDictionary(String modelProjectName, String inputDataFlowDiagram, String inputDataDictionary, Class<?> activator)
            throws StandaloneInitializationException {
        super(ModelType.DFD);
        StandaloneInitializerBuilder.builder()
                .registerProjectURI(activator, modelProjectName)
                .build()
                .init();

        if (!inputDataFlowDiagram.endsWith(FILE_EXTENSION_DFD)) inputDataFlowDiagram = inputDataFlowDiagram + FILE_EXTENSION_DFD;
        if(!inputDataDictionary.endsWith(FILE_EXTENSION_DD)) inputDataDictionary = inputDataDictionary + FILE_EXTENSION_DD;

        URI dfdURI = ResourceUtils.createRelativePluginURI(inputDataFlowDiagram, modelProjectName);
        URI ddURI = ResourceUtils.createRelativePluginURI(inputDataDictionary, modelProjectName);

        var provider = new DFDURIResourceProvider(dfdURI, ddURI);
        provider.loadRequiredResources();
        this.dataFlowDiagram = provider.getDataFlowDiagram();
        this.dataDictionary = provider.getDataDictionary();
    }

    /**
     * Loads a {@link DataFlowDiagramAndDictionary} using the provided paths to a data flow diagram and dictionary.
     * <p/>
     * This method uses file URIs and can be run standalone
     * @param inputDataFlowDiagram Absolute or relative path to the data flow diagram
     * @param inputDataDictionary Absolute or relative path to the data flow diagram
     */
    public DataFlowDiagramAndDictionary(String inputDataFlowDiagram, String inputDataDictionary) {
        super(ModelType.DFD);
        if (!inputDataFlowDiagram.endsWith(FILE_EXTENSION_DFD)) inputDataFlowDiagram = inputDataFlowDiagram + FILE_EXTENSION_DFD;
        if(!inputDataDictionary.endsWith(FILE_EXTENSION_DD)) inputDataDictionary = inputDataDictionary + FILE_EXTENSION_DD;

        URI dfdURI = URI.createFileURI(Paths.get(inputDataFlowDiagram)
                .toAbsolutePath()
                .normalize()
                .toString());
        URI ddURI = URI.createFileURI(Paths.get(inputDataDictionary)
                .toAbsolutePath()
                .normalize()
                .toString());

        var provider = new DFDURIResourceProvider(dfdURI, ddURI);
        provider.setupResources();
        provider.loadRequiredResources();
        this.dataFlowDiagram = provider.getDataFlowDiagram();
        this.dataDictionary = provider.getDataDictionary();
    }

    /**
     * Creates a new {@link DataFlowDiagramAndDictionary} interactively from the command line using the provided scanner
     * @param scanner Provided scanner to read from
     */
    public DataFlowDiagramAndDictionary(Scanner scanner) {
        super(ModelType.DFD);

        String inputDataFlowDiagram = this.getFilePath(scanner, "dataflowdiagram");
        String inputDataDictionary = this.getFilePath(scanner, "datadictionary");

        URI dfdURI = URI.createFileURI(inputDataFlowDiagram);
        URI ddURI = URI.createFileURI(inputDataDictionary);

        var provider = new DFDURIResourceProvider(dfdURI, ddURI);
        provider.loadRequiredResources();
        this.dataFlowDiagram = provider.getDataFlowDiagram();
        this.dataDictionary = provider.getDataDictionary();
    }

    /**
     * Returns the stored data flow diagram
     * @return Returns the stored data flow diagram
     */
    public DataFlowDiagram dataFlowDiagram() {
        return dataFlowDiagram;
    }

    /**
     * Returns the stored data dictionary
     * @return Returns the stored data dictionary
     */
    public DataDictionary dataDictionary() {
        return dataDictionary;
    }

    @Override
    public void save(String filePath, String fileName) {
        ResourceSet resourceSet = new ResourceSetImpl();
        Path basePath = Path.of(filePath, fileName)
                .toAbsolutePath()
                .normalize();

        Resource dfdResource = createResource(basePath + FILE_EXTENSION_DFD, new String[] {"dataflowdiagram"}, resourceSet);
        Resource ddResource = createResource(basePath + FILE_EXTENSION_DD, new String[] {"datadictionary"}, resourceSet);

        dfdResource.getContents()
                .add(dataFlowDiagram);
        ddResource.getContents()
                .add(dataDictionary);

        saveResource(dfdResource);
        saveResource(ddResource);
    }
}
