package org.dataflowanalysis.analysis.pcm.core;

public interface CallReturnBehavior {

    boolean isCalling();

    default boolean isReturning() {
        return !isCalling();
    }
}
