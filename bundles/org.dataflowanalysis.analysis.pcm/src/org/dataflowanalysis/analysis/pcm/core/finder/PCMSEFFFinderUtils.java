package org.dataflowanalysis.analysis.pcm.core.finder;

import java.util.List;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.pcm.core.PCMPartialFlowGraph;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.seff.AbstractAction;

public class PCMSEFFFinderUtils {
    private static PCMSEFFFinder seffFinder = new PCMSEFFFinder(new SEFFPCMVertexFactory(), new UserPCMVertexFactory());

    public static List<PCMPartialFlowGraph> findSequencesForSEFFAction(AbstractAction currentAction, SEFFFinderContext context,
            PCMPartialFlowGraph previousSequence, ResourceProvider resourceProvider) {

        return seffFinder.findSequencesForSEFFAction(currentAction, context, previousSequence, resourceProvider);
    }

    public static List<PCMPartialFlowGraph> returnToCaller(AbstractVertex<?> caller, SEFFFinderContext context, PCMPartialFlowGraph previousSequence,
            ResourceProvider resourceProvider) {

        return seffFinder.returnToCaller(caller, context, previousSequence, resourceProvider);
    }
}
