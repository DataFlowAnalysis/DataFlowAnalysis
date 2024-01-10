package org.dataflowanalysis.analysis.dfd.core;

import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.eclipse.emf.ecore.EObject;

import java.util.ArrayList;
import java.util.List;

import org.dataflowanalysis.analysis.AnalysisData;
import org.dataflowanalysis.analysis.core.AbstractActionSequenceElement;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;

public class DFDActionSequenceElement extends AbstractActionSequenceElement<EObject>{
	
	private String name;
	private Node node;
	private Node previousNode;
	private Flow flow;

	public DFDActionSequenceElement(List<DataFlowVariable> dataFlowVariables,
			List<CharacteristicValue> nodeCharacteristics, String name, Node node, Node previousNode, Flow flow) {
		super(dataFlowVariables, new ArrayList<>(), nodeCharacteristics); //ausgewertet wird erst unten DataFlowVariable kann hier leer sein
		this.name = name;
		this.node = node;
		this.previousNode = previousNode;
		this.flow = flow;
	}

	@Override
	public AbstractActionSequenceElement<EObject> evaluateDataFlow(List<DataFlowVariable> variables, AnalysisData analysisData) {
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

	public String getName() {
		return name;
	}    
}