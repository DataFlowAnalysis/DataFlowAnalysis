package org.dataflowanalysis.converter.dfd2web;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import org.dataflowanalysis.analysis.dfd.resource.DFDURIResourceProvider;
import org.dataflowanalysis.analysis.utils.ResourceUtils;
import org.dataflowanalysis.converter.ConverterModel;
import org.dataflowanalysis.converter.interactive.ModelType;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;

public final class DataFlowDiagramAndDictionary extends ConverterModel {
    private final DataFlowDiagram dataFlowDiagram;
    private final DataDictionary dataDictionary;

    public DataFlowDiagramAndDictionary(DataFlowDiagram dataFlowDiagram, DataDictionary dataDictionary) {
        super(ModelType.DFD);
        this.dataFlowDiagram = dataFlowDiagram;
        this.dataDictionary = dataDictionary;
    }

    public DataFlowDiagramAndDictionary(String project, String inputDataFlowDiagram, String inputDataDictionary, Class<?> activator)
            throws StandaloneInitializationException {
        super(ModelType.DFD);
        StandaloneInitializerBuilder.builder()
                .registerProjectURI(activator, project)
                .build()
                .init();

        URI dfdURI = ResourceUtils.createRelativePluginURI(inputDataFlowDiagram, project);
        URI ddURI = ResourceUtils.createRelativePluginURI(inputDataDictionary, project);

        var provider = new DFDURIResourceProvider(dfdURI, ddURI);
        provider.loadRequiredResources();
        this.dataFlowDiagram = provider.getDataFlowDiagram();
        this.dataDictionary = provider.getDataDictionary();
    }

    public DataFlowDiagramAndDictionary(String inputDataFlowDiagram, String inputDataDictionary) {
        super(ModelType.DFD);
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

    public DataFlowDiagramAndDictionary(Scanner scanner) {
        super(ModelType.DFD);

        String inputDataFlowDiagram = this.promptInput(scanner, "dataflowdiagram");
        String inputDataDictionary = this.promptInput(scanner, "datadictionary");

        URI dfdURI = URI.createFileURI(inputDataFlowDiagram);
        URI ddURI = URI.createFileURI(inputDataDictionary);

        var provider = new DFDURIResourceProvider(dfdURI, ddURI);
        provider.loadRequiredResources();
        this.dataFlowDiagram = provider.getDataFlowDiagram();
        this.dataDictionary = provider.getDataDictionary();
    }

    public DataFlowDiagram dataFlowDiagram() {
        return dataFlowDiagram;
    }

    public DataDictionary dataDictionary() {
        return dataDictionary;
    }

    @Override
    public void save(String filePath, String fileName) {
        ResourceSet resourceSet = new ResourceSetImpl();
        Path basePath = Path.of(filePath, fileName)
                .toAbsolutePath()
                .normalize();

        Resource dfdResource = createResource(basePath + ".dataflowdiagram", new String[] {"dataflowdiagram"}, resourceSet);
        Resource ddResource = createResource(basePath + ".datadictionary", new String[] {"datadictionary"}, resourceSet);

        dfdResource.getContents()
                .add(dataFlowDiagram);
        ddResource.getContents()
                .add(dataDictionary);

        saveResource(dfdResource);
        saveResource(ddResource);
    }
}
