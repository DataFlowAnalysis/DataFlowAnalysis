package org.dataflowanalysis.analysis.dfd.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.function.Predicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataFlowVariable;

import org.dataflowanalysis.dfd.datadictionary.AND;
import org.dataflowanalysis.dfd.datadictionary.AbstractAssignment;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.BinaryOperator;
import org.dataflowanalysis.dfd.datadictionary.ForwardingAssignment;
import org.dataflowanalysis.dfd.datadictionary.LabelReference;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.NOT;
import org.dataflowanalysis.dfd.datadictionary.OR;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.datadictionary.TRUE;
import org.dataflowanalysis.dfd.datadictionary.Term;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;

public class DFDCharacteristicsCalculator {
	
	/**
	 * Create DataFlowVariables for a DFDFlowGraph element
	 * @param dfdFlowGraph element
	 * @return DFDFlowGraph element annotated with DataFlowVariables
	 */
	public static DFDPartialFlowGraph fillDataFlowVariables (DFDPartialFlowGraph dfdFlowGraph) {
		DFDPartialFlowGraph test = DFDPartialFlowGraph.createFromEndVertex(evaluateVertex(dfdFlowGraph.getLastVertex()));
		return test;
	}
	
	/**
	 * Evaluates an DFDVertex and all previous DFDVertices
	 * @param vertex
	 * @return The evaluated DFDVertex
	 */
	private static DFDVertex evaluateVertex(DFDVertex vertex) {
		Node node = vertex.getNode();
		
		Map<Pin, DFDVertex> previousVertices = vertex.getMapPinToPreviousVertex();		
		
		List<DataFlowVariable> dataFlowVariables = new ArrayList<DataFlowVariable>(vertex.getAllDataFlowVariables());
		List<DataFlowVariable> outgoingDataFlowVariables = new ArrayList<DataFlowVariable>(vertex.getAllOutgoingDataFlowVariables());
		List<CharacteristicValue> nodeCharacteristics = new ArrayList<CharacteristicValue>();
		
		Map<Pin, List<Label>> mapOutputPinToOutgoingLabels = new HashMap<>();		
		Map<Pin, List<Label>> mapInputPinsToIncomingLabels = new HashMap<>();
		
		//Adding characteristics
		for (var label : node.getProperties()) {
			nodeCharacteristics.add(new DFDCharacteristicValue((LabelType) label.eContainer(), label));
		}
		
		//Evaluate Previous Elements
		for (var key : previousVertices.keySet()) {
			previousVertices.replace(key, evaluateVertex(previousVertices.get(key)));
		}
		
		//Create Map with all incoming Labels per pin
		for (var pin : vertex.getMapPinToInputFlow().keySet()) {
			for (var prevVertex : vertex.getMapPinToPreviousVertex().values()) {
				for (var dfv : prevVertex.getAllOutgoingDataFlowVariables()) {
					if (dfv.getVariableName().equals(vertex.getMapPinToInputFlow().get(pin).getSourcePin().getId())) {
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
		
		return new DFDVertex(dataFlowVariables, outgoingDataFlowVariables, nodeCharacteristics, vertex.getName(), vertex.getNode(), vertex.getMapPinToPreviousVertex(), vertex.getMapPinToInputFlow());
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

}