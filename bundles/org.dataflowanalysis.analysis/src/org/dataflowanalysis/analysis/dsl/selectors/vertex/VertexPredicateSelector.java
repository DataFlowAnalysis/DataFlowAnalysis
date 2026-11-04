package org.dataflowanalysis.analysis.dsl.selectors.vertex;

import java.util.function.Predicate;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.result.DSLConstraintTrace;

public class VertexPredicateSelector extends VertexSelector {
    private final Predicate<AbstractVertex<?>> predicate;

    public VertexPredicateSelector(DSLContext context, Predicate<AbstractVertex<?>> predicate) {
        super(context);
        this.predicate = predicate;
    }

    @Override
    public boolean matchesSource(AbstractVertex<?> vertex, DSLConstraintTrace dslConstraintTrace) {
        return predicate.test(vertex);
    }

    @Override
    public boolean matchesDestination(AbstractVertex<?> vertex, DSLConstraintTrace dslConstraintTrace) {
        return predicate.test(vertex);
    }
}
