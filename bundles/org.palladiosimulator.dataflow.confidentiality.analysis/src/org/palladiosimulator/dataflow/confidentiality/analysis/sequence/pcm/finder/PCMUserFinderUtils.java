package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.finder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.ActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.AbstractPCMActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.CallingUserActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.DataStore;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.PCMActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.PCMQueryUtils;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.SEFFWithContext;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.repository.OperationalDataStoreComponent;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Branch;
import org.palladiosimulator.pcm.usagemodel.BranchTransition;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.Stop;

public class PCMUserFinderUtils {
	private PCMUserFinderUtils() {
		// Utility class
	}
	
	public static List<PCMActionSequence> findSequencesForUserAction(AbstractUserAction currentAction, List<DataStore> dataStores,
            PCMActionSequence previousSequence) {
        if (currentAction instanceof Start) {
            return findSequencesForUserStartAction((Start) currentAction, dataStores, previousSequence);

        } else if (currentAction instanceof Stop) {
            return findSequencesForUserStopAction((Stop) currentAction, dataStores, previousSequence);

        } else if (currentAction instanceof Branch) {
            return findSequencesForUserBranchAction((Branch) currentAction, dataStores, previousSequence);

        } else if (currentAction instanceof EntryLevelSystemCall) {
            return findSequencesForEntryLevelSystemCall((EntryLevelSystemCall) currentAction, dataStores, previousSequence);

        } else {
            throw new IllegalArgumentException(
                    String.format("The type %s is not supported in usage scenarios.", currentAction.getClass()
                        .getName()));
        }
    }

    private static List<PCMActionSequence> findSequencesForUserStartAction(Start currentAction, List<DataStore> dataStores,
            PCMActionSequence previousSequence) {
        return findSequencesForUserAction(currentAction.getSuccessor(), dataStores, previousSequence);
    }

    private static List<PCMActionSequence> findSequencesForUserStopAction(Stop currentAction, List<DataStore> dataStores,
            PCMActionSequence previousSequence) {
        Optional<AbstractUserAction> parentAction = PCMQueryUtils.findParentOfType(currentAction,
                AbstractUserAction.class, false);

        if (parentAction.isEmpty()) {
            return List.of(previousSequence);
        } else {
            return findSequencesForUserAction(parentAction.get()
                .getSuccessor(), dataStores, previousSequence);
        }
    }

    private static List<PCMActionSequence> findSequencesForUserBranchAction(Branch currentAction, List<DataStore> dataStores,
            PCMActionSequence previousSequence) {
        return currentAction.getBranchTransitions_Branch()
            .stream()
            .map(BranchTransition::getBranchedBehaviour_BranchTransition)
            .map(PCMQueryUtils::getStartActionOfScenarioBehavior)
            .flatMap(Optional::stream)
            .map(it -> findSequencesForUserAction(it, dataStores, previousSequence))
            .flatMap(List::stream)
            .toList();
    }

    private static List<PCMActionSequence> findSequencesForEntryLevelSystemCall(EntryLevelSystemCall currentAction, List<DataStore> dataStores,
            PCMActionSequence previousSequence) {
        var callingEntity = new CallingUserActionSequenceElement(currentAction, true);
        PCMActionSequence currentActionSequence = new PCMActionSequence(previousSequence, callingEntity);

        OperationProvidedRole calledRole = currentAction.getProvidedRole_EntryLevelSystemCall();
        OperationSignature calledSignature = currentAction.getOperationSignature__EntryLevelSystemCall();
        Optional<SEFFWithContext> calledSEFF = PCMQueryUtils.findCalledSEFF(calledRole, calledSignature,
                new ArrayDeque<>());

        if (calledSEFF.isEmpty()) {
            return new ArrayList<PCMActionSequence>();
        } else {
        	if (calledSEFF.get().seff().getBasicComponent_ServiceEffectSpecification() instanceof OperationalDataStoreComponent) {
                Deque<AbstractPCMActionSequenceElement<?>> callers = new ArrayDeque<>();
        		callers.add(callingEntity);
        		SEFFFinderContext finderContext = new SEFFFinderContext(calledSEFF.get().context(), callers, calledSignature.getParameters__OperationSignature(), dataStores);
        		
        		return PCMDatabaseFinderUtils.findSequencesForDatabaseAction(calledSEFF.get(), finderContext, currentActionSequence);
        	}
            Optional<StartAction> SEFFStartAction = PCMQueryUtils.getFirstStartActionInActionList(calledSEFF.get()
                .seff()
                .getSteps_Behaviour());

            if (SEFFStartAction.isEmpty()) {
                throw new IllegalStateException("Unable to find SEFF start action.");
            } else {
                Deque<AbstractPCMActionSequenceElement<?>> callers = new ArrayDeque<>();
                callers.add(callingEntity);

                SEFFFinderContext finderContext = new SEFFFinderContext(calledSEFF.get().context(), callers, calledSignature.getParameters__OperationSignature(), dataStores);
                return PCMSEFFFinderUtils.findSequencesForSEFFAction(SEFFStartAction.get(), finderContext ,currentActionSequence);
            }
        }
    }

    public static List<PCMActionSequence> findSequencesForUserActionReturning(EntryLevelSystemCall currentAction, List<DataStore> dataStores,
            PCMActionSequence previousSequence) {
        PCMActionSequence currentActionSequence = new PCMActionSequence(previousSequence,
                new CallingUserActionSequenceElement(currentAction, false));
        return findSequencesForUserAction(currentAction.getSuccessor(), dataStores, currentActionSequence);
    }
}
