package org.dataflowanalysis.analysis.pcm.resource;

import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.RepositoryPackage;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentPackage;
import org.palladiosimulator.pcm.system.SystemPackage;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public abstract class PCMResourceProvider extends ResourceProvider {
    /**
     * Returns the usage model that the resource loader has loaded
     * @return Usage model saved in the resources
     */
    public abstract UsageModel getUsageModel();

    /**
     * Returns the allocation model that the resource loader has loaded
     * @return Allocation model saved in the resources
     */
    public abstract Allocation getAllocation();

    /**
     * Determines, whether the resource loader has sufficient resources to run the analysis
     * @return This method returns true, if the analysis can be executed with the resource loader. Otherwise, the method
     * returns false
     */
    @Override
    public boolean sufficientResourcesLoaded() {
        if (this.getUsageModel() == null || this.getAllocation() == null) {
            return false;
        }
        if (this.lookupToplevelElement(RepositoryPackage.eINSTANCE.getRepository()).isEmpty()) {
            return false;
        }
        if (this.lookupToplevelElement(SystemPackage.eINSTANCE.getSystem()).isEmpty()) {
            return false;
        }
        if (this.lookupToplevelElement(ResourceenvironmentPackage.eINSTANCE.getResourceEnvironment()).isEmpty()) {
            return false;
        }
        return true;
    }
}
