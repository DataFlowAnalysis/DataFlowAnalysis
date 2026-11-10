package org.dataflowanalysis.analysis.dsl;

import java.util.Optional;
import java.util.StringJoiner;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

/**
 * Contains the {@link SourceSelectors} of an {@link AnalysisConstraint}. It stores {@link DataSourceSelectors} and
 * {@link VertexSourceSelectors} that describe the origin of the flow
 */
public final class SourceSelectors extends AbstractParseable {
    private final Optional<DataSourceSelectors> dataSourceSelectors;
    private final Optional<VertexSourceSelectors> vertexSourceSelectors;

    public SourceSelectors(DataSourceSelectors dataSourceSelectors, VertexSourceSelectors vertexSourceSelectors) {
        this.dataSourceSelectors = Optional.of(dataSourceSelectors);
        this.vertexSourceSelectors = Optional.of(vertexSourceSelectors);
    }

    public SourceSelectors(DataSourceSelectors dataSourceSelectors) {
        this.dataSourceSelectors = Optional.of(dataSourceSelectors);
        this.vertexSourceSelectors = Optional.empty();
    }

    public SourceSelectors(VertexSourceSelectors vertexSourceSelectors) {
        this.dataSourceSelectors = Optional.empty();
        this.vertexSourceSelectors = Optional.of(vertexSourceSelectors);
    }

    public SourceSelectors() {
        this.dataSourceSelectors = Optional.empty();
        this.vertexSourceSelectors = Optional.empty();
    }

    public Optional<DataSourceSelectors> getDataSourceSelectors() {
        return dataSourceSelectors;
    }

    public Optional<VertexSourceSelectors> getVertexSourceSelectors() {
        return vertexSourceSelectors;
    }

    public static ParseResult<SourceSelectors> fromString(StringView string, DSLContext context) {
        string.skipWhitespace();
        ParseResult<DataSourceSelectors> dataSourceSelector = DataSourceSelectors.fromString(string, context);
        ParseResult<VertexSourceSelectors> nodeSourceSelector;
        if (dataSourceSelector.successful()) {
            string.skipWhitespace();
            nodeSourceSelector = VertexSourceSelectors.fromString(string, context);
        } else {
            string.skipWhitespace();
            nodeSourceSelector = VertexSourceSelectors.fromString(string, context);
            if (nodeSourceSelector.successful()) {
                string.skipWhitespace();
                dataSourceSelector = DataSourceSelectors.fromString(string, context);
            }
        }

        if (nodeSourceSelector.successful() && dataSourceSelector.successful()) {
            return ParseResult.ok(new SourceSelectors(dataSourceSelector.getResult(), nodeSourceSelector.getResult()));
        } else if (dataSourceSelector.successful()) {
            return ParseResult.ok(new SourceSelectors(dataSourceSelector.getResult()));
        } else if (nodeSourceSelector.successful()) {
            return ParseResult.ok(new SourceSelectors(nodeSourceSelector.getResult()));
        } else {
            return ParseResult.ok(new SourceSelectors());
        }
    }

    @Override
    public String toString() {
        StringJoiner dslString = new StringJoiner(" ");
        if (this.dataSourceSelectors.isPresent() && !this.dataSourceSelectors.get()
                .getSelectors()
                .isEmpty()) {
            dslString.add(this.dataSourceSelectors.get()
                    .toString());
        }
        if (this.vertexSourceSelectors.isPresent() && !this.vertexSourceSelectors.get()
                .getSelectors()
                .isEmpty()) {
            dslString.add(this.vertexSourceSelectors.get()
                    .toString());
        }
        return dslString.toString();
    }
}
