package org.dataflowanalysis.analysis.dfd.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.function.Predicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataFlowVariable;

import org.dataflowanalysis.dfd.datadictionary.AND;
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
		List<CharacteristicValue> nodeCharacteristics = new ArrayList<CharacteristicValue>();
		
		for (var label : node.getProperties()) {
			nodeCharacteristics.add(new DFDCharacteristicValue((LabelType) label.eContainer(), label));
		}
		
		
		if (previousVertices.size() == 0) {
			return new DFDVertex(dataFlowVariables, nodeCharacteristics, vertex.getName(), vertex.getNode(), vertex.getMapPinToPreviousVertex(), vertex.getMapPinToInputFlow());
					
		}
		
		for (var key : previousVertices.keySet()) {
			previousVertices.replace(key, evaluateVertex(previousVertices.get(key)));
		}
		
		List<Pin> listOfAllOutputPinsWithIncomingFlowsIntoVertex = new ArrayList<>();
		for (var flow : vertex.getMapPinToInputFlow().values()) {
			listOfAllOutputPinsWithIncomingFlowsIntoVertex.add(flow.getSourcePin());
		}
		
		
		
		for (var prevVertex : previousVertices.values()) {
			List<Label> outputLabel = new ArrayList<>();
			List<Label> prevVertexLabels = new ArrayList<>();
			for (DataFlowVariable dfv : prevVertex.getAllDataFlowVariables()) {
				for (CharacteristicValue cv : dfv.getAllCharacteristics()) {
					prevVertexLabels.add(((DFDCharacteristicValue)cv).getLabel());
				}
			}
			for (var assignment : prevVertex.getNode().getBehaviour().getAssignment()) {
				if(listOfAllOutputPinsWithIncomingFlowsIntoVertex.contains(assignment.getOutputPin())) {
					if (assignment instanceof ForwardingAssignment) {
						outputLabel.addAll(prevVertexLabels);
					} else if(evaluateTerm(((Assignment)assignment).getTerm(), prevVertexLabels)) {
						outputLabel.addAll(((Assignment)assignment).getOutputLabels());						
					} else if(!evaluateTerm(((Assignment)assignment).getTerm(), prevVertexLabels)) {
						outputLabel.removeAll(((Assignment)assignment).getOutputLabels());						
					}
				}
				
			}
			
			List<CharacteristicValue> characteristics = new ArrayList<>();
			
			for (Label label : outputLabel) {
				characteristics.add(new DFDCharacteristicValue((LabelType) label.eContainer(), label));
			}
			
			characteristics = characteristics.stream().filter(distinctByKey(CharacteristicValue::getValueId)).collect(Collectors.toList());
			dataFlowVariables.add(new DataFlowVariable(prevVertex.getName(), characteristics));
		}
		
		return new DFDVertex(dataFlowVariables, nodeCharacteristics, vertex.getName(), vertex.getNode(), vertex.getMapPinToPreviousVertex(), vertex.getMapPinToInputFlow());
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