package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.palladiosimulator.dataflow.confidentiality.analysis.PCMAnalysisUtils;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.DataFlowCharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.NodeCharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.PCMQueryUtils;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.allocation.AllocationPackage;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.SetVariableAction;
import org.palladiosimulator.pcm.seff.StartAction;

public class SEFFActionSequenceElement<T extends AbstractAction> extends AbstractPCMActionSequenceElement<T> {
	private List<Parameter> parameter;
	
    public SEFFActionSequenceElement(T element, Deque<AssemblyContext> context, List<Parameter> parameter) {
        super(element, context);
        this.parameter = parameter;
    }

    public SEFFActionSequenceElement(SEFFActionSequenceElement<T> oldElement, List<DataFlowVariable> dataFlowVariables, List<CharacteristicValue> nodeVariables) {
        super(oldElement, dataFlowVariables, nodeVariables);
    }

    @Override
    public AbstractActionSequenceElement<T> evaluateDataFlow(List<DataFlowVariable> variables) {
    	List<CharacteristicValue> nodeCharacteristics = this.evaluateNodeCharacteristics();
        if (this.getElement() instanceof StartAction) {
        	List<String> dataflowParameter = this.parameter.stream()
        			.map(it -> it.getParameterName())
        			.collect(Collectors.toList());
        	List<DataFlowVariable> passedVariables = variables.stream()
        			.filter(it -> dataflowParameter.contains(it.variableName()))
        			.collect(Collectors.toList());
        	return new SEFFActionSequenceElement<T>(this, new ArrayList<>(passedVariables), nodeCharacteristics);
    	} else if (!(this.getElement() instanceof SetVariableAction)) {
    		throw new IllegalStateException("Unexpected action sequence element with unknown PCM type");
    	}
    	List<VariableCharacterisation> variableCharacterisations = ((SetVariableAction) this.getElement())
                .getLocalVariableUsages_SetVariableAction()
                .stream()
                .flatMap(it -> it.getVariableCharacterisation_VariableUsage()
                    .stream())
                .toList();
    	DataFlowCharacteristicsCalculator characteristicsCalculator = new DataFlowCharacteristicsCalculator(variables, nodeCharacteristics);
        variableCharacterisations.forEach(it -> characteristicsCalculator.evaluate(it));
        return new SEFFActionSequenceElement<T>(this, characteristicsCalculator.getCalculatedCharacteristics(), nodeCharacteristics);
    }
    
    protected List<CharacteristicValue> evaluateNodeCharacteristics() {
    	NodeCharacteristicsCalculator characteristicsCalculator = new NodeCharacteristicsCalculator(this.getElement());
    	return characteristicsCalculator.getNodeCharacteristics(Optional.of(this.getContext()));
    }
    
    public List<Parameter> getParameter() {
		return parameter;
	}

    @Override
    public String toString() {
    	String elementName = this.getElement().getEntityName();
    	if (this.getElement() instanceof StartAction) {
    		Optional<ResourceDemandingSEFF> seff = PCMQueryUtils.findParentOfType(this.getElement(), ResourceDemandingSEFF.class, false);
    		if (seff.isPresent()) {
    			elementName = "Begining " + seff.get().getDescribedService__SEFF().getEntityName();
    		}
    	}
        return String.format("%s (%s, %s))", this.getClass()
            .getSimpleName(),
                elementName,
                this.getElement()
                    .getId());
    }

}
