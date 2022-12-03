package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.DataFlowCharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.NodeCharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.PCMQueryUtils;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.SetVariableAction;
import org.palladiosimulator.pcm.seff.StartAction;

public class SEFFActionSequenceElement<T extends AbstractAction> extends AbstractPCMActionSequenceElement<T> {
	private final Logger logger = Logger.getLogger(SEFFActionSequenceElement.class);
	
	private List<Parameter> parameter;
	
	/**
	 * Construct a new SEFF Action Sequence element with the given underlying Palladio Element, {@link AssemblyContext} and a list of passed {@link Parameter}.
	 * @param element Underlying Palladio SEFF Element
	 * @param context Assembly context of the SEFF Element
	 * @param parameter List of parameters, that were passed to the SEFF Element
	 */
    public SEFFActionSequenceElement(T element, Deque<AssemblyContext> context, List<Parameter> parameter) {
        super(element, context);
        this.parameter = parameter;
    }

    /**
     * Create a new SEFF Action Sequence element using an old SEFF Action Sequence element and a updated list of dataflow variables and node characteristics
     * @param oldElement Old SEFF Action Sequence Element
     * @param dataFlowVariables Updated dataflow variables
     * @param nodeCharacteristics Updated node characteristics
     */
    public SEFFActionSequenceElement(SEFFActionSequenceElement<T> oldElement, List<DataFlowVariable> dataFlowVariables, List<CharacteristicValue> nodeCharacteristics) {
        super(oldElement, dataFlowVariables, nodeCharacteristics);
    }

    @Override
    public AbstractActionSequenceElement<T> evaluateDataFlow(List<DataFlowVariable> variables) {
    	List<CharacteristicValue> nodeCharacteristics = this.evaluateNodeCharacteristics();
        if (this.getElement() instanceof StartAction) {
        	return new SEFFActionSequenceElement<T>(this, new ArrayList<>(variables), nodeCharacteristics);
    	} else if (!(this.getElement() instanceof SetVariableAction)) {
    		logger.error("Found unexpected sequence element of unknown PCM type " + this.getElement().getClass().getName());
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
    
    /**
     * Returns a list of Characteristic values that are present at the node using the {@link NodeCharacteristicsCalculator}
     * @return List of Characteristic values present at node
     */
    protected List<CharacteristicValue> evaluateNodeCharacteristics() {
    	NodeCharacteristicsCalculator characteristicsCalculator = new NodeCharacteristicsCalculator(this.getElement());
    	return characteristicsCalculator.getNodeCharacteristics(Optional.of(this.getContext()));
    }
    
    /**
     * Returns a list of parameters, that the SEFF was called with
     * @return List of parameters present for SEFF
     */
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
