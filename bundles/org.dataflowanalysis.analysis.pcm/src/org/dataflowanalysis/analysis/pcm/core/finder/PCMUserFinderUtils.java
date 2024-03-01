package org.dataflowanalysis.analysis.pcm.core.finder;

import java.util.List;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.PCMPartialFlowGraph;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

public class PCMUserFinderUtils {
    private static PCMUserFinder userFinder = new PCMUserFinder(new UserPCMVertexFactory(), new SEFFPCMVertexFactory());

    private PCMUserFinderUtils() {
        // Utility class
    }

    public static List<PCMPartialFlowGraph> findSequencesForUserAction(AbstractUserAction currentAction, PCMPartialFlowGraph previousSequence,
            ResourceProvider resourceProvider) {
        return userFinder.findSequencesForUserAction(currentAction, previousSequence, resourceProvider);
    }

    public static List<PCMPartialFlowGraph> findSequencesForUserActionReturning(EntryLevelSystemCall currentAction,
            PCMPartialFlowGraph previousSequence, ResourceProvider resourceProvider, AbstractPCMVertex<?> caller) {
        return findSequencesForUserActionReturning(currentAction, previousSequence, resourceProvider, caller);
    }
}
