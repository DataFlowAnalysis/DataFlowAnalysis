package org.dataflowanalysis.analysis.dfd.resource;

import java.util.ArrayList;
import java.util.List;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class DFDURIResourceProvider extends DFDResourceProvider {
    private final URI dataFlowDiagramURI;
    private final URI dataDictionaryURI;
    private DataFlowDiagram dataFlowDiagram;
    private DataDictionary dataDictionary;

    /**
     * Creates a new resource loader with the given model URIs
     * @param dataFlowDiagramURI URI to the data flow diagram model
     * @param dataDictionaryURI URI to the data dictionary model
     */
    public DFDURIResourceProvider(URI dataFlowDiagramURI, URI dataDictionaryURI) {
        this.dataFlowDiagramURI = dataFlowDiagramURI;
        this.dataDictionaryURI = dataDictionaryURI;
    }

    @Override
    public void loadRequiredResources() {
        this.dataFlowDiagram = (DataFlowDiagram) this.loadModelContent(dataFlowDiagramURI);
        this.dataDictionary = (DataDictionary) this.loadModelContent(dataDictionaryURI);
        List<Resource> loadedResources = null;
        do {
            loadedResources = new ArrayList<>(this.resources.getResources());
            loadedResources.forEach(EcoreUtil::resolveAll);
        } while (loadedResources.size() != this.resources.getResources().size());
    }

    @Override
    public DataFlowDiagram getDataFlowDiagram() {
        return this.dataFlowDiagram;
    }

    @Override
    public DataDictionary getDataDictionary() {
        return this.dataDictionary;
    }
}
