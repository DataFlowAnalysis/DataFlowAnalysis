package org.dataflowanalysis.analysis.tests.dfd.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dataflowanalysis.analysis.dfd.core.DFDCharacteristicValue;
import org.dataflowanalysis.analysis.dfd.simple.DFDSimpleTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.simple.DFDSimpleVertex;
import org.dataflowanalysis.dfd.datadictionary.AbstractAssignment;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.Behavior;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.ForwardingAssignment;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelReference;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.datadictionary.SetAssignment;
import org.dataflowanalysis.dfd.datadictionary.Term;
import org.dataflowanalysis.dfd.datadictionary.AND;
import org.dataflowanalysis.dfd.datadictionary.NOT;
import org.dataflowanalysis.dfd.datadictionary.OR;
import org.dataflowanalysis.dfd.datadictionary.TRUE;
import org.dataflowanalysis.dfd.datadictionary.UnsetAssignment;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.dataflowanalysis.dfd.dataflowdiagram.dataflowdiagramFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DFDSimpleUnitTests {
	private final dataflowdiagramFactory dfdFactory = dataflowdiagramFactory.eINSTANCE;
	private final datadictionaryFactory ddFactory = datadictionaryFactory.eINSTANCE;
	private DataFlowDiagram dataFlowDiagram;
	private DataDictionary dataDictionary;
	private Label label1;
	private Label label2;

	@BeforeEach
	public void init() {
		dataFlowDiagram = dfdFactory.createDataFlowDiagram();
		dataDictionary = ddFactory.createDataDictionary();

		LabelType type = ddFactory.createLabelType();
		type.setEntityName("type");
		label1 = ddFactory.createLabel();
		label1.setEntityName("value1");
		type.getLabel().add(label1);
		label2 = ddFactory.createLabel();
		label2.setEntityName("value2");
		type.getLabel().add(label2);
		dataDictionary.getLabelTypes().add(type);
	}
	
	@Test
	public void testCopying() {
		Node a = createNode("a");
		Node b = createNode("b");
		
		Flow a2b_1 = createFlow(a, b, null, null, "a2b_1");
		Flow a2b_2 = createFlow(a, b, null, null, "a2b_2");
		
		a.getBehavior().getOutPin().forEach(pin -> {
			var assignment = ddFactory.createAssignment();
			assignment.setTerm(ddFactory.createTRUE());
			assignment.setOutputPin(pin);
			assignment.getOutputLabels().add(dataDictionary.getLabelTypes().get(0).getLabel().get(0));
			a.getBehavior().getAssignment().add(assignment);
		});		
				
		var tfg = new DFDSimpleTransposeFlowGraphFinder(dataDictionary, dataFlowDiagram).findTransposeFlowGraphs().get(0);
		
		var copiedFlowGraph = tfg.copy();
		
		assert(tfg.getVertices().stream().map(DFDSimpleVertex.class::cast).allMatch(vertex -> {
			return copiedFlowGraph.getVertices().stream().map(DFDSimpleVertex.class::cast).anyMatch(copiedVertex -> vertex.equalsSemantically(copiedVertex));
		}));
	}
	
	@Test
	public void testEvaluation() {
		Node a = createNode("a");
		Node b = createNode("b");
		Node c = createNode("c");
		Node d = createNode("d");
		Node e = createNode("e");		
		
		Flow a2b = createFlow(a, b, null, null, "a2b");
		Flow b2c = createFlow(b, c, null, null, "b2c");
		Flow c2d = createFlow(c, d, null, null, "c2d");
		Flow d2e = createFlow(d, e, null, null, "d2e");
		
		createAndAddAssignment(a, null, null, List.of(label1, label2), null, SetAssignment.class);
		createAndAddAssignment(b, null, null, null, null, ForwardingAssignment.class);
		createAndAddAssignment(b, null, null, List.of(label2), null, UnsetAssignment.class);
		
		AND term = ddFactory.createAND();
		OR or = ddFactory.createOR();
		TRUE trueTerm = ddFactory.createTRUE();
		NOT not = ddFactory.createNOT();
		
		not.setNegatedTerm(ddFactory.createTRUE());
		or.getTerms().add(trueTerm);
		or.getTerms().add(not);
		term.getTerms().add(or);
		
		LabelReference labelReference = ddFactory.createLabelReference();
		labelReference.setLabel(label1);
		
		term.getTerms().add(labelReference);
		
		createAndAddAssignment(c, null, null, List.of(label2), term, Assignment.class);
		
		OR term2 = ddFactory.createOR();
		NOT not2 = ddFactory.createNOT();
		not2.setNegatedTerm(ddFactory.createTRUE());
		term2.getTerms().add(not2);

		LabelReference labelReference2 = ddFactory.createLabelReference();
		labelReference2.setLabel(label1);
		
		term2.getTerms().add(labelReference2);
		
		createAndAddAssignment(d, null, null, List.of(label2), term2, Assignment.class);
		
		DFDSimpleVertex aVertex = new DFDSimpleVertex(a, new HashSet<>(), Map.of(a2b.getSourcePin(), a2b));
		DFDSimpleVertex bVertex = new DFDSimpleVertex(b, Set.of(aVertex), Map.of(a2b.getDestinationPin(), a2b, b2c.getSourcePin(), b2c));
		DFDSimpleVertex cVertex = new DFDSimpleVertex(c, Set.of(bVertex), Map.of(b2c.getDestinationPin(), b2c, c2d.getSourcePin(), c2d));
		DFDSimpleVertex dVertex = new DFDSimpleVertex(d, Set.of(cVertex), Map.of(c2d.getDestinationPin(), c2d, d2e.getSourcePin(), d2e));
		DFDSimpleVertex eVertex = new DFDSimpleVertex(e, Set.of(dVertex), Map.of(d2e.getDestinationPin(), d2e));
		
		eVertex.evaluateDataFlow();
		
		assertEquals(getAllOutgoingLabel(aVertex), List.of(label1, label2));
		assertEquals(getAllIncomingLabel(bVertex), List.of(label1, label2));
		assertEquals(getAllOutgoingLabel(bVertex),  List.of(label1));
		assertEquals(getAllIncomingLabel(cVertex),  List.of(label1));
		assertEquals(getAllOutgoingLabel(cVertex),  List.of(label2));
		assertEquals(getAllIncomingLabel(dVertex), List.of(label2));
		assertEquals(getAllOutgoingLabel(dVertex), List.of());
		assertEquals(getAllIncomingLabel(eVertex), List.of());
		
		var tfg = new DFDSimpleTransposeFlowGraphFinder(dataDictionary, dataFlowDiagram).findTransposeFlowGraphs().get(0);
		tfg.evaluate();
		
		assert(tfg.getVertices().stream().map(DFDSimpleVertex.class::cast).allMatch(it -> {
			return List.of(aVertex, bVertex, cVertex, dVertex, eVertex).stream().anyMatch(vertex -> vertex.equalsSemantically(it));
		}));
	}
	
	@Test
	public void testToString() {
		Node a = createNode("a");
		DFDSimpleVertex vertex = new DFDSimpleVertex(a, new HashSet<>(), new HashMap<>());
		assertEquals(vertex.getName(), "a");
		assertEquals(vertex.toString(), "(a, " + a.getId() + ")");
	}
	
	private List<Label> getAllIncomingLabel(DFDSimpleVertex vertex) {
		return vertex.getAllIncomingDataCharacteristics().stream().flatMap(it -> it.getAllCharacteristics().stream()).map(DFDCharacteristicValue.class::cast).map(it -> it.getLabel()).toList();
	}
	
	private List<Label> getAllOutgoingLabel(DFDSimpleVertex vertex) {
		return vertex.getAllOutgoingDataCharacteristics().stream().flatMap(it -> it.getAllCharacteristics().stream()).map(DFDCharacteristicValue.class::cast).map(it -> it.getLabel()).toList();
	}
	
	private void createAndAddAssignment(Node node, List<Pin> inPins, Pin outPin, List<Label> label, Term term, Class<? extends AbstractAssignment> assignmentType) {
		AbstractAssignment assignment;
		
		if (assignmentType.equals(ForwardingAssignment.class)) {
			assignment = ddFactory.createForwardingAssignment();
			((ForwardingAssignment)assignment).getInputPins().addAll(inPins == null ? node.getBehavior().getInPin() : inPins);
		} else if (assignmentType.equals(SetAssignment.class)) {
			assignment = ddFactory.createSetAssignment();
			((SetAssignment)assignment).getOutputLabels().addAll(label);
		} else if (assignmentType.equals(UnsetAssignment.class)) {
			assignment = ddFactory.createUnsetAssignment();
			((UnsetAssignment)assignment).getOutputLabels().addAll(label);
		} else if (assignmentType.equals(Assignment.class)) {
			assignment = ddFactory.createAssignment();
			((Assignment)assignment).getOutputLabels().addAll(label);
			((Assignment)assignment).getInputPins().addAll(inPins == null ? node.getBehavior().getInPin() : inPins);
			((Assignment)assignment).setTerm(term);
		} else {
			throw new IllegalArgumentException();
		}
		
		assignment.setOutputPin(outPin == null ? node.getBehavior().getOutPin().get(0) : outPin);
		node.getBehavior().getAssignment().add(assignment);
	}
	
	
	private Node createNode(String name) {
		Node node = dfdFactory.createProcess();
		node.setEntityName(name);
		Behavior behaviour = ddFactory.createBehavior();
		behaviour.setEntityName(name + "_behaviour");
		node.setBehavior(behaviour);
		dataFlowDiagram.getNodes().add(node);
		dataDictionary.getBehavior().add(behaviour);
		return node;
	}

	private Flow createFlow(Node sourceNode, Node destinationNode, Pin sourcePin, Pin destinationPin, String name) {
		Flow flow = dfdFactory.createFlow();
		flow.setDestinationNode(destinationNode);
		flow.setSourceNode(sourceNode);
		if (sourcePin == null) {
			sourcePin = ddFactory.createPin();
			sourcePin.setEntityName(sourceNode.getEntityName() + "_out_" + sourceNode.getBehavior().getOutPin().size());
			sourceNode.getBehavior().getOutPin().add(sourcePin);
		}
		if (destinationPin == null) {
			destinationPin = ddFactory.createPin();
			destinationPin.setEntityName(
					destinationNode.getEntityName() + "_in_" + destinationNode.getBehavior().getInPin().size());
			destinationNode.getBehavior().getInPin().add(destinationPin);
		}
		flow.setDestinationPin(destinationPin);
		flow.setSourcePin(sourcePin);
		flow.setEntityName(name);
		dataFlowDiagram.getFlows().add(flow);
		return flow;
	}
}
