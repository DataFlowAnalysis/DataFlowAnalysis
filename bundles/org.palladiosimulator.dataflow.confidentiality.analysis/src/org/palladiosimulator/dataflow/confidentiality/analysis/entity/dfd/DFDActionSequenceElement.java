package org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd;

import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisData;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.AbstractDFDActionSequenceElement;

public class DFDActionSequenceElement extends AbstractDFDActionSequenceElement{

	public DFDActionSequenceElement(List<DataFlowVariable> dataFlowVariables,
			List<CharacteristicValue> nodeCharacteristics, String name) {
		super(dataFlowVariables, nodeCharacteristics, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public AbstractActionSequenceElement evaluateDataFlow(List variables, AnalysisData analysisData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

}
