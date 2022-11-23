package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.finder;

import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.ActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.AbstractPCMActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.DataStore;
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
            SEFFFinderContext context,
            ActionSequence previousSequence) {
		boolean isWriting = true;
		if (seff.seff().getDescribedService__SEFF().getEntityName().equals("get")) {
			isWriting = false;
		}
		OperationalDataStoreComponent currentAction = (OperationalDataStoreComponent) seff.seff().getBasicComponent_ServiceEffectSpecification();
		DataStore dataStore = context.getDataStores().stream()
				.filter(it -> it.getDatabaseComponentName().equals(currentAction.getEntityName()))
				.findAny().orElse(new DataStore(currentAction.getEntityName()));
		context.addDataStore(dataStore);
		var newEntity = new DatabaseActionSequenceElement<>(currentAction, context.getContext(), context.getAvailableVariables(), isWriting, dataStore);
		ActionSequence currentSequence = new ActionSequence(previousSequence, newEntity);
		
		return returnToCaller(currentAction, context, currentSequence);
    }
	
	private static List<ActionSequence> returnToCaller(OperationalDataStoreComponent currentAction,
            SEFFFinderContext context,
            ActionSequence previousSequence) {
		Optional<AbstractAction> parentAction = PCMQueryUtils.findParentOfType(currentAction, AbstractAction.class,
                false);

        if (parentAction.isPresent()) {
            AbstractAction successor = parentAction.get()
                .getSuccessor_AbstractAction();
            return PCMSEFFFinderUtils.findSequencesForSEFFAction(successor, context, previousSequence);
        } else {
            AbstractPCMActionSequenceElement<?> caller = context.getLastCaller();
            context.updateParametersForCallerReturning(caller);
            return PCMSEFFFinderUtils.returnToCaller(caller, context, previousSequence);
        }
	}
}
