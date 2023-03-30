package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.PCMResourceLoader;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.PCMDataCharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.PCMNodeCharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.PCMQueryUtils;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.SetVariableAction;
import org.palladiosimulator.pcm.seff.StartAction;

public class SEFFActionSequenceElement<T extends AbstractAction> extends AbstractPCMActionSequenceElement<T> {
	private final Logger logger = Logger.getLogger(SEFFActionSequenceElement.class);
	
	private final List<Parameter> parameter;
	
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
        this.parameter = oldElement.getParameter();
    }

    @Override
    public AbstractActionSequenceElement<T> evaluateDataFlow(List<DataFlowVariable> variables, PCMResourceLoader resourceLoader) {
    	List<CharacteristicValue> nodeCharacteristics = this.evaluateNodeCharacteristics(resourceLoader);
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
    	
    	PCMDataCharacteristicsCalculator characteristicsCalculator = new PCMDataCharacteristicsCalculator(variables, nodeCharacteristics, resourceLoader);
        variableCharacterisations.forEach(it -> characteristicsCalculator.evaluate(it));
        return new SEFFActionSequenceElement<T>(this, characteristicsCalculator.getCalculatedCharacteristics(), nodeCharacteristics);
    }
    
    /**
     * Returns a list of Characteristic values that are present at the node using the {@link PCMNodeCharacteristicsCalculator}
     * @return List of Characteristic values present at node
     */
    protected List<CharacteristicValue> evaluateNodeCharacteristics(PCMResourceLoader resourceLoader) {
    	PCMNodeCharacteristicsCalculator characteristicsCalculator = new PCMNodeCharacteristicsCalculator(this.getElement(), resourceLoader);
    	return characteristicsCalculator.getNodeCharacteristics(Optional.of(this.getContext()));
    }
    
    /**
     * Returns a list of parameters, that the SEFF was called with
     * @return List of parameters present for SEFF
     */
    public List<Parameter> getParameter() {
		return parameter;
	}
    
    /**
     * Returns whether a SEFF Action Sequence Element (i.e. Start Action) was created due to branching behavior
     * @return Returns true, if the SEFF Action was created, because branching behavior was defined. Otherwise, the method returns false.
     */
    public boolean isBranching() {
    	Optional<BranchAction> branchAction = PCMQueryUtils.findParentOfType(this.getElement(), BranchAction.class, false);
    	return branchAction.isPresent();
    }

    @Override
    public String toString() {
    	String elementName = this.getElement().getEntityName();
    	if (this.getElement() instanceof StartAction) {
    		Optional<ResourceDemandingSEFF> seff = PCMQueryUtils.findParentOfType(this.getElement(), ResourceDemandingSEFF.class, false);
    		if (seff.isPresent()) {
    			elementName = "Beginning " + seff.get().getDescribedService__SEFF().getEntityName();
    		}
    		if (this.isBranching()) {
    			Optional<BranchAction> branchAction = PCMQueryUtils.findParentOfType(this.getElement(), BranchAction.class, false);
    			Optional<AbstractBranchTransition> branchTransition = PCMQueryUtils.findParentOfType(this.getElement(), AbstractBranchTransition.class, false);
    			elementName = "Branching " + seff.get().getDescribedService__SEFF().getEntityName() + "." + branchAction.get().getEntityName() + "." +  branchTransition.get().getEntityName();
    		}
    	}
        return String.format("%s (%s, %s))", this.getClass()
            .getSimpleName(),
                elementName,
                this.getElement()
                    .getId());
    }

}
