package org.palladiosimulator.dataflow.confidentiality.analysis.constraint;

import java.util.List;
import java.util.Map;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.AbstractPCMActionSequenceElement;

import de.uka.ipd.sdq.identifier.Identifier;

public class ConstraintData {
	private final String nodeID;
	private final List<CharacteristicValueData> nodeCharacteristics;
	private final Map<String, List<CharacteristicValueData>> dataFlowVariables;
	
	public ConstraintData(String nodeID, List<CharacteristicValueData> nodeCharacteristics, Map<String, List<CharacteristicValueData>> dataFlowVariable) {
		this.nodeID = nodeID;
		this.nodeCharacteristics = nodeCharacteristics;
		this.dataFlowVariables = dataFlowVariable;
	}
	
	public boolean matches(AbstractActionSequenceElement<?> element) {
		if (!(element instanceof AbstractPCMActionSequenceElement<?>)) {
			return false;
		}
		AbstractPCMActionSequenceElement<?> sequenceElement = (AbstractPCMActionSequenceElement<?>) element;
		Identifier pcmElement = (Identifier) sequenceElement.getElement();
		return this.nodeID.equals(pcmElement.getId());
	}
	
	public boolean hasNodeCharacteristic(CharacteristicValue actualCharacteristicValue) {
		return true;
	}
	
	public boolean hasDataFlowVariable(DataFlowVariable actualDataFlowVariable) {
		return true;
	}
	
	public int nodeCharacteristicsAmount() {
		return this.nodeCharacteristics.size();
	}
	
	public int dataFlowVariablesAmount() {
		return this.dataFlowVariables.size();
	}
}
