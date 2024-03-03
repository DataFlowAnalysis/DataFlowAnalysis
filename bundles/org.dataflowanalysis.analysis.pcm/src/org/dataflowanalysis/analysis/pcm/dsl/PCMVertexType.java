package org.dataflowanalysis.analysis.pcm.dsl;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.selectors.VertexType;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.UserPCMVertex;

public enum PCMVertexType implements VertexType {
    // TODO: Add more enum variants
    USER;

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        if (!(vertex instanceof AbstractPCMVertex<?> pcmVertex)) {
            return false;
        }
        switch (this) {
            case USER -> {
                return (pcmVertex instanceof UserPCMVertex<?>);
            }
            default -> {
                return false;
            }
        }
    }
}
