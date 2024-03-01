package org.dataflowanalysis.analysis.pcm.core;

import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.pcm.core.finder.PCMUserFinder;
import org.dataflowanalysis.analysis.pcm.core.finder.SEFFPCMVertexFactory;
import org.dataflowanalysis.analysis.pcm.core.finder.UserPCMVertexFactory;
import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;
import org.dataflowanalysis.analysis.pcm.utils.PCMQueryUtils;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public class PCMPartialFlowGraphFinder {
    private final Logger logger = Logger.getLogger(PCMPartialFlowGraphFinder.class);

    private final ResourceProvider resourceProvider;
    private PCMUserFinder userFinder;

    public PCMPartialFlowGraphFinder(ResourceProvider resourceProvider) {
        this(resourceProvider, new PCMUserFinder(new UserPCMVertexFactory(), new SEFFPCMVertexFactory()));
    }
    
    public PCMPartialFlowGraphFinder(ResourceProvider resourceProvider, PCMUserFinder userFinder) {
    	this.resourceProvider = resourceProvider;
    	this.userFinder = userFinder;
    }

    public List<PCMPartialFlowGraph> findPartialFlowGraphs() {
        PCMResourceProvider pcmResourceProvider = (PCMResourceProvider) this.resourceProvider;
        List<PCMPartialFlowGraph> sequences = findSequencesForUsageModel(pcmResourceProvider.getUsageModel());
        logger.info(String.format("Found %d action %s.", sequences.size(), sequences.size() == 1 ? "sequence" : "sequences"));
        return sequences;
    }

    private List<PCMPartialFlowGraph> findSequencesForUsageModel(UsageModel usageModel) {
        PCMPartialFlowGraph initialList = new PCMPartialFlowGraph();
        List<Start> startActions = PCMQueryUtils.findStartActionsForUsageModel(usageModel);

        return startActions.stream().map(it -> userFinder.findSequencesForUserAction(it, initialList, this.resourceProvider))
                .flatMap(List::stream).toList();
    }
}
