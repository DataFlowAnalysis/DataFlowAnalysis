package org.dataflowanalysis.analysis.pcm.core;

public interface CallReturnBehavior {

    public boolean isCalling();

    default public boolean isReturning() {
        return !isCalling();
    }

}
