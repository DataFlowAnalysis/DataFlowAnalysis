package org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.node;

import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.CharacteristicValue;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.entity.Entity;

public interface NodeCharacteristicsCalculator {
	public List<CharacteristicValue> getNodeCharacteristics(Entity node, Deque<AssemblyContext> context);
}
