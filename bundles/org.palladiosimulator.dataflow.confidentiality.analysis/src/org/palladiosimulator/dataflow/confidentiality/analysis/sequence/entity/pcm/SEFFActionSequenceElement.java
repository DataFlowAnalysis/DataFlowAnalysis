package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.Deque;
import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.CharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.SetVariableAction;

public class SEFFActionSequenceElement<T extends AbstractAction> extends AbstractPCMActionSequenceElement<T> {

    public SEFFActionSequenceElement(T element, Deque<AssemblyContext> context) {
        super(element, context);
        // TODO Auto-generated constructor stub
    }

    public SEFFActionSequenceElement(SEFFActionSequenceElement<T> oldElement, List<DataFlowVariable> variables) {
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
        dataflowElements.stream()
            .forEach(it -> characteristicsCalculator.evaluate(it));
        return new SEFFActionSequenceElement<>(this, characteristicsCalculator.getCalculatedCharacteristics());
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
