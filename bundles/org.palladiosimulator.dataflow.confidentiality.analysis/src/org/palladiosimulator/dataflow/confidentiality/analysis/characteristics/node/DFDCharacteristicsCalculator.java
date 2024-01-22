package org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.function.Predicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DFDCharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd.DFDFlowGraph;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd.DFDVertex;

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
	 * Create DataFlowVariables for a DFDActionSequence element
	 * @param dfdActionSequence element
	 * @return DFDActionSequence element annotated with DataFlowVariables
	 */
	public static DFDFlowGraph fillDataFlowVariables (DFDFlowGraph dfdActionSequence) {
		DFDFlowGraph test = DFDFlowGraph.createFromEndElement(evaluateElement(dfdActionSequence.getLastElement()));
		return test;
	}
	
	/**
	 * Evaluates an element and all previous elements
	 * @param element
	 * @return The evaluated element
	 */
	private static DFDVertex evaluateElement(DFDVertex element) {
		Node node = element.getNode();
		
		Map<Pin, DFDVertex> previousElements = element.getMapPinToPreviousElement();
		List<DataFlowVariable> dataFlowVariables = new ArrayList<DataFlowVariable>(element.getAllDataFlowVariables());
		List<CharacteristicValue> nodeCharacteristics = new ArrayList<CharacteristicValue>();
		
		for (var label : node.getProperties()) {
			nodeCharacteristics.add(new DFDCharacteristicValue((LabelType) label.eContainer(), label));
		}
		
		
		if (previousElements.size() == 0) {
			return new DFDVertex(dataFlowVariables, nodeCharacteristics, element.getName(), element.getNode(), element.getMapPinToPreviousElement(), element.getMapPinToInputFlow());
					
		}
		
		for (var key : previousElements.keySet()) {
			previousElements.replace(key, evaluateElement(previousElements.get(key)));
		}
		
		List<Pin> listOfAllOutputPinsWithIncomingFlowsIntoElement = new ArrayList<>();
		for (var flow : element.getMapPinToInputFlow().values()) {
			listOfAllOutputPinsWithIncomingFlowsIntoElement.add(flow.getSourcePin());
		}
		
		
		
		for (var prevElement : previousElements.values()) {
			List<Label> outputLabel = new ArrayList<>();
			List<Label> prevElementLabels = new ArrayList<>();
			for (DataFlowVariable dfv : prevElement.getAllDataFlowVariables()) {
				for (CharacteristicValue cv : dfv.getAllCharacteristics()) {
					prevElementLabels.add(((DFDCharacteristicValue)cv).getLabel());
				}
			}
			for (var assignment : prevElement.getNode().getBehaviour().getAssignment()) {
				if(listOfAllOutputPinsWithIncomingFlowsIntoElement.contains(assignment.getOutputPin())) {
					if (assignment instanceof ForwardingAssignment) {
						outputLabel.addAll(prevElementLabels);
					} else if(evaluateTerm(((Assignment)assignment).getTerm(), prevElementLabels)) {
						outputLabel.addAll(((Assignment)assignment).getOutputLabels());						
					} else if(!evaluateTerm(((Assignment)assignment).getTerm(), prevElementLabels)) {
						outputLabel.removeAll(((Assignment)assignment).getOutputLabels());						
					}
				}
				
			}
			
			List<CharacteristicValue> characteristics = new ArrayList<>();
			
			for (Label label : outputLabel) {
				characteristics.add(new DFDCharacteristicValue((LabelType) label.eContainer(), label));
			}
			
			characteristics = characteristics.stream().filter(distinctByKey(CharacteristicValue::getValueId)).collect(Collectors.toList());
			dataFlowVariables.add(new DataFlowVariable(prevElement.getName(), characteristics));
		}
		
		return new DFDVertex(dataFlowVariables, nodeCharacteristics, element.getName(), element.getNode(), element.getMapPinToPreviousElement(), element.getMapPinToInputFlow());
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
