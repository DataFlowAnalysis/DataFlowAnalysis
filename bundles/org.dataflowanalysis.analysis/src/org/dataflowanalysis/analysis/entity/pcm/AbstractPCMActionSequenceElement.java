package org.dataflowanalysis.analysis.entity.pcm;

import java.util.Deque;
import java.util.List;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.builder.AnalysisData;
import org.dataflowanalysis.analysis.characteristics.CharacteristicValue;
import org.dataflowanalysis.analysis.characteristics.DataFlowVariable;
import org.dataflowanalysis.analysis.characteristics.variable.DataCharacteristicsCalculator;
import org.dataflowanalysis.analysis.entity.sequence.AbstractActionSequenceElement;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.repository.OperationSignature;

public abstract class AbstractPCMActionSequenceElement<T extends Entity> extends AbstractActionSequenceElement<T> {
	private final Logger logger = Logger.getLogger(AbstractPCMActionSequenceElement.class);
	
    private final Deque<AssemblyContext> context;
    private final T element;	


    /**
     * Constructs a new Action Sequence Element with the underlying Palladio Element and Assembly Context
     * @param element Underlying Palladio Element of the Sequence Element
     * @param context Assembly context of the Palladio Element
     */
    public AbstractPCMActionSequenceElement(T element, Deque<AssemblyContext> context) {
        this.element = element;
        this.context = context;
    }
    
    /**
     * Builds a new Sequence element with an existing element and a list of Node and DataFlow variables
     * @param oldElement Old element, which element and context should be copied
     * @param dataFlowVariables DataFlow variables, which should be present for the action sequence element
     * @param nodeCharacteristics Node characteristics, which should be present for the action sequence element
     */
    public AbstractPCMActionSequenceElement(AbstractPCMActionSequenceElement<T> oldElement, List<DataFlowVariable> dataFlowVariables, List<CharacteristicValue> nodeCharacteristics) {
    	super(dataFlowVariables, nodeCharacteristics);
    	this.element = oldElement.getElement();
    	this.context = oldElement.getContext();
    }
    
    /**
     * Calculate the node characteristics for the sequence element with the given analysis data
     * @param analysisData Analysis data containing the node characteristics calculator
     * @return Returns a list of node characteristics that are applied to the sequence element
     */
    protected List<CharacteristicValue> getNodeCharacteristics(AnalysisData analysisData) {
    	return analysisData.getNodeCharacteristicsCalculator().getNodeCharacteristics(this.element, this.context);
    }
    
    /**
     * Calculate the data characteristics for the sequence element with the given analysis data
     * @param analysisData Analysis data containing the node characteristics calculator
     * @return Returns a list of data characteristics that are applied to the sequence element
     */
    protected List<DataFlowVariable> getDataFlowVariables(AnalysisData analysisData, 
    		List<CharacteristicValue> nodeCharacteristics, List<VariableCharacterisation> variableCharacterisations, List<DataFlowVariable> oldDataFlowVariables) {
    	DataCharacteristicsCalculator dataCharacteristicsCalculator = analysisData.getVariableCharacteristicsCalculator().createNodeCalculator(oldDataFlowVariables, nodeCharacteristics);
    	variableCharacterisations.forEach(dataCharacteristicsCalculator::evaluate);
    	return dataCharacteristicsCalculator.getCalculatedCharacteristics();
    }
    
    /**
     * Checks the parameters to the call signature for characterizations that have no impact on the results at all
     * @param callSigniture Call Signature of the call
     * @param variableCharacterisations Variable characterizations that are applied to the sequence element
     */
    protected void checkCallParameter(OperationSignature callSigniture, List<VariableCharacterisation> variableCharacterisations) {
    	List<String> parameter = callSigniture.getParameters__OperationSignature().stream()
    			.map(it -> it.getParameterName())
    			.toList();
    	
    	List<String> referencedParameter =
    			variableCharacterisations.stream()
    			.map(it -> it.getVariableUsage_VariableCharacterisation().getNamedReference__VariableUsage().getReferenceName())
    			.toList();
    	
    	referencedParameter.stream()
    		.filter(it -> !parameter.contains(it))
    		.forEach(it -> {
    			logger.warn("Unknown reference to variable " + it + " in variable characterisation in element " + element);
    			logger.warn("Present variables:" + parameter + ", Referenced parameter: " + referencedParameter);
    	});
    }

    /**
     * Return the saved element of the sequence element
     * @return
     */
    public T getElement() {
        return element;
    }

    /**
     * Returns the assembly contexts of the sequence element
     * @return Returns a {@link Deque} of {@link AssemblyContext}s that the sequence element has
     */
    public Deque<AssemblyContext> getContext() {
        return context;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(context, element);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("rawtypes")
        AbstractPCMActionSequenceElement other = (AbstractPCMActionSequenceElement) obj;
        return Objects.equals(context, other.context) && Objects.equals(element, other.element);
    }

}
