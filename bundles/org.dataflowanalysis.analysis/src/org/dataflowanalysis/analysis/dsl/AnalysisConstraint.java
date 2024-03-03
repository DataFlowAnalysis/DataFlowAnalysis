package org.dataflowanalysis.analysis.dsl;

import org.apache.commons.lang.NotImplementedException;
import org.dataflowanalysis.analysis.core.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;

import java.util.List;

public class AnalysisConstraint {
    private List<AbstractSelector> flowSource;
    private List<AbstractSelector> flowDestination;

    // TODO: Can we look at partial flow graphs independently?
    public List<AbstractVertex<?>> matchPartialFlowGraph(AbstractPartialFlowGraph partialFlowGraph) {
        throw new NotImplementedException();
    }
}
