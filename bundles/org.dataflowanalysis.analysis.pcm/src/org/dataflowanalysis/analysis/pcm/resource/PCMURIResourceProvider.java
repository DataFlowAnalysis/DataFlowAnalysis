package org.dataflowanalysis.analysis.pcm.resource;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public class PCMURIResourceProvider extends PCMResourceProvider {
    private final URI usageModelURI;
    private final URI allocationModelURI;
    private final URI nodeCharacteristicURI;
    private UsageModel usageModel;
    private Allocation allocation;

    /**
     * Creates a new resource loader with the given model URIs
     * @param usageModelURI URI to the usage model
     * @param allocationModelURI URI to the allocation model
     * @param nodeCharacteristicsURI URI to the node characteristics model
     */
    public PCMURIResourceProvider(URI usageModelURI, URI allocationModelURI, URI nodeCharacteristicsURI) {
        this.usageModelURI = usageModelURI;
        this.allocationModelURI = allocationModelURI;
        this.nodeCharacteristicURI = nodeCharacteristicsURI;
    }

    @Override
    public void loadRequiredResources() {
        this.usageModel = (UsageModel) this.loadModelContent(usageModelURI);
        this.allocation = (Allocation) this.loadModelContent(allocationModelURI);
        this.loadModelContent(this.nodeCharacteristicURI);
        List<Resource> loadedResources;
        do {
            loadedResources = new ArrayList<>(this.resources.getResources());
            loadedResources.forEach(EcoreUtil::resolveAll);
        } while (loadedResources.size() != this.resources.getResources()
                .size());
    }

    @Override
    public UsageModel getUsageModel() {
        return this.usageModel;
    }

    @Override
    public Allocation getAllocation() {
        return this.allocation;
    }
}
