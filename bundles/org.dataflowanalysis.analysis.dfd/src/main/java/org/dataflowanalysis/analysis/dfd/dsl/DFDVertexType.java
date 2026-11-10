package org.dataflowanalysis.analysis.dfd.dsl;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.analysis.dsl.selectors.VertexType;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.dataflowanalysis.dfd.dataflowdiagram.External;
import org.dataflowanalysis.dfd.dataflowdiagram.Process;
import org.dataflowanalysis.dfd.dataflowdiagram.Store;

public enum DFDVertexType implements VertexType {
    EXTERNAL,
    PROCESS,
    STORE;

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        if (!(vertex instanceof DFDVertex dfdVertex)) {
            return false;
        }
        switch (this) {
            case EXTERNAL -> {
                return (dfdVertex.getReferencedElement() instanceof External);
            }
            case PROCESS -> {
                return (dfdVertex.getReferencedElement() instanceof Process);
            }
            case STORE -> {
                return (dfdVertex.getReferencedElement() instanceof Store);
            }
            default -> {
                return false;
            }
        }
    }

    public static ParseResult<VertexType> fromString(StringView string) {
        if (string.startsWith("EXTERNAL")) {
            string.advance("EXTERNAL".length());
            return ParseResult.ok(DFDVertexType.EXTERNAL);
        }
        if (string.startsWith("PROCESS")) {
            string.advance("PROCESS".length());
            return ParseResult.ok(DFDVertexType.PROCESS);
        }
        if (string.startsWith("STORE")) {
            string.advance("STORE".length());
            return ParseResult.ok(DFDVertexType.STORE);
        }
        return ParseResult.error("Invalid dfd vertex type!");
    }
}
