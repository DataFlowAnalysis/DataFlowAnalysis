package org.dataflowanalysis.analysis.dfd.core;

import java.util.HashSet;

import org.dataflowanalysis.analysis.core.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;

public class DFDPartialFlowGraph extends AbstractPartialFlowGraph {

    public DFDPartialFlowGraph(AbstractVertex<?> sink) {
        super(sink);
    }

    @Override
    public AbstractPartialFlowGraph evaluate() {
        DFDVertex newSink = ((DFDVertex) sink).clone();
        newSink.unify(new HashSet<>());
        newSink.evaluateDataFlow();
        return new DFDPartialFlowGraph(newSink);
    }  
  
}
