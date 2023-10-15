package org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisData;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.AbstractActionSequenceElement;

import mdpa.dfd.dataflowdiagram.Node;
import mdpa.dfd.dataflowdiagram.Flow;

public class DFDActionSequenceElement extends AbstractActionSequenceElement<EObject>{
	
	String name;
	Node node;
	Node previousNode;
	Flow flow;

	public DFDActionSequenceElement(List<DataFlowVariable> dataFlowVariables,
			List<CharacteristicValue> nodeCharacteristics, String name, Node node, Node previousNode, Flow flow) {
		super(dataFlowVariables, nodeCharacteristics); //ausgewertet wird erst unten DataFlowVariable kann hier leer sein
		// TODO Auto-generated constructor stub
		this.name = name;
		this.node = node;
		this.previousNode = previousNode;
		this.flow = flow;
	}

	@Override
	public AbstractActionSequenceElement<EObject> evaluateDataFlow(List variables, AnalysisData analysisData) {
		// TODO funktional leer lassen & Exception fürs Debugging
		return null;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Node getPreviousNode() {
        return previousNode;
    }

    public void setPreviousNode(Node previousNode) {
        this.previousNode = previousNode;
    }

	public Flow getFlow() {
		return flow;
	}
	
    
}
