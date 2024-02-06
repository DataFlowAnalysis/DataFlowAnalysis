package org.dataflowanalysis.analysis.dfd.core;

import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.eclipse.emf.ecore.EObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.HashMap;

import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.flowgraph.AbstractVertex;
import org.dataflowanalysis.dfd.datadictionary.AND;
import org.dataflowanalysis.dfd.datadictionary.AbstractAssignment;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.BinaryOperator;
import org.dataflowanalysis.dfd.datadictionary.ForwardingAssignment;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelReference;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.NOT;
import org.dataflowanalysis.dfd.datadictionary.OR;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.datadictionary.TRUE;
import org.dataflowanalysis.dfd.datadictionary.Term;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;

public class DFDVertex extends AbstractVertex<EObject>{
	
	String name;
	Node node;
	Map<Pin, DFDVertex> mapPinToPreviousVertex;
	Map<Pin, Flow> mapPinToInputFlow; 

	
	
	public DFDVertex(String name, Node node, Map<Pin, DFDVertex> mapPinToPreviousVertex, Map<Pin, Flow> mapPinToInputFlow) {
		super(node, new ArrayList<>(mapPinToPreviousVertex.values())); //ausgewertet wird erst unten DataFlowVariable kann hier leer sein
		// TODO Auto-generated constructor stub
		this.name = name;
		this.node = node;
		this.mapPinToPreviousVertex = mapPinToPreviousVertex;
		this.mapPinToInputFlow = mapPinToInputFlow;
	}

	@Override
	public void evaluateDataFlow() {
		if(super.isEvaluated()) return;
		
		Node node = this.getNode();
		
		Map<Pin, DFDVertex> previousVertices = this.getMapPinToPreviousVertex();		
		
		List<DataFlowVariable> dataFlowVariables = new ArrayList<DataFlowVariable>();
		List<DataFlowVariable> outgoingDataFlowVariables = new ArrayList<DataFlowVariable>();
		List<CharacteristicValue> nodeCharacteristics = new ArrayList<CharacteristicValue>();
		
		Map<Pin, List<Label>> mapOutputPinToOutgoingLabels = new HashMap<>();		
		Map<Pin, List<Label>> mapInputPinsToIncomingLabels = new HashMap<>();
		
		//Adding characteristics
		for (var label : node.getProperties()) {
			nodeCharacteristics.add(new DFDCharacteristicValue((LabelType) label.eContainer(), label));
		}
		
		//Evaluate Previous Elements
		for (var key : previousVertices.keySet()) {
			previousVertices.get(key).evaluateDataFlow();
		}
		
		//Create Map with all incoming Labels per pin
		for (var pin : this.getMapPinToInputFlow().keySet()) {
			for (var prevVertex : this.getMapPinToPreviousVertex().values()) {
				for (var dfv : prevVertex.getAllOutgoingDataFlowVariables()) {
					if (dfv.getVariableName().equals(this.getMapPinToInputFlow().get(pin).getSourcePin().getId())) {
						mapInputPinsToIncomingLabels.putIfAbsent(pin, new ArrayList<>());
						for (var cv : dfv.getAllCharacteristics()) {
							mapInputPinsToIncomingLabels.get(pin).add(((DFDCharacteristicValue)cv).getLabel());
						}
					}
				}
			}			
		}
		
		//Create data flow variables from map
		for (var pin : mapInputPinsToIncomingLabels.keySet()) {
			List<CharacteristicValue> characteristics = new ArrayList<>();
			for (var label : mapInputPinsToIncomingLabels.get(pin)) {
				characteristics.add(new DFDCharacteristicValue((LabelType) label.eContainer(), label));
			}
			characteristics = characteristics.stream().filter(distinctByKey(CharacteristicValue::getValueId)).collect(Collectors.toList());
			dataFlowVariables.add(new DataFlowVariable(pin.getId(), characteristics));
		}
		
		//Create Map with all Outgoing Labels per pin
		for (var assignment : node.getBehaviour().getAssignment()) {
			List<Label> incomingLabels = combineLabelsOnAllInputPins(assignment, mapInputPinsToIncomingLabels);
			mapOutputPinToOutgoingLabels.putIfAbsent(assignment.getOutputPin(), new ArrayList<>());
			if (assignment instanceof ForwardingAssignment) {
				mapOutputPinToOutgoingLabels.get(assignment.getOutputPin()).addAll(incomingLabels);
			} else if(evaluateTerm(((Assignment)assignment).getTerm(), incomingLabels)) {
				mapOutputPinToOutgoingLabels.get(assignment.getOutputPin()).addAll(((Assignment)assignment).getOutputLabels());						
			} else if(!evaluateTerm(((Assignment)assignment).getTerm(), incomingLabels)) {
				mapOutputPinToOutgoingLabels.get(assignment.getOutputPin()).removeAll(((Assignment)assignment).getOutputLabels());						
			}
		}
		
		//Create outgoing dfvs from map
		for (var pin : mapOutputPinToOutgoingLabels.keySet()) {
			List<CharacteristicValue> characteristics = new ArrayList<>();
			for (var label : mapOutputPinToOutgoingLabels.get(pin)) {
				characteristics.add(new DFDCharacteristicValue((LabelType) label.eContainer(), label));
			}
			characteristics = characteristics.stream().filter(distinctByKey(CharacteristicValue::getValueId)).collect(Collectors.toList());
			outgoingDataFlowVariables.add(new DataFlowVariable(pin.getId(), characteristics));
		}
		
		this.setPropagationResult(dataFlowVariables, outgoingDataFlowVariables, nodeCharacteristics);
		
	}
	
