package org.dataflowanalysis.analysis.dfd.core;

import java.util.HashSet;

import org.dataflowanalysis.analysis.core.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;

public class DFDPartialFlowGraph extends AbstractPartialFlowGraph implements Comparable<DFDPartialFlowGraph> {

    public DFDPartialFlowGraph(AbstractVertex<?> sink) {
        super(sink);
    }

    @Override
    public int compareTo(DFDPartialFlowGraph o) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public AbstractPartialFlowGraph evaluate() {
        DFDVertex newSink = ((DFDVertex) sink).clone();
        newSink.unify(new HashSet<>());
        newSink.evaluateDataFlow();
        return new DFDPartialFlowGraph(newSink);
    }
}