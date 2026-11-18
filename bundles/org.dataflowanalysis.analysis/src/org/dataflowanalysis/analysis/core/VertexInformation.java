package org.dataflowanalysis.analysis.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.dataflowanalysis.analysis.dsl.selectors.vertex.VertexType;

public class VertexInformation {
    private Set<CharacteristicValue> previousVertexCharacteristics;
    private Set<String> previousVertexNames;
    private Set<VertexType> previousVertexTypes;

    public VertexInformation(Set<CharacteristicValue> previousVertexCharacteristics, Set<String> previousVertexNames,
            Set<VertexType> previousVertexTypes) {
        this.previousVertexCharacteristics = previousVertexCharacteristics;
        this.previousVertexNames = previousVertexNames;
        this.previousVertexTypes = previousVertexTypes;
    }

    public static VertexInformation fromVertex(List<CharacteristicValue> vertexCharacteristics,
            AbstractVertex<?> vertex) {
        return new VertexInformation(new HashSet<>(vertexCharacteristics),
                new HashSet<>(Collections.singleton(vertex.getName())), new HashSet<>(vertex.getVertexTypes()));
    }

    public void extendInformation(AbstractVertex<?> vertex) {
        this.previousVertexCharacteristics.addAll(vertex.getPreviousVertexInformation()
                .getPreviousVertexCharacteristics());
        this.previousVertexNames.addAll(vertex.getPreviousVertexInformation()
                .getPreviousVertexNames());
        this.previousVertexTypes.addAll(vertex.getPreviousVertexInformation()
                .getPreviousVertexTypes());
    }

    public Set<CharacteristicValue> getPreviousVertexCharacteristics() {
        return previousVertexCharacteristics;
    }

    public Set<String> getPreviousVertexNames() {
        return previousVertexNames;
    }

    public Set<VertexType> getPreviousVertexTypes() {
        return previousVertexTypes;
    }
}
