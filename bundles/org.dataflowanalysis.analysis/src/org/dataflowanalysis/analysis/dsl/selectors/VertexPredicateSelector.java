package org.dataflowanalysis.analysis.dsl.selectors;

import java.util.function.Predicate;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;

public class VertexPredicateSelector extends VertexSelector {
    private final Predicate<AbstractVertex<?>> predicate;

    public VertexPredicateSelector(DSLContext context, Predicate<AbstractVertex<?>> predicate) {
        super(context);
        this.predicate = predicate;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        return predicate.test(vertex);
    }
}
