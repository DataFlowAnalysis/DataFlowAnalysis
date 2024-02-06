package org.dataflowanalysis.analysis.pcm.flowgraph;

public interface CallReturnBehavior {

    public boolean isCalling();

    default public boolean isReturning() {
        return !isCalling();
    }

}
