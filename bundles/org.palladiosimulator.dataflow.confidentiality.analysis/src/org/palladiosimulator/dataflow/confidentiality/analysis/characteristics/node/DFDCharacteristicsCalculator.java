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
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.AbstractActionSequenceElement;

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
import mdpa.dfd.dataflowdiagram.Flow;

public class DFDCharacteristicsCalculator {
	
	/**
	 * Create DataFlowVariables for a DFDActionSequence element
	 * @param dfdActionSequence element
	 * @return DFDActionSequence element annotated with DataFlowVariables
	 */
	public static DFDActionSequence fillDataFlowVariables (DFDActionSequence dfdActionSequence) {
		List<AbstractActionSequenceElement<?>> actionSequence = new ArrayList<AbstractActionSequenceElement<?>>();
		if (dfdActionSequence.getElements().size() < 2) return dfdActionSequence;
		List<DataFlowVariable> previousVariables = new ArrayList<>();
		for (var abstractElement : dfdActionSequence.getElements()) {
			DFDActionSequenceElement element = (DFDActionSequenceElement) abstractElement;
			List<DataFlowVariable> dataFlowVariables = new ArrayList<DataFlowVariable>(element.getAllDataFlowVariables());
			
			dataFlowVariables.add(new DataFlowVariable(element.getNode().getEntityName(), evaluateAssignments(element, previousVariables)));
			DFDActionSequenceElement newElement = new DFDActionSequenceElement(dataFlowVariables, element.getAllNodeCharacteristics(), element.getName(), element.getNode(), element.getPreviousNode(), element.getFlow());
			actionSequence.add(newElement);
			previousVariables = dataFlowVariables;
		}
		
		return new DFDActionSequence(actionSequence);
	}
	
	/**
	 * Evaluate all Assignments on Node DFDActionSequenceElement
	 * @param element DFDActionSequenceElement to be evaluated
	 * @param previousVariables All incoming Data Flow Variables
	 * @return All DataFlowVariables on Node
	 */
	private static List<CharacteristicValue> evaluateAssignments(DFDActionSequenceElement element, List<DataFlowVariable> previousVariables) {
		List<Label> allPrevNodeLabels = new ArrayList<>();
		
		
		previousVariables.stream().forEach(dfv -> {
				dfv.characteristics().stream().forEach(c -> {
					DFDCharacteristicValue cv = (DFDCharacteristicValue) c;
					allPrevNodeLabels.add(cv.label());
				});
			});
		
		
		
		List<Label> outputLabel = new ArrayList<>();
		List<Label> removeFromOutputLabel = new ArrayList<>();
		
		for (var assignment : element.getPreviousNode().getBehaviour().getAssignment()) {
			Flow flow = element.getFlow();
			Pin pin = flow.getSourcePin();
			if(assignment.getOutputPin().equals(pin) && flow.getDestinationNode().equals(element.getNode())) {
				if (assignment instanceof ForwardingAssignment) {
					outputLabel.addAll(allPrevNodeLabels);
				} else if(evaluateTerm(((Assignment)assignment).getTerm(), allPrevNodeLabels)) {
					outputLabel.addAll(((Assignment)assignment).getOutputLabels());						
				} else if(!evaluateTerm(((Assignment)assignment).getTerm(), allPrevNodeLabels)) {
					removeFromOutputLabel.addAll(((Assignment)assignment).getOutputLabels());						
				}
			}
			
		}
		
		outputLabel.removeAll(removeFromOutputLabel);
		
		List<CharacteristicValue> characteristics = new ArrayList<>();
		
		for (Label label : outputLabel) {
			characteristics.add(new DFDCharacteristicValue((LabelType) label.eContainer(), label));
		}
		
		characteristics = characteristics.stream().filter(distinctByKey(CharacteristicValue::getValueId)).collect(Collectors.toList());
		System.out.println(element.getNode().getEntityName() + "size:" + characteristics.size());
		return characteristics;
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
