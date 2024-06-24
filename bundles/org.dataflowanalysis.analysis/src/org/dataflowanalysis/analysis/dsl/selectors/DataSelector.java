package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.dsl.context.DSLContext;

public abstract class DataSelector extends AbstractSelector {
    public DataSelector(DSLContext context) {
        super(context);
    }
}
