package org.dataflowanalysis.analysis.dsl;

import javax.swing.text.html.Option;
import java.util.Optional;

public final class SourceSelectors {
    private final Optional<DataSourceSelectors> dataSourceSelectors;
    private final Optional<NodeSourceSelectors> nodeSourceSelectors;

    public SourceSelectors(DataSourceSelectors dataSourceSelectors, NodeSourceSelectors nodeSourceSelectors) {
        this.dataSourceSelectors = Optional.of(dataSourceSelectors);
        this.nodeSourceSelectors = Optional.of(nodeSourceSelectors);
    }

    public SourceSelectors(DataSourceSelectors dataSourceSelectors) {
        this.dataSourceSelectors = Optional.of(dataSourceSelectors);
        this.nodeSourceSelectors = Optional.empty();
    }

    public SourceSelectors(NodeSourceSelectors nodeSourceSelectors) {
        this.dataSourceSelectors = Optional.empty();
        this.nodeSourceSelectors = Optional.of(nodeSourceSelectors);
    }
    

    public Optional<DataSourceSelectors> getDataSourceSelectors() {
        return dataSourceSelectors;
    }

    public Optional<NodeSourceSelectors> getNodeSourceSelectors() {
        return nodeSourceSelectors;
    }
}
