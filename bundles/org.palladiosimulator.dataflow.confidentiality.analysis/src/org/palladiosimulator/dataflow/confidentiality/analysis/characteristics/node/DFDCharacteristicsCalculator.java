package org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DFDCharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd.DFDActionSequenceElement;

import mdpa.dfd.datadictionary.*;
import mdpa.dfd.dataflowdiagram.*;



public class DFDCharacteristicsCalculator {
	private static HashMap<String, List<Label>> finishedAssignemnts = new HashMap<>();
	private static Set<Assignment> calulatedAssignemnts = new HashSet<>();

	
	public static List<DFDActionSequenceElement> fillDataFlowVariables (List<DFDActionSequenceElement> dfdActionSequenceElements) {
		List<DFDActionSequenceElement> start = new ArrayList<>();
		List<DFDActionSequenceElement> out = new ArrayList<>();
		for (DFDActionSequenceElement elem : dfdActionSequenceElements) {
			if (elem.getAssignment().getInputPins().size() == 0) start.add(elem);
		}
		 ExecutorService executor = Executors.newFixedThreadPool(12); //Change!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		 for (DFDActionSequenceElement startElement : start) {
			 evalStub(startElement);
			 List<Future<List<DFDActionSequenceElement>>> futures = new ArrayList<>();
			 List<DFDActionSequenceElement> test = findNextInLine(startElement, dfdActionSequenceElements);		
			 while (test.size() > 0) {
				 
				 for (DFDActionSequenceElement element : test) {
					 MyCallable myCallable = new MyCallable(element, dfdActionSequenceElements);
					 futures.add(executor.submit(myCallable));
				 }
				 test = new ArrayList<>();
				 for (Future<List<DFDActionSequenceElement>> future : futures) {
					 if (future.isDone()) {
						 try {
							 List<DFDActionSequenceElement> next = future.get();
							 out.add(next.get(next.size()-1));
							 next.remove(next.size()-1);
							 test.addAll(next);
							 futures.remove(future);
						 } catch (Exception e) {
							 e.printStackTrace();
						 }
					 }
				 }
			 }
		 }
		 return out;
	}
	
	
	private static void evalStub(DFDActionSequenceElement dfdActionSequenceElement) {
		AbstractAssignment assignment = dfdActionSequenceElement.getAssignment();
		if (assignment instanceof Assignment) {
			if (evaluateTerm(((Assignment)assignment).getTerm(), new ArrayList<Label>())) finishedAssignemnts.put(assignment.getId(), ((Assignment)assignment).getOutputLabels());
			else finishedAssignemnts.put(assignment.getId(), ((Assignment)assignment).getOutputLabels());
		}
	}
	
	
	
	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
	    KeySetView<Object, Boolean> seen = ConcurrentHashMap.newKeySet();
	    return t -> seen.add(keyExtractor.apply(t));
	}
	
	private static boolean evaluateTerm(Term term, List<Label> inLabels) {
		if (term instanceof TRUE) {
			return true;
		}
		else if (term instanceof NOT) {
			NOT notTerm = (NOT) term;
			return !evaluateTerm(notTerm.getNegatedTerm(), inLabels);
		}
		else if (term instanceof LabelReference) {
			return inLabels.contains(((LabelReference)term).getLabel());
		}
		else if (term instanceof BinaryOperator) {
			BinaryOperator binaryTerm = (BinaryOperator) term;
			if (binaryTerm instanceof AND) {
				return evaluateTerm(binaryTerm.getTerms().get(0), inLabels) && evaluateTerm(binaryTerm.getTerms().get(1), inLabels);
			}
			else if(binaryTerm instanceof OR) {
				return evaluateTerm(binaryTerm.getTerms().get(0), inLabels) || evaluateTerm(binaryTerm.getTerms().get(1), inLabels);
			}
		}
		
		return false;
	}
	
	private static List<DFDActionSequenceElement> findNextInLine(DFDActionSequenceElement element, List<DFDActionSequenceElement> dfdActionSequenceElements) {
		 List<DFDActionSequenceElement> next = new ArrayList<>();
		 for (DFDActionSequenceElement dfdActionSequenceElement: dfdActionSequenceElements) {
			 for (Flow flow: dfdActionSequenceElement.getFlows()) {
				 if (flow.getSourcePin().getId().equals(element.getAssignment().getOutputPin().getId())) next.add(dfdActionSequenceElement);
			 }
		 }
		 return next;
	 }
	
	private static List<Label> evaluateAssigment(AbstractAssignment a, List<Label> inLabel) {
		if (a instanceof Assignment) {
			if (evaluateTerm(((Assignment)a).getTerm(), inLabel)) return ((Assignment)a).getOutputLabels();
			else return new ArrayList<Label>();
		} else {
			return inLabel;
		}
	}
	
		
	private static class MyCallable implements Callable<List<DFDActionSequenceElement>> {		
		private DFDActionSequenceElement eval(DFDActionSequenceElement element) throws InterruptedException{
			
				 List<Label> inLabel = new ArrayList<>();
				 
				 
				 
				 for (Node node : element.getPreviousNodes()) {					 
					 for (AbstractAssignment assignment : node.getBehaviour().getAssignment()) {
						 if (element.getFlows().stream().map(f -> f.getSourcePin()).toList().contains(assignment.getOutputPin())) {
							 	System.out.println("Assignment:" + element.getNode().getEntityName() + " needs: " + assignment.getId() + "   " + node.getEntityName());
								 while (!finishedAssignemnts.containsKey(assignment.getId())) {
								 }
								 inLabel.addAll(finishedAssignemnts.getOrDefault(assignment.getId(), new ArrayList<>()));
							 }
						 	
						 }
					 }
				 
				 AbstractAssignment assignment = element.getAssignment();
				 if (assignment instanceof Assignment) {
					 if (evaluateTerm(((Assignment)assignment).getTerm(), inLabel)) {
						 for (Label label: ((Assignment)assignment).getOutputLabels()) {
							 finishedAssignemnts.putIfAbsent(assignment.getId(), new ArrayList<Label>());
							 finishedAssignemnts.get(assignment.getId()).add(label);
						 }
					 }
				 } else {
					 for (Label label: inLabel) {
						 finishedAssignemnts.putIfAbsent(assignment.getId(), new ArrayList<Label>());
						 finishedAssignemnts.get(assignment.getId()).add(label);
					 }
				 }
				 List<CharacteristicValue> characteristics = new ArrayList<>();
				 for (Label label : inLabel) {
						characteristics.add(new DFDCharacteristicValue((LabelType) label.eContainer(), label));
					}
				 List<DataFlowVariable> dataFlowVariables  = new ArrayList<>(element.getAllDataFlowVariables());
				 dataFlowVariables.add(new DataFlowVariable(element.getNode().getEntityName(), characteristics));
				 
				 List<CharacteristicValue> nodeCharacteristics = new ArrayList<CharacteristicValue>();
					for (var label : element.getNode().getProperties()) {
						nodeCharacteristics.add(new DFDCharacteristicValue((LabelType) label.eContainer(), label));
					}
				 
				 //element.getAllDataFlowVariables().add(new DataFlowVariable(element.getAssignment().getEntityName(), characteristics));
				 
				 DFDActionSequenceElement newElement = new DFDActionSequenceElement(dataFlowVariables, nodeCharacteristics, element.getName(), element.getNode(), element.getPreviousNodes(), element.getAssignment(), element.getFlows());
				 
				 return newElement;
				 
			}
		 
		
		public MyCallable(DFDActionSequenceElement elem,  List<DFDActionSequenceElement> dfdActionSequenceElements) {
			this.elem = elem;
			this.dfdActionSequenceElements = dfdActionSequenceElements;
		}
		
		private DFDActionSequenceElement elem;
		private  List<DFDActionSequenceElement> dfdActionSequenceElements;

		@Override
		public List<DFDActionSequenceElement> call() throws Exception {			
			List<DFDActionSequenceElement> next = findNextInLine(elem, dfdActionSequenceElements);
			next.add(eval(elem));
			return findNextInLine(elem, dfdActionSequenceElements);
		}	
	}
}
