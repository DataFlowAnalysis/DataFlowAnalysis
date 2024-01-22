package org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisData;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.AbstractActionSequenceElement;

import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.datadictionary.Pin;

public class DFDVertex extends AbstractActionSequenceElement<EObject>{
	
	String name;
	Node node;
	Map<Pin, DFDVertex> mapPinToPreviousElement;
	Map<Pin, Flow> mapPinToInputFlow; //Not created rn

	public DFDVertex(List<DataFlowVariable> dataFlowVariables,
			List<CharacteristicValue> nodeCharacteristics, String name, Node node, Map<Pin, DFDVertex> mapPinToPreviousElement, Map<Pin, Flow> mapPinToInputFlow) {
		super(dataFlowVariables, nodeCharacteristics); //ausgewertet wird erst unten DataFlowVariable kann hier leer sein
		// TODO Auto-generated constructor stub
		this.name = name;
		this.node = node;
		this.mapPinToPreviousElement = mapPinToPreviousElement;
		this.mapPinToInputFlow = mapPinToInputFlow;
	}
	
	public DFDVertex(String name, Node node, Map<Pin, DFDVertex> mapPinToPreviousElement, Map<Pin, Flow> mapPinToInputFlow) {
		super(new ArrayList<>(), new ArrayList<>()); //ausgewertet wird erst unten DataFlowVariable kann hier leer sein
		// TODO Auto-generated constructor stub
		this.name = name;
		this.node = node;
		this.mapPinToPreviousElement = mapPinToPreviousElement;
		this.mapPinToInputFlow = mapPinToInputFlow;
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
    
    public DFDVertex clone() {
    	return new DFDVertex(new ArrayList<>(super.getAllDataFlowVariables()),new ArrayList<>(super.getAllNodeCharacteristics()), this.name, this.node, new HashMap<>(this.mapPinToPreviousElement), new HashMap<>(this.mapPinToInputFlow));
    }


	public Map<Pin, DFDVertex> getMapPinToPreviousElement() {
		return mapPinToPreviousElement;
	}

	public Map<Pin, Flow> getMapPinToInputFlow() {
		return mapPinToInputFlow;
	}

	public String getName() {
		return name;
	}
	
    
}
