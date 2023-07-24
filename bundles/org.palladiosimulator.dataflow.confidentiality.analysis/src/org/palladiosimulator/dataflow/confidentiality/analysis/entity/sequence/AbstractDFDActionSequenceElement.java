package org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisData;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DataFlowVariable;
import mdpa.dfd.behaviour.behaviourmodel.Behaviour;
import mdpa.dfd.behaviour.behaviourmodel.Pin;
import mdpa.dfd.diagram.dataflowdiagrammodel.Node;

public abstract class AbstractDFDActionSequenceElement extends AbstractActionSequenceElement {

	public AbstractDFDActionSequenceElement(List<DataFlowVariable> dataFlowVariables,
			List<CharacteristicValue> nodeCharacteristics) {
		super(dataFlowVariables, nodeCharacteristics);
	}
}
