package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.finder;

import java.util.List;
import java.util.Optional;

import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.AbstractPCMActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.DataStore;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.DatabaseActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.PCMActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.SEFFWithContext;
import org.palladiosimulator.dataflow.confidentiality.analysis.utils.pcm.PCMQueryUtils;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.repository.OperationalDataStoreComponent;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.seff.AbstractAction;

public class PCMDatabaseFinderUtils {
	
	private PCMDatabaseFinderUtils() {
		// Utility class
	}
	
	public static List<PCMActionSequence> findSequencesForDatabaseAction(SEFFWithContext seff,
            SEFFFinderContext context,
            PCMActionSequence previousSequence) {
		boolean isWriting = true;
		if (seff.seff().getDescribedService__SEFF().getEntityName().equals("get")) {
			isWriting = false;
		}
		OperationalDataStoreComponent currentAction = (OperationalDataStoreComponent) seff.seff().getBasicComponent_ServiceEffectSpecification(); 
		OperationSignature callSigniture = (OperationSignature) seff.seff().getDescribedService__SEFF();
		Optional<DataStore> dataStore = context.getDataStores().stream()
				.filter(it -> it.getDatabaseComponentName().equals(currentAction.getEntityName()))
				.findAny();
		
		if (dataStore.isEmpty()) {
			dataStore = Optional.of(new DataStore(currentAction.getEntityName()));
			if (!callSigniture.getParameters__OperationSignature().isEmpty()) {
				dataStore.get().setDatabaseVariableName(callSigniture.getParameters__OperationSignature().get(0).getParameterName());
			}
			context.addDataStore(dataStore.get());
		} else if (isWriting) {
			String variableName = callSigniture.getParameters__OperationSignature().get(0).getParameterName();
			dataStore.get().setDatabaseVariableName(variableName);
		}
		
		context.addDataStore(dataStore.get());
		var newEntity = new DatabaseActionSequenceElement<>(currentAction, context.getContext(), isWriting, dataStore.get());
		PCMActionSequence currentSequence = new PCMActionSequence(previousSequence, newEntity);
		
		return returnToCaller(currentAction, context, currentSequence);
    }
	
	private static List<PCMActionSequence> returnToCaller(OperationalDataStoreComponent currentAction,
            SEFFFinderContext context,
            PCMActionSequence previousSequence) {
		Optional<AbstractAction> parentAction = PCMQueryUtils.findParentOfType(currentAction, AbstractAction.class,
                false);

        if (parentAction.isPresent()) {
            AbstractAction successor = parentAction.get()
                .getSuccessor_AbstractAction();
            return PCMSEFFFinderUtils.findSequencesForSEFFAction(successor, context, previousSequence);
        } else {
            AbstractPCMActionSequenceElement<?> caller = context.getLastCaller();
            context.updateParameterForCallerReturning(caller);
            return PCMSEFFFinderUtils.returnToCaller(caller, context, previousSequence);
        }
	}
}
