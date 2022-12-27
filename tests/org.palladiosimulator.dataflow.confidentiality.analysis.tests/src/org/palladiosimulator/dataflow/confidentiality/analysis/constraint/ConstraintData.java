package org.palladiosimulator.dataflow.confidentiality.analysis.constraint;

import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;

public class ConstraintData {
	private final String nodeName;
	private final List<CharacteristicValue> nodeCharacteristics;
	private final List<DataFlowVariable> dataFlowVariables;
	
	public ConstraintData(String nodeName, List<CharacteristicValue> nodeCharacteristics, List<DataFlowVariable> dataFlowVariable) {
		this.nodeName = nodeName;
		this.nodeCharacteristics = nodeCharacteristics;
		this.dataFlowVariables = dataFlowVariable;
	}
	
	public String getNodeName() {
		return nodeName;
	}
	
	public List<CharacteristicValue> getNodeCharacteristics() {
		return nodeCharacteristics;
	}
	
	public List<DataFlowVariable> getDataFlowVariables() {
		return dataFlowVariables;
	}
}
