package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.palladiosimulator.dataflow.confidentiality.analysis.PCMAnalysisUtils;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.repository.OperationalDataStoreComponent;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.allocation.AllocationPackage;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.Parameter;

public class DatabaseActionSequenceElement<T extends OperationalDataStoreComponent> extends AbstractPCMActionSequenceElement<T> {
	// TODO: Implement better, please ;(
	private static HashMap<String, List<CharacteristicValue>> tempStore = new HashMap<>();
	
	private boolean isWriting;
	
	public DatabaseActionSequenceElement(T element, Deque<AssemblyContext> context, List<Parameter> parameters, boolean isWriting) {
        super(element, context, parameters);
        this.isWriting = isWriting;
    }
	
	public DatabaseActionSequenceElement(DatabaseActionSequenceElement<T> oldElement, 
			List<DataFlowVariable> dataFlowVariables, 
			List<CharacteristicValue> nodeVariables) {
		super(oldElement, dataFlowVariables, nodeVariables);
		this.isWriting = oldElement.isWriting();
	}

	@Override
	public AbstractActionSequenceElement<T> evaluateDataFlow(List<DataFlowVariable> variables) {
		List<CharacteristicValue> nodeVariables = this.evaluateNodeCharacteristics();
		List<DataFlowVariable> availableDataFlowVariables = this.getAvailableDataFlowVariables(variables);
		if (this.isWriting()) {
			String dataSourceName = this.getParameter().get(0).getParameterName();
			DataFlowVariable dataSource = availableDataFlowVariables.stream()
					.filter(it -> it.variableName().equals(dataSourceName))
					.findAny().orElse(new DataFlowVariable(dataSourceName));
			DatabaseActionSequenceElement.tempStore.put(this.getElement().getEntityName(), dataSource.characteristics());
			return new DatabaseActionSequenceElement<>(this, availableDataFlowVariables, nodeVariables);
		}
		DataFlowVariable modifiedVariable = new DataFlowVariable("RETURN");
		List<CharacteristicValue> storedData = DatabaseActionSequenceElement.tempStore.get(this.getElement().getEntityName());
		if (storedData == null) {
			availableDataFlowVariables.add(modifiedVariable);
			return new DatabaseActionSequenceElement<>(this, availableDataFlowVariables, nodeVariables);
		}
		for(CharacteristicValue characteristicValue : storedData) {
			modifiedVariable = modifiedVariable.addCharacteristic(characteristicValue);
		}
		availableDataFlowVariables.add(modifiedVariable);
		return new DatabaseActionSequenceElement<>(this, availableDataFlowVariables, nodeVariables);
	}
	
    protected List<CharacteristicValue> evaluateNodeCharacteristics() {
    	List<CharacteristicValue> nodeVariables = new ArrayList<>();
    	
    	var allocations = PCMAnalysisUtils.lookupElementOfType(AllocationPackage.eINSTANCE.getAllocation()).stream()
    			.filter(Allocation.class::isInstance)
    			.map(Allocation.class::cast)
    			.collect(Collectors.toList());
    	
    	var allocation = allocations.stream()
    			.filter(it -> it.getAllocationContexts_Allocation().stream()
    					.map(alloc -> alloc.getAssemblyContext_AllocationContext())
    					.anyMatch(this.getContext().getFirst()::equals)
    					)
    			.findFirst()
    			.orElseThrow();
    	
    	var allocationContexts = allocation.getAllocationContexts_Allocation();
    	    	
    	for (AllocationContext allocationContext : allocationContexts) {
    		if (this.getContext().contains(allocationContext.getAssemblyContext_AllocationContext())) {
        		nodeVariables.addAll(this.evaluateNodeCharacteristics(allocationContext.getResourceContainer_AllocationContext()));
    		}
    	}
    	return nodeVariables;
    }

	@Override
    public List<DataFlowVariable> getAvailableDataFlowVariables(List<DataFlowVariable> variables) {
    	return new ArrayList<>(variables);
    }
	
	public boolean isWriting() {
		return this.isWriting;
	}

	@Override
	public String toString() {
		String writing = isWriting ? "writing" : "reading";
        return String.format("%s / %s (%s, %s))", this.getClass()
            .getSimpleName(), writing,
                this.getElement()
                    .getEntityName(),
                this.getElement()
                    .getId());
	}
}
