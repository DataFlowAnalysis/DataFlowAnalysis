package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.finder;

import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.ActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.AbstractPCMActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.DatabaseActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.PCMQueryUtils;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.SEFFWithContext;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.repository.OperationalDataStoreComponent;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.AbstractAction;

public class PCMDatabaseFinderUtils {
	private PCMDatabaseFinderUtils() {
		// Utility class
	}
	
	public static List<ActionSequence> findSequencesForDatabaseAction(SEFFWithContext seff,
            Deque<AssemblyContext> context, Deque<AbstractPCMActionSequenceElement<?>> callers, List<Parameter> availableVariables,
            ActionSequence previousSequence) {
		boolean isWriting = true;
		if (seff.seff().getDescribedService__SEFF().getEntityName().equals("get")) {
			isWriting = false;
		}
		OperationalDataStoreComponent currentAction = (OperationalDataStoreComponent) seff.seff().getBasicComponent_ServiceEffectSpecification();
		var newEntity = new DatabaseActionSequenceElement<>(currentAction, context, availableVariables, isWriting);
		ActionSequence currentSequence = new ActionSequence(previousSequence, newEntity);
		
		return returnToCaller(currentAction, context, callers, availableVariables, currentSequence);
    }
	
	private static List<ActionSequence> returnToCaller(OperationalDataStoreComponent currentAction,
            Deque<AssemblyContext> context, Deque<AbstractPCMActionSequenceElement<?>> callers, List<Parameter> availableVariables,
            ActionSequence previousSequence) {
		Optional<AbstractAction> parentAction = PCMQueryUtils.findParentOfType(currentAction, AbstractAction.class,
                false);

        if (parentAction.isPresent()) {
            AbstractAction successor = parentAction.get()
                .getSuccessor_AbstractAction();
            return PCMSEFFFinderUtils.findSequencesForSEFFAction(successor, context, callers, availableVariables, previousSequence);
        } else {
            AbstractPCMActionSequenceElement<?> caller = callers.removeLast();
            List<Parameter> parentVariables = PCMSEFFFinderUtils.getParametersCaller(caller);
            return PCMSEFFFinderUtils.returnToCaller(caller, callers, parentVariables, previousSequence);
        }
	}
}
