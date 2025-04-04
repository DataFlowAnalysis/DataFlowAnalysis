package org.dataflowanalysis.analysis.dsl;

import java.util.Optional;

/**
 * Contains the {@link SourceSelectors} of an {@link AnalysisConstraint}. It stores {@link DataSourceSelectors} and
 * {@link VertexSourceSelectors} that describe the origin of the flow
 */
public final class SourceSelectors {
    private final Optional<DataSourceSelectors> dataSourceSelectors;
    private final Optional<VertexSourceSelectors> nodeSourceSelectors;

    public SourceSelectors(DataSourceSelectors dataSourceSelectors, VertexSourceSelectors vertexSourceSelectors) {
        this.dataSourceSelectors = Optional.of(dataSourceSelectors);
        this.nodeSourceSelectors = Optional.of(vertexSourceSelectors);
    }

    public SourceSelectors(DataSourceSelectors dataSourceSelectors) {
        this.dataSourceSelectors = Optional.of(dataSourceSelectors);
        this.nodeSourceSelectors = Optional.empty();
    }

    public SourceSelectors(VertexSourceSelectors vertexSourceSelectors) {
        this.dataSourceSelectors = Optional.empty();
        this.nodeSourceSelectors = Optional.of(vertexSourceSelectors);
    }

    public Optional<DataSourceSelectors> getDataSourceSelectors() {
        return dataSourceSelectors;
    }

    public Optional<VertexSourceSelectors> getNodeSourceSelectors() {
        return nodeSourceSelectors;
    }
}
