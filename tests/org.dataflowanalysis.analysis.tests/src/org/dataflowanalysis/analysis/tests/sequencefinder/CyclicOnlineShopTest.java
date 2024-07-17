package org.dataflowanalysis.analysis.tests.sequencefinder;

import org.dataflowanalysis.analysis.core.TransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.core.DFDCyclicTransposeFlowGraphFinder;

public class CyclicOnlineShopTest extends BaseOnlineShopTest{

    @Override
    protected Class<? extends TransposeFlowGraphFinder> getTransposeFlowGraphFinder() {
        return DFDCyclicTransposeFlowGraphFinder.class;
    }

}
