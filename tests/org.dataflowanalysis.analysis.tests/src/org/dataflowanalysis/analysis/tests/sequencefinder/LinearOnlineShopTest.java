package org.dataflowanalysis.analysis.tests.sequencefinder;

import org.dataflowanalysis.analysis.core.TransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraphFinder;

public class LinearOnlineShopTest extends BaseOnlineShopTest{

    @Override
    protected Class<? extends TransposeFlowGraphFinder> getTransposeFlowGraphFinder() {
        return DFDTransposeFlowGraphFinder.class;
    }

}
