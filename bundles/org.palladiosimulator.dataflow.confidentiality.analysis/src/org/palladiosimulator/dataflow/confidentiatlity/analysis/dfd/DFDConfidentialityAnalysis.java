package org.palladiosimulator.dataflow.confidentiatlity.analysis.dfd;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.log4j.Level;
import org.palladiosimulator.dataflow.confidentiality.analysis.DataFlowConfidentialityAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.AbstractDFDActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.ActionSequence;

import mdpa.dfd.diagram.dataflowdiagrammodel.DataFlowDiagram;
import mdpa.dfd.diagram.dataflowdiagrammodel.Node;
import mdpa.dfd.behaviour.behaviourmodel.Behaviour;
import mdpa.dfd.behaviour.behaviourmodel.Assignment;

public class DFDConfidentialityAnalysis implements DataFlowConfidentialityAnalysis {
	private String pathToModel;
	DataFlowDiagram dfd;

	@Override
	public boolean initializeAnalysis() {
		this.dfd = DFDLoader.loadDFDModel(this.pathToModel);
		return true;
	}

	@Override
	public List<ActionSequence> findAllSequences() {
		
		return null;
	}
	
	private AbstractDFDActionSequenceElement mapNodeToAbstractDFDActionSequenceElement(Node node) {
		var dataFlowVariables = new ArrayList<DataFlowVariable>();
		var behaviour = node.getBehaviour();
		for(var inputPin:  behaviour.getIn()) {
			dataFlowVariables.add(new DataFlowVariable(inputPin.getName(), //TODO: Assignment des vorherigen Knoten))
		}
		
		var nodeCharacteristics = new ArrayList<CharacteristicValue>();
		for(Assignment assignment: behaviour.getAssignment()) {
			nodeCharacteristics.add(new CharacteristicValue(/*TODO: LabelType)*/, new Literal() assignment.getLhsLabel());
			//Todo: LabelType auf das Enum von CharacteristicsType mappen
		}
		
		return null;
		
	}
	
	private 

	@Override
	public List<ActionSequence> evaluateDataFlows(List<ActionSequence> sequences) {
		
		
		var actionSequences = new ArrayList<ActionSequence>();
		for(var node: dfd.getNodes()) {
			actionSequences.add();
		}
		
		return null;
	}

	@Override
	public List<AbstractActionSequenceElement<?>> queryDataFlow(ActionSequence sequence,
			Predicate<? super AbstractActionSequenceElement<?>> condition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLoggerLevel(Level level) {
		// TODO Auto-generated method stub
		
	}
	
	public String getPathToModel() {
		return this.pathToModel;
	}
	
	public void setPathToModel(String pathToModel) {
		this.pathToModel = pathToModel;
	}

}