	private static List<Label> combineLabelsOnAllInputPins(AbstractAssignment assignment, Map<Pin, List<Label>> mapInputPinsToIncomingLabels) {
		List<Label> allLabel = new ArrayList<>();
		for (var inputPin : assignment.getInputPins()) {
			allLabel.addAll(mapInputPinsToIncomingLabels.getOrDefault(inputPin, new ArrayList<>()));
		}
		return allLabel;
	}
	
	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
	    KeySetView<Object, Boolean> seen = ConcurrentHashMap.newKeySet();
	    return t -> seen.add(keyExtractor.apply(t));
	}
	
	/**
	 * Evaluate Assignment Term with List of Incoming Labels
	 * @param term Term to be evaluated
	 * @param inputLabel Incoming Label
	 * @return Evaluation
	 */
	private static boolean evaluateTerm(Term term, List<Label> inputLabel) {
		if (term instanceof TRUE) {
			return true;
		}
		else if (term instanceof NOT) {
			NOT notTerm = (NOT) term;
			return !evaluateTerm(notTerm.getNegatedTerm(), inputLabel);
		}
		else if (term instanceof LabelReference) {
			if (inputLabel.contains(((LabelReference) term).getLabel())) return true;
			else return false;
		}
		else if (term instanceof BinaryOperator) {
			BinaryOperator binaryTerm = (BinaryOperator) term;
			if (binaryTerm instanceof AND) {
				return evaluateTerm(binaryTerm.getTerms().get(0), inputLabel) && evaluateTerm(binaryTerm.getTerms().get(1), inputLabel);
			}
			else if(binaryTerm instanceof OR) {
				return evaluateTerm(binaryTerm.getTerms().get(0), inputLabel) || evaluateTerm(binaryTerm.getTerms().get(1), inputLabel);
			}
		}
		
		return false;
	}
	
	public void unify(Set<DFDVertex> vertices) {
		for (var key : this.getMapPinToPreviousVertex().keySet()) {
			for (var vertex : vertices) {
				if (vertex.isEqual(this.getMapPinToPreviousVertex().get(key))) {
					this.getMapPinToPreviousVertex().put(key, vertex);
				}				
			}
			vertices.add(this.getMapPinToPreviousVertex().get(key));
		}
		for (var vertex : this.getMapPinToPreviousVertex().values()) {
			vertex.unify(vertices);
		}
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<AbstractVertex<?>> getPreviousElements() {
		return new ArrayList<>(this.mapPinToPreviousVertex.values());
	}

    public void setNode(Node node) {
        this.node = node;
    }
    
    public boolean isEqual(DFDVertex vertex) {
    	if (!this.node.equals(vertex.getNode())) return false;
    	if (!this.name.equals(vertex.getName())) return false;
    	for (var key : this.getMapPinToPreviousVertex().keySet()) {
    		if (!this.getMapPinToPreviousVertex().get(key).isEqual(vertex.getMapPinToPreviousVertex().get(key))) return false;
    	}
    	return true;
    }    
    
    public Node getNode() {
		return node;
	}

	public DFDVertex clone() {
		Map<Pin, DFDVertex> newMapPinToPreviousVertex= new HashMap<>();
		for (var key : this.mapPinToPreviousVertex.keySet()) {
			DFDVertex previousClone = this.mapPinToPreviousVertex.get(key).clone();
			newMapPinToPreviousVertex.put(key, previousClone);
		}
		DFDVertex clone = new DFDVertex(this.name, this.node, newMapPinToPreviousVertex, new HashMap<>(this.mapPinToInputFlow));
    	return clone;
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