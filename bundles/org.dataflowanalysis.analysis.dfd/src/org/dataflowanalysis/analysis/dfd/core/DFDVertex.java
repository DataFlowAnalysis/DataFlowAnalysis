package org.dataflowanalysis.analysis.dfd.core;

import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.eclipse.emf.ecore.EObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristicsCalculatorFactory;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.core.NodeCharacteristicsCalculator;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;

public class DFDVertex extends AbstractVertex<EObject>{
	
	String name;
	Node node;
	Map<Pin, DFDVertex> mapPinToPreviousVertex;
	Map<Pin, Flow> mapPinToInputFlow; 

	public DFDVertex(List<DataFlowVariable> dataFlowVariables,
			List<CharacteristicValue> nodeCharacteristics, String name, Node node, Map<Pin, DFDVertex> mapPinToPreviousVertex, Map<Pin, Flow> mapPinToInputFlow) {
		super(dataFlowVariables, new ArrayList<>(), nodeCharacteristics); //ausgewertet wird erst unten DataFlowVariable kann hier leer sein
		// TODO Auto-generated constructor stub
		this.name = name;
		this.node = node;
		this.mapPinToPreviousVertex = mapPinToPreviousVertex;
		this.mapPinToInputFlow = mapPinToInputFlow;
	}
	
	public DFDVertex(String name, Node node, Map<Pin, DFDVertex> mapPinToPreviousVertex, Map<Pin, Flow> mapPinToInputFlow) {
		super(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()); //ausgewertet wird erst unten DataFlowVariable kann hier leer sein
		// TODO Auto-generated constructor stub
		this.name = name;
		this.node = node;
		this.mapPinToPreviousVertex = mapPinToPreviousVertex;
		this.mapPinToInputFlow = mapPinToInputFlow;
	}

	@Override
	public AbstractVertex<EObject> evaluateDataFlow(List<DataFlowVariable> variables, NodeCharacteristicsCalculator nodeCharacteristicsCalculator, DataCharacteristicsCalculatorFactory dataCharacteristicsCalculatorFactory) {
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
    
    public DFDVertex clone() {
    	return new DFDVertex(new ArrayList<>(super.getAllDataFlowVariables()),new ArrayList<>(super.getAllNodeCharacteristics()), this.name, this.node, new HashMap<>(this.mapPinToPreviousVertex), new HashMap<>(this.mapPinToInputFlow));
    }


	public Map<Pin, DFDVertex> getMapPinToPreviousVertex() {
		return mapPinToPreviousVertex;
	}

	public Map<Pin, Flow> getMapPinToInputFlow() {
		return mapPinToInputFlow;
	}

	public String getName() {
		return name;
	}
}