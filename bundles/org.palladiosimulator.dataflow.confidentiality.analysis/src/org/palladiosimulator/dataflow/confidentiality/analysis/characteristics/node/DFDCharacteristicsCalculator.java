package org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.node;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.function.Predicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DFDCharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd.DFDActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd.DFDActionSequenceElement;

import mdpa.dfd.datadictionary.AND;
import mdpa.dfd.datadictionary.Assignment;
import mdpa.dfd.datadictionary.Label;
import mdpa.dfd.datadictionary.BinaryOperator;
import mdpa.dfd.datadictionary.ForwardingAssignment;
import mdpa.dfd.datadictionary.LabelReference;
import mdpa.dfd.datadictionary.LabelType;
import mdpa.dfd.datadictionary.NOT;
import mdpa.dfd.datadictionary.OR;
import mdpa.dfd.datadictionary.Pin;
import mdpa.dfd.datadictionary.TRUE;
import mdpa.dfd.datadictionary.Term;
import mdpa.dfd.dataflowdiagram.Node;

public class DFDCharacteristicsCalculator {
	
	
	/*public static DFDActionSequenceElement fillDataFlowVariables (DFDActionSequenceElement dfdActionSequenceElement) {
		List<DataFlowVariable> dataFlowVariables = new ArrayList<DataFlowVariable>(dfdActionSequenceElement.getAllDataFlowVariables());
		for(var inputPin:  dfdActionSequenceElement.getNode().getBehaviour().getIn()) {
			dataFlowVariables.add(new DataFlowVariable(inputPin.getEntityName(), evaluateAssignments(dfdActionSequenceElement.getPreviousNode(), inputPin)));
		}
		
		return dfdActionSequenceElement;
	}*/
	
	public static DFDActionSequence fillDataFlowVariables (DFDActionSequence dfdActionSequence) {
		DFDActionSequenceElement prev = null; 
		for (var dfdActionSequenceElement : dfdActionSequence.getElements()) {
			List<DataFlowVariable> dataFlowVariables = new ArrayList<DataFlowVariable>(dfdActionSequenceElement.getAllDataFlowVariables());
			dataFlowVariables.add(new DataFlowVariable(((DFDActionSequenceElement)dfdActionSequenceElement).getNode().getEntityName(), evaluateAssignments((DFDActionSequenceElement)dfdActionSequenceElement, prev)));
		}
		
		return dfdActionSequence;
	}
	
	private static List<CharacteristicValue> evaluateAssignments(DFDActionSequenceElement element, DFDActionSequenceElement previousElement) {
		List<Label> allPrevNodeLabels = new ArrayList<>();
		previousElement.getAllDataFlowVariables().stream().forEach(dfv -> {
			dfv.characteristics().stream().forEach(c -> {
				DFDCharacteristicValue cv = (DFDCharacteristicValue) c;
				allPrevNodeLabels.add(cv.label());
			});
		});;
		
		List<CharacteristicValue> characteristics = new ArrayList<>();
		
		
		for (var assignment : element.getPreviousNode().getBehaviour().getAssignment()) {
			if(assignment.getOutputPin().equals(element.getFlow().getSourcePin()) && element.getFlow().getDestinationNode().equals(element.getNode())) {
				if (assignment instanceof ForwardingAssignment) {
					for (var label : allPrevNodeLabels) {
						characteristics.add(new DFDCharacteristicValue((LabelType) label.eContainer(), label));
					}
				}
				
				if(evaluateTerm(((Assignment)assignment).getTerm(), allPrevNodeLabels)) {
					for (var label : ((Assignment)assignment).getOutputLabels()) {
						characteristics.add(new DFDCharacteristicValue((LabelType) label.eContainer(), label)); //TODO:soll nicht doppelt reinkommen
					}
				}
			}
			
		}
		characteristics = characteristics.stream().filter(distinctByKey(CharacteristicValue::getValueId)).collect(Collectors.toList());
		return characteristics;
	}
	
	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
	    KeySetView<Object, Boolean> seen = ConcurrentHashMap.newKeySet();
	    return t -> seen.add(keyExtractor.apply(t));
	}
	
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
