package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import org.palladiosimulator.dataflow.confidentiality.analysis.PCMAnalysisUtils;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.allocation.AllocationPackage;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableUsage;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.seff.AbstractAction;

public class SEFFActionSequenceElement<T extends AbstractAction> extends AbstractPCMActionSequenceElement<T> {
	
    public SEFFActionSequenceElement(T element, Deque<AssemblyContext> context, List<Parameter> parameters) {
        super(element, context, parameters);
    }

    public SEFFActionSequenceElement(SEFFActionSequenceElement<T> oldElement, List<DataFlowVariable> dataFlowVariables, List<CharacteristicValue> nodeVariables) {
        super(oldElement, dataFlowVariables, nodeVariables);
    }

    @Override
    public AbstractActionSequenceElement<T> evaluateDataFlow(List<DataFlowVariable> variables) {
    	List<CharacteristicValue> nodeCharacteristics = this.evaluateNodeCharacteristics();
        List<DataFlowVariable> dataFlowVariables = this.evaluateDataFlowCharacteristics(variables, nodeCharacteristics);
        return new SEFFActionSequenceElement<T>(this, dataFlowVariables, nodeCharacteristics);
    }
    
    @Override
    public List<DataFlowVariable> getAvailableDataFlowVariables(List<DataFlowVariable> variables) {
    	List<String> availableVariableNames = this.getParameter().stream()
    			.map(it -> it.getParameterName())
    			.collect(Collectors.toList());
    	return variables.stream()
    			.filter(it -> availableVariableNames.stream().anyMatch(st -> it.variableName().equals(st)))
    			.collect(Collectors.toList());
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
    public String toString() {
        return String.format("%s (%s, %s))", this.getClass()
            .getSimpleName(),
                this.getElement()
                    .getEntityName(),
                this.getElement()
                    .getId());
    }

}
