package org.dataflowanalysis.examplemodels.results.dfd;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.examplemodels.results.Identifier;

public class DFDIdentifier implements Identifier {
    private final String id;

    public DFDIdentifier(String id) {
        this.id = id;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        if (!(vertex instanceof DFDVertex dfdVertex)) {
            return false;
        }
        return dfdVertex.getReferencedElement()
                .getId()
                .equals(this.id);
    }

    public static DFDIdentifier of(String id) {
        return new DFDIdentifier(id);
    }

    @Override
    public String toString() {
        return String.format("DFD vertex with ID %s", this.id);
    }
}
