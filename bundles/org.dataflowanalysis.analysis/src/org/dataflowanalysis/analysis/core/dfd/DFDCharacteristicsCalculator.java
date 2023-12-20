package org.dataflowanalysis.analysis.core.dfd;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.function.Predicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.dataflowanalysis.analysis.core.AbstractActionSequenceElement;
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
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;

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
			
			Node node = element.getNode();
			List<CharacteristicValue> nodeCharacteristics = new ArrayList<CharacteristicValue>();
			for (var label : node.getProperties()) {
				nodeCharacteristics.add(new DFDCharacteristicValue((LabelType) label.eContainer(), label));
			}
			
			List<DataFlowVariable> dataFlowVariables = new ArrayList<DataFlowVariable>(element.getAllDataFlowVariables());			
			dataFlowVariables.add(new DataFlowVariable(element.getNode().getEntityName(), evaluateAssignments(element, previousVariables)));
			DFDActionSequenceElement newElement = new DFDActionSequenceElement(dataFlowVariables, nodeCharacteristics, element.getName(), element.getNode(), element.getPreviousNode(), element.getFlow());
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
		
		for (var assignment : element.getPreviousNode().getBehaviour().getAssignment()) {
			Flow flow = element.getFlow();
			Pin pin = flow.getSourcePin();
			if(assignment.getOutputPin().equals(pin) && flow.getDestinationNode().equals(element.getNode())) {
				if (assignment instanceof ForwardingAssignment) {
					outputLabel.addAll(allPrevNodeLabels);
				} else if(evaluateTerm(((Assignment)assignment).getTerm(), allPrevNodeLabels)) {
					outputLabel.addAll(((Assignment)assignment).getOutputLabels());						
				} else if(!evaluateTerm(((Assignment)assignment).getTerm(), allPrevNodeLabels)) {
					outputLabel.removeAll(((Assignment)assignment).getOutputLabels());						
				}
			}
			
		}
				
		List<CharacteristicValue> characteristics = new ArrayList<>();
		
		for (Label label : outputLabel) {
			characteristics.add(new DFDCharacteristicValue((LabelType) label.eContainer(), label));
		}
		
		characteristics = characteristics.stream().filter(distinctByKey(CharacteristicValue::getValueId)).collect(Collectors.toList());
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