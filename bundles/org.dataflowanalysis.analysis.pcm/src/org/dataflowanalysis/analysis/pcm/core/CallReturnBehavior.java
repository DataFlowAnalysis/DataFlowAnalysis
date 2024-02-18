package org.dataflowanalysis.analysis.pcm.core;

public interface CallReturnBehavior {

    public boolean isCalling();

    public default boolean isReturning() {
        return !isCalling();
    }
}
