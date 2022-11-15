package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.CharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.EnumCharacteristic;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.profile.ProfileConstants;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.palladiosimulator.mdsdprofiles.api.StereotypeAPI;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.seff.SetVariableAction;
import org.palladiosimulator.pcm.seff.StartAction;

public abstract class AbstractPCMActionSequenceElement<T extends EObject> extends AbstractActionSequenceElement<T> {

    private final Deque<AssemblyContext> context;
    private final T element;

    public AbstractPCMActionSequenceElement(T element, Deque<AssemblyContext> context) {
        this.element = element;
        this.context = context;
    }
    
    public AbstractPCMActionSequenceElement(AbstractPCMActionSequenceElement<T> oldElement, List<DataFlowVariable> dataFlowVariables, List<CharacteristicValue> nodeVariables) {
    	super(dataFlowVariables, nodeVariables);
    	this.element = oldElement.getElement();
    	this.context = oldElement.getContext();
    }
   
    protected List<DataFlowVariable> evaluateDataFlowCharacteristics(List<DataFlowVariable> variables, List<CharacteristicValue> nodeCharacteristics) {
    	if (this.getElement() instanceof StartAction) {
    		return variables;
    	} else if (!(this.getElement() instanceof SetVariableAction)) {
    		throw new IllegalStateException("Unexpected action sequence element with unknown PCM type");
    	}
    	List<VariableCharacterisation> variableCharacterisations = ((SetVariableAction) this.getElement())
                .getLocalVariableUsages_SetVariableAction()
                .stream()
                .flatMap(it -> it.getVariableCharacterisation_VariableUsage()
                    .stream())
                .toList();
            CharacteristicsCalculator characteristicsCalculator = new CharacteristicsCalculator(variables, nodeCharacteristics);
            variableCharacterisations.forEach(it -> characteristicsCalculator.evaluate(it));
            return characteristicsCalculator.getCalculatedCharacteristics();
    }
    
    protected List<CharacteristicValue> evaluateNodeCharacteristics(EObject object) {
    	List<CharacteristicValue> nodeCharacteristics = new ArrayList<>();
    	var enumCharacteristics = StereotypeAPI.<List<EnumCharacteristic>>getTaggedValueSafe(object, ProfileConstants.characterisable.getValue(), ProfileConstants.characterisable.getStereotype());
    	if (enumCharacteristics.isPresent()) {
    		var nodeEnumCharacteristics = enumCharacteristics.get();
    		for (EnumCharacteristic nodeEnumCharacteristic : nodeEnumCharacteristics) {
        		for (Literal nodeLiteral : nodeEnumCharacteristic.getValues()) {
        			nodeCharacteristics.add(new CharacteristicValue(nodeEnumCharacteristic.getType(), nodeLiteral));
        		}
    		}
    	}
    	return nodeCharacteristics;
    }

    public T getElement() {
        return element;
    }

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
