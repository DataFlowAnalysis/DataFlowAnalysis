package org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.node;

import java.util.Deque;
import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.CharacteristicValue;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.entity.Entity;

// TODO: This Interface is created or named poorly, as it has hard dependencies to Palladio
// Should be renamed or fully deleted when builder pattern gets a HARD rework
public interface NodeCharacteristicsCalculator {
	public List<CharacteristicValue> getNodeCharacteristics(Entity node, Deque<AssemblyContext> context);
}
