package org.dataflowanalysis.analysis.pcm.flowgraph;

public interface CallReturnBehavior {

  public boolean isCalling();

  public default boolean isReturning() {
    return !isCalling();
  }
}
