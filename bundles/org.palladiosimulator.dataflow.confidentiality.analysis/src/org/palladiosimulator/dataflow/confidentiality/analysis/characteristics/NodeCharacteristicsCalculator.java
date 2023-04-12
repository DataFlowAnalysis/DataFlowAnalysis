package org.palladiosimulator.dataflow.confidentiality.analysis.characteristics;

import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.palladiosimulator.dataflow.confidentiality.analysis.entity.CharacteristicValue;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;

public interface NodeCharacteristicsCalculator {
	public List<CharacteristicValue> getNodeCharacteristics(Optional<Deque<AssemblyContext>> context);
}
