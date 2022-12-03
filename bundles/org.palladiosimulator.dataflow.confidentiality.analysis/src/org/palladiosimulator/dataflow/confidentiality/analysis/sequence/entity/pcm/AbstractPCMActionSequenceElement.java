package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.EnumCharacteristic;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.profile.ProfileConstants;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.palladiosimulator.mdsdprofiles.api.StereotypeAPI;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;

public abstract class AbstractPCMActionSequenceElement<T extends EObject> extends AbstractActionSequenceElement<T> {

    private final Deque<AssemblyContext> context;
    private final T element;	


    public AbstractPCMActionSequenceElement(T element, Deque<AssemblyContext> context) {
        this.element = element;
        this.context = context;
    }
    
    /**
     * Builds a new Sequence element with an existing element and a list of Node and DataFlow variables
     * @param oldElement Old element, which element and context should be copied
     * @param dataFlowVariables DataFlow variables, which should be present for the action sequence element
     * @param nodeVariables Node variables, which should be present for the action sequence element
     */
    public AbstractPCMActionSequenceElement(AbstractPCMActionSequenceElement<T> oldElement, List<DataFlowVariable> dataFlowVariables, List<CharacteristicValue> nodeVariables) {
    	super(dataFlowVariables, nodeVariables);
    	this.element = oldElement.getElement();
    	this.context = oldElement.getContext();
    }
    
    /**
     * Evaluates the node characteristics for an object that can be tagged with Characteristic Values
     * @param object Object that can be tagged with Characteristic Values
     * @return Returns a list of Characteristic Values that tag the provided object
     */
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
