package org.palladiosimulator.dataflow.confidentiality.analysis.entity;

public interface CallReturnBehavior {

    public boolean isCalling();

    default public boolean isReturning() {
        return !isCalling();
    }

}
