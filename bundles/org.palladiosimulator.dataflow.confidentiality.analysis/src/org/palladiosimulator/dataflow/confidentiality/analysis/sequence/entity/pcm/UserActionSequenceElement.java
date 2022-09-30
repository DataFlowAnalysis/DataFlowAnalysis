package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayDeque;
import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.CharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.seff.SetVariableAction;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

public class UserActionSequenceElement<T extends AbstractUserAction> extends AbstractPCMActionSequenceElement<T> {

    public UserActionSequenceElement(T element) {
        super(element, new ArrayDeque<>());
    }

    public UserActionSequenceElement(UserActionSequenceElement<T> oldElement, List<DataFlowVariable> variables) {
        super(oldElement, variables);
    }

    @Override
    public AbstractActionSequenceElement<T> evaluateDataFlow(List<DataFlowVariable> variables) {
        List<VariableCharacterisation> dataflowElements = ((SetVariableAction) super.getElement())
            .getLocalVariableUsages_SetVariableAction()
            .stream()
            .flatMap(it -> it.getVariableCharacterisation_VariableUsage()
                .stream())
            .toList();
        CharacteristicsCalculator characteristicsCalculator = new CharacteristicsCalculator(variables);
        dataflowElements.forEach(it -> characteristicsCalculator.evaluate(it));
        return new UserActionSequenceElement<T>(this, characteristicsCalculator.getCalculatedCharacteristics());
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
