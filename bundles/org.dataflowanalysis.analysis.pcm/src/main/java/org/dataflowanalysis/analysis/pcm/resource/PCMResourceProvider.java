package org.dataflowanalysis.analysis.pcm.resource;

import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.dddsl.DDDslStandaloneSetup;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.NodeCharacteristicsPackage;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.util.NodeCharacteristicsResourceFactoryImpl;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationPackage;
import org.palladiosimulator.pcm.allocation.util.AllocationResourceFactoryImpl;
import org.palladiosimulator.pcm.repository.RepositoryPackage;
import org.palladiosimulator.pcm.repository.util.RepositoryResourceFactoryImpl;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentPackage;
import org.palladiosimulator.pcm.resourceenvironment.util.ResourceenvironmentResourceFactoryImpl;
import org.palladiosimulator.pcm.system.SystemPackage;
import org.palladiosimulator.pcm.system.util.SystemResourceFactoryImpl;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsagemodelPackage;
import org.palladiosimulator.pcm.usagemodel.util.UsagemodelResourceFactoryImpl;

public abstract class PCMResourceProvider extends ResourceProvider {
    @Override
    public void setupResources() {
        this.resources.getPackageRegistry()
                .put(AllocationPackage.eNS_URI, AllocationPackage.eINSTANCE);
        this.resources.getResourceFactoryRegistry()
                .getExtensionToFactoryMap()
                .put(AllocationPackage.eNAME, new AllocationResourceFactoryImpl());
        this.resources.getPackageRegistry()
                .put(NodeCharacteristicsPackage.eNS_URI, NodeCharacteristicsPackage.eINSTANCE);
        this.resources.getResourceFactoryRegistry()
                .getExtensionToFactoryMap()
                .put(NodeCharacteristicsPackage.eNAME, new NodeCharacteristicsResourceFactoryImpl());
        this.resources.getPackageRegistry()
                .put(RepositoryPackage.eNS_URI, RepositoryPackage.eINSTANCE);
        this.resources.getResourceFactoryRegistry()
                .getExtensionToFactoryMap()
                .put(RepositoryPackage.eNAME, new RepositoryResourceFactoryImpl());
        this.resources.getPackageRegistry()
                .put(ResourceenvironmentPackage.eNS_URI, ResourceenvironmentPackage.eINSTANCE);
        this.resources.getResourceFactoryRegistry()
                .getExtensionToFactoryMap()
                .put(ResourceenvironmentPackage.eNAME, new ResourceenvironmentResourceFactoryImpl());
        this.resources.getPackageRegistry()
                .put(SystemPackage.eNS_URI, SystemPackage.eINSTANCE);
        this.resources.getResourceFactoryRegistry()
                .getExtensionToFactoryMap()
                .put(SystemPackage.eNAME, new SystemResourceFactoryImpl());
        this.resources.getPackageRegistry()
                .put(UsagemodelPackage.eNS_URI, UsagemodelPackage.eINSTANCE);
        this.resources.getResourceFactoryRegistry()
                .getExtensionToFactoryMap()
                .put(UsagemodelPackage.eNAME, new UsagemodelResourceFactoryImpl());

        DDDslStandaloneSetup.doSetup();
    }

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
        if (this.lookupToplevelElement(RepositoryPackage.eINSTANCE.getRepository())
                .isEmpty()) {
            return false;
        }
        if (this.lookupToplevelElement(SystemPackage.eINSTANCE.getSystem())
                .isEmpty()) {
            return false;
        }
        return !this.lookupToplevelElement(ResourceenvironmentPackage.eINSTANCE.getResourceEnvironment())
                .isEmpty();
    }
}
