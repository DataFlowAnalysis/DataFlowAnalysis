package org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.node;

import java.util.Deque;
import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.CharacteristicValue;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.entity.Entity;

public interface NodeCharacteristicsCalculator {
	/**
	 * Returns a list of applied node characteristics at the given node in the given assembly context
	 * @param node Node of whom the characteristics will be calculated
	 * @param context Assembly context applicable to the node
	 * @return Returns a list of node characteristics (i.e. characteristic type and literal) that are applied at that node
	 */
	public List<CharacteristicValue> getNodeCharacteristics(Entity node, Deque<AssemblyContext> context);
	
	/**
	 * Checks the given list of assignments for errors or inconsistencies
	 * @param assignments List of assignments that should be checked
	 */
	public void checkAssignments();
}
