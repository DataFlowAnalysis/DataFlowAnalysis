package org.dataflowanalysis.analysis.pcm.dsl;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.selectors.VertexType;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.CallReturnBehavior;
import org.dataflowanalysis.analysis.pcm.core.seff.SEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.UserPCMVertex;

public enum PCMVertexType implements VertexType {
    USER,
    SEFF,
    CALLING,
    RETURNING;

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        if (!(vertex instanceof AbstractPCMVertex<?> pcmVertex)) {
            return false;
        }
        switch (this) {
            case USER -> {
                return (pcmVertex instanceof UserPCMVertex<?>);
            }
            case SEFF -> {
                return (pcmVertex instanceof SEFFPCMVertex<?>);
            }
            case CALLING -> {
                return (pcmVertex instanceof CallReturnBehavior) && ((CallReturnBehavior) pcmVertex).isCalling();
            }
            case RETURNING -> {
                return (pcmVertex instanceof CallReturnBehavior) && ((CallReturnBehavior) pcmVertex).isReturning();
            }
            default -> {
                return false;
            }
        }
    }
}
