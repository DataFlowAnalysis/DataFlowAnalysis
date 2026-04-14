package org.dataflowanalysis.analysis.dsl.logic;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;

public class XorLogicalOperator extends LogicalOperator {
    private final AbstractSelector lhs;
    private final AbstractSelector rhs;

    public XorLogicalOperator(AbstractSelector lhs, AbstractSelector rhs, DSLContext context) {
        super(context);
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        return lhs.matches(vertex) ^ rhs.matches(vertex);
    }
}
