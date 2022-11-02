package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import org.palladiosimulator.dataflow.confidentiality.analysis.PCMAnalysisUtils;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.CharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.EnumCharacteristic;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.profile.ProfileConstants;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.palladiosimulator.mdsdprofiles.api.StereotypeAPI;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationPackage;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.SetVariableAction;

public class SEFFActionSequenceElement<T extends AbstractAction> extends AbstractPCMActionSequenceElement<T> {

    public SEFFActionSequenceElement(T element, Deque<AssemblyContext> context) {
        super(element, context);
    }

    public SEFFActionSequenceElement(SEFFActionSequenceElement<T> oldElement, List<DataFlowVariable> dataFlowVariables, List<CharacteristicValue> nodeVariables) {
        super(oldElement, dataFlowVariables, nodeVariables);
    }

    @Override
    public AbstractActionSequenceElement<T> evaluateDataFlow(List<DataFlowVariable> variables) {
    	List<CharacteristicValue> nodeVariables = this.evaluateNodeCharacteristics();
        List<VariableCharacterisation> variableCharacterisations = ((SetVariableAction) super.getElement())
            .getLocalVariableUsages_SetVariableAction()
            .stream()
            .flatMap(it -> it.getVariableCharacterisation_VariableUsage()
                .stream())
            .toList();
        CharacteristicsCalculator characteristicsCalculator = new CharacteristicsCalculator(variables, nodeVariables);
        variableCharacterisations.stream()
            .forEach(it -> characteristicsCalculator.evaluate(it));
        return new SEFFActionSequenceElement<>(this, characteristicsCalculator.getCalculatedCharacteristics(), nodeVariables);
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
    	
    	var resourceContainers = allocation.getTargetResourceEnvironment_Allocation().getResourceContainer_ResourceEnvironment();
    	
    	for (ResourceContainer container : resourceContainers) {
    		var nodeCharacteristics = StereotypeAPI.<List<EnumCharacteristic>>getTaggedValueSafe(container, ProfileConstants.characterisable.getValue(), ProfileConstants.characterisable.getStereotype());
        	if (nodeCharacteristics.isPresent()) {
        		var nodeEnumCharacteristics = nodeCharacteristics.get();
        		for (EnumCharacteristic nodeEnumCharacteristic : nodeEnumCharacteristics) {
            		for (Literal nodeLiteral : nodeEnumCharacteristic.getValues()) {
            			nodeVariables.add(new CharacteristicValue(nodeEnumCharacteristic.getType(), nodeLiteral));
            		}
        		}
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
