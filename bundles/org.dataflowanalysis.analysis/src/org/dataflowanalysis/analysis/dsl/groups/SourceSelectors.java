package org.dataflowanalysis.analysis.dsl.groups;

import com.google.common.collect.Streams;
import java.util.List;
import java.util.StringJoiner;
import org.dataflowanalysis.analysis.dsl.AbstractParseable;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

/**
 * Contains the {@link SourceSelectors} of an {@link AnalysisConstraint}. It stores {@link DataSourceSelectors} and
 * {@link VertexSourceSelectors} that describe the origin of the flow
 */
public final class SourceSelectors extends AbstractParseable {
    private final DataSourceSelectors dataSourceSelectors;
    private final VertexSourceSelectors vertexSourceSelectors;

    public SourceSelectors() {
        this.dataSourceSelectors = new DataSourceSelectors();
        this.vertexSourceSelectors = new VertexSourceSelectors();
    }

    public SourceSelectors(DataSourceSelectors dataSourceSelectors, VertexSourceSelectors vertexSourceSelectors) {
        this.dataSourceSelectors = dataSourceSelectors;
        this.vertexSourceSelectors = vertexSourceSelectors;
    }

    public List<AbstractSelector> getSelectors() {
        return Streams.concat(this.dataSourceSelectors.getSelectors()
                .stream(),
                this.vertexSourceSelectors.getSelectors()
                        .stream())
                .toList();
    }

    public void addDataSourceSelector(AbstractSelector selector) {
        this.dataSourceSelectors.addSelector(selector);
    }

    public DataSourceSelectors getDataSourceSelectors() {
        return dataSourceSelectors;
    }

    public void addVertexSourceSelector(AbstractSelector selector) {
        this.vertexSourceSelectors.addSelector(selector);
    }

    public VertexSourceSelectors getVertexSourceSelectors() {
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
            return ParseResult.ok(new SourceSelectors(dataSourceSelector.getResult(), new VertexSourceSelectors()));
        } else if (nodeSourceSelector.successful()) {
            return ParseResult.ok(new SourceSelectors(new DataSourceSelectors(), nodeSourceSelector.getResult()));
        } else {
            return ParseResult.ok(new SourceSelectors());
        }
    }

    @Override
    public String toString() {
        StringJoiner dslString = new StringJoiner(" ");
        if (!this.dataSourceSelectors.getSelectors()
                .isEmpty()) {
            dslString.add(this.dataSourceSelectors.toString());
        }
        if (!this.vertexSourceSelectors.getSelectors()
                .isEmpty()) {
            dslString.add(this.vertexSourceSelectors.toString());
        }
        return dslString.toString();
    }
}
