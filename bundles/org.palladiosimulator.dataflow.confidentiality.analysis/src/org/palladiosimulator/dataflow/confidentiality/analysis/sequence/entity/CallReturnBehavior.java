package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity;

public interface CallReturnBehavior {

    public boolean isCalling();

    default public boolean isReturning() {
        return !isCalling();
    }

}
