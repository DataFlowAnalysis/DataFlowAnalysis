package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.DSLContext;
import org.dataflowanalysis.analysis.dsl.Intersection;

public class IntersectionConditionalSelector implements ConditionalSelector {
    private final Intersection intersection;

    public IntersectionConditionalSelector(Intersection intersection) {
        this.intersection = intersection;
    }

    @Override
    public boolean matchesSelector(AbstractVertex<?> vertex, DSLContext context) {
        return intersection.match(vertex, context).isEmpty();
    }
}
