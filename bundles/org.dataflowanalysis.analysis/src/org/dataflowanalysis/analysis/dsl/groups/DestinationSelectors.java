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
 * Contains the {@link DestinationSelectors} of an {@link AnalysisConstraint}. It stores
 * {@link DataDestinationSelectors} and {@link VertexDestinationSelectors} that describe the destination of the flow
 */
public final class DestinationSelectors extends AbstractParseable {
    private final DataDestinationSelectors dataDestinationSelectors;
    private final VertexDestinationSelectors vertexDestinationSelectors;

    public DestinationSelectors() {
        this.dataDestinationSelectors = new DataDestinationSelectors();
        this.vertexDestinationSelectors = new VertexDestinationSelectors();
    }

    public DestinationSelectors(DataDestinationSelectors dataDestinationSelectors,
            VertexDestinationSelectors vertexDestinationSelectors) {
        this.dataDestinationSelectors = dataDestinationSelectors;
        this.vertexDestinationSelectors = vertexDestinationSelectors;
    }

    public void addDataDestinationSelector(AbstractSelector selector) {
        this.dataDestinationSelectors.addSelector(selector);
    }

    public DataDestinationSelectors getDataDestinationSelectors() {
        return dataDestinationSelectors;
    }

    public void addVertexDestinationSelector(AbstractSelector selector) {
        this.vertexDestinationSelectors.addSelector(selector);
    }

    public VertexDestinationSelectors getVertexDestinationSelectors() {
        return vertexDestinationSelectors;
    }

    public List<AbstractSelector> getSelectors() {
        return Streams.concat(this.dataDestinationSelectors.getSelectors()
                .stream(),
                this.vertexDestinationSelectors.getSelectors()
                        .stream())
                .toList();
    }

    public static ParseResult<DestinationSelectors> fromString(StringView string, DSLContext context) {
        string.skipWhitespace();
        ParseResult<DataDestinationSelectors> dataDestinationSelector = DataDestinationSelectors.fromString(string,
                context);
        ParseResult<VertexDestinationSelectors> vertexDestinationSelector;
        if (dataDestinationSelector.successful()) {
            string.skipWhitespace();
            vertexDestinationSelector = VertexDestinationSelectors.fromString(string, context);
        } else {
            string.skipWhitespace();
            vertexDestinationSelector = VertexDestinationSelectors.fromString(string, context);
            if (vertexDestinationSelector.successful()) {
                string.skipWhitespace();
                dataDestinationSelector = DataDestinationSelectors.fromString(string, context);
            }
        }

        if (vertexDestinationSelector.successful() && dataDestinationSelector.successful()) {
            return ParseResult.ok(new DestinationSelectors(dataDestinationSelector.getResult(),
                    vertexDestinationSelector.getResult()));
        } else if (dataDestinationSelector.successful()) {
            return ParseResult.ok(
                    new DestinationSelectors(dataDestinationSelector.getResult(), new VertexDestinationSelectors()));
        } else if (vertexDestinationSelector.successful()) {
            return ParseResult.ok(
                    new DestinationSelectors(new DataDestinationSelectors(), vertexDestinationSelector.getResult()));
        } else {
            return ParseResult.ok(new DestinationSelectors());
        }
    }

    @Override
    public String toString() {
        StringJoiner dslString = new StringJoiner(" ");
        if (!this.dataDestinationSelectors.getSelectors()
                .isEmpty()) {
            dslString.add(this.dataDestinationSelectors.toString());
        }
        if (!this.vertexDestinationSelectors.getSelectors()
                .isEmpty()) {
            dslString.add(this.vertexDestinationSelectors.toString());
        }
        return dslString.toString();
    }
}
