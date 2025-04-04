package org.dataflowanalysis.analysis.pcm.core.finder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.TransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.pcm.core.PCMTransposeFlowGraph;
import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;
import org.dataflowanalysis.analysis.pcm.utils.PCMQueryUtils;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

public class PCMTransposeFlowGraphFinder implements TransposeFlowGraphFinder {
    private final Logger logger = Logger.getLogger(PCMTransposeFlowGraphFinder.class);

    private final ResourceProvider resourceProvider;
    private final Collection<AssemblyContext> contexts;
    private final Collection<Parameter> parameter;

    public PCMTransposeFlowGraphFinder(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
        this.contexts = List.of();
        this.parameter = List.of();
    }

    public PCMTransposeFlowGraphFinder(ResourceProvider resourceProvider, Collection<AssemblyContext> contexts, Collection<Parameter> parameter) {
        this.resourceProvider = resourceProvider;
        this.contexts = contexts;
        this.parameter = parameter;
    }

    @Override
    public List<? extends PCMTransposeFlowGraph> findTransposeFlowGraphs() {
        if (!(this.resourceProvider instanceof PCMResourceProvider pcmResourceProvider)) {
            logger.error("Resource provider of the transpose flow graph finder is not a pcm resource provider, please provide a correct one");
            throw new IllegalStateException();
        }
        return this.findTransposeFlowGraphs(List.of(), PCMQueryUtils.findStartActionsForUsageModel(pcmResourceProvider.getUsageModel()));
    }

    @Override
    public List<? extends PCMTransposeFlowGraph> findTransposeFlowGraphs(List<?> sourceNodes) {
        return this.findTransposeFlowGraphs(sourceNodes, List.of());
    }

    /**
     * Determines the transpose flow graphs starting at the given list of source nodes and ending at the list of sink nodes.
     * <p/>
     * If the list of sink nodes is empty, every action sequence starting at the source nodes will be returned <b> Only
     * works from user nodes or within one SEFF due to missing and required context information (e.g. where a SEFF returns
     * to) </b>
     * @param sinkNodes List of sink nodes the transpose flow graphs should end at
     * @param sourceNodes List of source nodes the transpose flow graphs should start at
     * @return Returns a list of all transpose flow graphs starting at the list of sources and ending at the list of sinks
     */
    @Override
    public List<? extends PCMTransposeFlowGraph> findTransposeFlowGraphs(List<?> sinkNodes, List<?> sourceNodes) {
        List<PCMTransposeFlowGraph> transposeFlowGraphs = new ArrayList<>();
        List<AbstractUserAction> userActions = sourceNodes.stream()
                .filter(AbstractUserAction.class::isInstance)
                .map(AbstractUserAction.class::cast)
                .toList();
        List<AbstractAction> seffActions = sourceNodes.stream()
                .filter(AbstractAction.class::isInstance)
                .map(AbstractAction.class::cast)
                .toList();
        List<Entity> sinks = sinkNodes.stream()
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .toList();

        transposeFlowGraphs.addAll(this.findTransposeFlowGraphsForUserActions(userActions, sinks));
        transposeFlowGraphs.addAll(this.findTransposeFlowGraphsForSEFFActions(seffActions, sinks));
        logger.info(String.format("Found %d transpose flow %s.", transposeFlowGraphs.size(), transposeFlowGraphs.size() == 1 ? "graph" : "graphs"));
        return transposeFlowGraphs;
    }

    private List<PCMTransposeFlowGraph> findTransposeFlowGraphsForUserActions(List<AbstractUserAction> userActions, List<Entity> sinks) {
        return userActions.stream()
                .map(it -> new PCMUserTransposeFlowGraphFinder(this.resourceProvider, sinks).findSequencesForUserAction(it))
                .flatMap(List::stream)
                .toList();
    }

    private List<PCMTransposeFlowGraph> findTransposeFlowGraphsForSEFFActions(List<AbstractAction> seffActions, List<Entity> sinks) {
        SEFFFinderContext context = new SEFFFinderContext(new ArrayDeque<>(contexts), new ArrayDeque<>(), new ArrayList<>(parameter));
        return seffActions.stream()
                .map(it -> new PCMSEFFTransposeFlowGraphFinder(this.resourceProvider, context, sinks, new PCMTransposeFlowGraph())
                        .findSequencesForSEFFAction(it))
                .flatMap(List::stream)
                .toList();
    }
}
