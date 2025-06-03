package org.dataflowanalysis.examplemodels.results.pcm;

import java.util.Optional;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.CallReturnBehavior;
import org.dataflowanalysis.examplemodels.results.Identifier;

public class PCMIdentifier implements Identifier {
    private final String id;
    private final Optional<Boolean> calling;

    public PCMIdentifier(String id) {
        this.id = id;
        this.calling = Optional.empty();
    }

    public PCMIdentifier(String id, boolean calling) {
        this.id = id;
        this.calling = Optional.of(calling);
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        if (!(vertex instanceof AbstractPCMVertex<?> pcmVertex)) {
            return false;
        }
        if (this.calling.isPresent() && vertex instanceof CallReturnBehavior callReturnBehavior) {
            if (callReturnBehavior.isCalling() != this.calling.get()) {
                return false;
            }
        }
        return pcmVertex.getReferencedElement()
                .getId()
                .equals(this.id);
    }

    public static PCMIdentifier of(String id) {
        return new PCMIdentifier(id);
    }

    public static PCMIdentifier of(String id, boolean calling) {
        return new PCMIdentifier(id, calling);
    }

    @Override
    public String toString() {
        if (this.calling.isPresent()) {
            String callString = this.calling.get() ? "Calling" : "Returning";
            return String.format("%s PCM Vertex with ID %s", callString, this.id);
        } else {
            return String.format("PCM vertex with ID %s", this.id);
        }
    }
}
