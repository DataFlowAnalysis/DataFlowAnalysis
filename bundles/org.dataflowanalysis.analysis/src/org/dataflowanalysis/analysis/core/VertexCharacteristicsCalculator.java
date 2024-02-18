package org.dataflowanalysis.analysis.core;

import java.util.Deque;
import java.util.List;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.entity.Entity;

public interface VertexCharacteristicsCalculator {
    /**
     * Returns a list of applied vertex characteristics at the given node in the given assembly context
     * @param vertex Vertex of whom the characteristics will be calculated
     * @param context Assembly context applicable to the node
     * @return Returns a list of vertex characteristics (i.e. characteristic type and literal) that are applied at that
     * vertex
     */
    public List<CharacteristicValue> getNodeCharacteristics(Entity vertex, Deque<AssemblyContext> context);

    /**
     * Checks the given list of assignments for errors or inconsistencies
     * @param assignments List of assignments that should be checked
     */
    public void checkAssignments();
}
