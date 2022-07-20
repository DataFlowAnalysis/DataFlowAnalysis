package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.Deque;
import java.util.Objects;

import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;

public abstract class AbstractPCMActionSequenceElement<T extends EObject> extends AbstractActionSequenceElement<T> {

    private final Deque<AssemblyContext> context;
    private final T element;

    public AbstractPCMActionSequenceElement(T element, Deque<AssemblyContext> context) {
        this.element = element;
        this.context = context;
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
