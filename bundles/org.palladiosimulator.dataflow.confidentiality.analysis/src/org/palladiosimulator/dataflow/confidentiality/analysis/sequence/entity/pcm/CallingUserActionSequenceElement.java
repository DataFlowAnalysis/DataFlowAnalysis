package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.CharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CallReturnBehavior;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

import com.google.common.collect.Streams;

public class CallingUserActionSequenceElement extends UserActionSequenceElement<EntryLevelSystemCall>
        implements CallReturnBehavior {

    private final boolean isCalling;

    public CallingUserActionSequenceElement(EntryLevelSystemCall element, boolean isCalling) {
        super(element);
        this.isCalling = isCalling;
        // TODO Auto-generated constructor stub
    }

    public CallingUserActionSequenceElement(CallingUserActionSequenceElement oldElement,
            List<DataFlowVariable> variables) {
        super(oldElement, variables);
        this.isCalling = oldElement.isCalling();
    }

    @Override
    public boolean isCalling() {
        return this.isCalling;
    }

    // TODO: Custom hash and equals required?

    /**
     * Input: ccd . GrantedRoles . User := true Elements: variable.characteristicType.value := Term
     */
    @Override
    public AbstractActionSequenceElement<EntryLevelSystemCall> evaluateDataFlow(List<DataFlowVariable> variables) {
        var elementStream = Streams.concat(super.getElement().getInputParameterUsages_EntryLevelSystemCall()
            .stream(),
                super.getElement().getOutputParameterUsages_EntryLevelSystemCall()
                    .stream());
        List<VariableCharacterisation> dataflowElements = elementStream
            .flatMap(it -> it.getVariableCharacterisation_VariableUsage()
                .stream())
            .toList();

        CharacteristicsCalculator characteristicsCalculator = new CharacteristicsCalculator(variables);
        dataflowElements.stream()
            .forEach(it -> characteristicsCalculator.evaluate(it));
        AbstractActionSequenceElement<EntryLevelSystemCall> evaluatedElement = new CallingUserActionSequenceElement(
                this, characteristicsCalculator.getCalculatedCharacteristics());
        return evaluatedElement;
    }

    @Override
    public String toString() {
        String calling = isCalling ? "calling" : "returning";
        return String.format("%s / %s (%s, %s))", this.getClass()
            .getSimpleName(), calling,
                this.getElement()
                    .getEntityName(),
                this.getElement()
                    .getId());
    }

}
