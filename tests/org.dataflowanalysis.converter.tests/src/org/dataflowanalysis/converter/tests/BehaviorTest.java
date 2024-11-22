package org.dataflowanalysis.converter.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.dataflowanalysis.converter.BehaviorConverter;
import org.dataflowanalysis.converter.DataFlowDiagramAndDictionary;
import org.dataflowanalysis.converter.DataFlowDiagramConverter;
import org.dataflowanalysis.converter.webdfd.Child;
import org.dataflowanalysis.converter.webdfd.WebEditorDfd;
import org.dataflowanalysis.dfd.datadictionary.Behavior;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.datadictionary.*;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.dataflowanalysis.dfd.dataflowdiagram.dataflowdiagramFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class BehaviorTest {
    private BehaviorConverter behaviourConverter;
    private DataFlowDiagramConverter dataFlowDiagramConverter;
    private final dataflowdiagramFactory dfdFactory = dataflowdiagramFactory.eINSTANCE;
    private final datadictionaryFactory ddFactory = datadictionaryFactory.eINSTANCE;
    private DataFlowDiagram dataFlowDiagram;
    private DataDictionary dataDictionary;

    @BeforeEach
    public void init() {    	
    	dataFlowDiagramConverter = new DataFlowDiagramConverter();
        dataFlowDiagram = dfdFactory.createDataFlowDiagram();
        dataDictionary = ddFactory.createDataDictionary();
        behaviourConverter = new BehaviorConverter(dataDictionary);
    }

    @ParameterizedTest
    @ValueSource(strings = {"TRUE || FALSE", "TypeA.ValueA && TypeB.ValueB", "TypeA.ValueA || TypeB.ValueB", "!TypeA.ValueA && TypeB.ValueB",
            "TypeA.ValueA || !TypeB.ValueB", "(TypeA.ValueA && TypeB.ValueB) || TypeC.ValueC", "!(TypeA.ValueA || TypeB.ValueB) && TypeC.ValueC",
            "((TypeA.ValueA && TRUE) || !TypeB.ValueB) || FALSE", "(!TypeA.ValueA && TypeB.ValueB) || (TypeC.ValueC && !TypeD.ValueD)",
            "((TypeA.ValueA || !TypeB.ValueB) && TypeC.ValueC) || (TypeD.ValueD && !(TypeE.ValueE || TypeF.ValueF))",
            "!((TypeA.ValueA && (TypeB.ValueB || !TypeC.ValueC)) || (!(TypeD.ValueD && TypeE.ValueE) && (TypeF.ValueF || TypeG.ValueG)))"})
    @DisplayName("Test Behavior Conversion")
    void testBehaviorConversion(String behavior) {
    	for (char c = 'A'; c <= 'G'; c++) {
    		LabelType type = ddFactory.createLabelType();
        	type.setEntityName("Type" + c);
        	Label label = ddFactory.createLabel();
        	label.setEntityName("Value" + c);
        	type.getLabel().add(label);
        	dataDictionary.getLabelTypes().add(type);
    	}
        assertEquals(behavior, behaviourConverter.termToString(behaviourConverter.stringToTerm(behavior)));
    }
    
    
    @Test
    void testAssignmentConversion() {
    	Node a = createNode("a");
    	Node b = createNode("b");
    	Node c = createNode("c");
    	createFlow(a, b, null, null, "a2b");
    	createFlow(b, c, null, null, "b2c");
    	Assignment assignment = ddFactory.createAssignment();
    	assignment.getInputPins().addAll(b.getBehavior().getInPin());
    	assignment.setOutputPin(b.getBehavior().getOutPin().get(0));
    	NOT not = ddFactory.createNOT();
    	AND and = ddFactory.createAND();
    	TRUE trueTerm = ddFactory.createTRUE();
    	LabelReference ref = ddFactory.createLabelReference();
    	
    	LabelType type = ddFactory.createLabelType();
    	type.setEntityName("type");
    	Label label = ddFactory.createLabel();
    	label.setEntityName("value");
    	type.getLabel().add(label);
    	Label label2 = ddFactory.createLabel();
    	label2.setEntityName("value2");
    	type.getLabel().add(label2);
    	dataDictionary.getLabelTypes().add(type);
    	    	
    	ref.setLabel(label);
    	
    	and.getTerms().add(ref);
    	and.getTerms().add(trueTerm);
    	
    	not.setNegatedTerm(and);
    	
    	assignment.setTerm(not);
    	
    	assignment.getOutputLabels().add(label);
    	assignment.getOutputLabels().add(label2);
    	
    	b.getBehavior().getAssignment().add(assignment);
    	
    	var webDfd = dataFlowDiagramConverter.dfdToWeb(new DataFlowDiagramAndDictionary(dataFlowDiagram, dataDictionary));
    	
    	testAssignment(webDfd, "b", List.of("assign type.value,type.value2 if !(type.value && TRUE) from a2b"));    	
    }
    
    @Test
    void testFlowNameAndPinDelimiter() {
    	Node a = createNode("a");
    	Node b = createNode("b");
    	Node c = createNode("c");
    	Node d = createNode("d");
    	
    	createFlow(a, c, null, null, "a2c");
    	Pin destinationPin = c.getBehavior().getInPin().get(0);
    	createFlow(b, c, null, destinationPin, "b2c");
    	
    	Pin c_out = ddFactory.createPin();
    	ForwardingAssignment assignment = ddFactory.createForwardingAssignment();
    	assignment.setOutputPin(c_out);
    	assignment.getInputPins().add(c.getBehavior().getInPin().get(0));
    	c.getBehavior().getOutPin().add(c_out);
    	c.getBehavior().getAssignment().add(assignment);
    	
    	createFlow(c, d, c_out, null, "c2d");
    	
    	var webDfd = dataFlowDiagramConverter.dfdToWeb(new DataFlowDiagramAndDictionary(dataFlowDiagram, dataDictionary));
    	
    	testAssignment(webDfd, "c", List.of("forward a2c|b2c"));    	
    	
    	Node z = createNode("z");
    	var newFlow = createFlow(z, c, null, null, "z2c");
    	assignment.getInputPins().add(newFlow.getDestinationPin());
    	
    	//prevents exception before analysis hotfix merge, Can be deleted after
    	Node y = createNode("y");
    	createFlow(y, z, null, null, "y2z");
    	ForwardingAssignment assignment2 = ddFactory.createForwardingAssignment();
    	assignment2.setOutputPin(z.getBehavior().getOutPin().get(0));
    	assignment2.getInputPins().add(z.getBehavior().getInPin().get(0));
    	z.getBehavior().getAssignment().add(assignment2);
    	
    	webDfd = dataFlowDiagramConverter.dfdToWeb(new DataFlowDiagramAndDictionary(dataFlowDiagram, dataDictionary));
    	
    	testAssignment(webDfd, "c", List.of("forward a2c|b2c,z2c"));    	
    }
    
    private void testAssignment(WebEditorDfd webDFD, String nodeName, List<String> assignments) {
    	for (Child child : webDFD.model().children()) {
    		if (child.type().startsWith("node") && child.text().equals(nodeName)) {
    			List<String> assignmentsFromString = child.ports().stream().flatMap(s -> ((s.behavior() != null) ? List.of(s.behavior().split("\n")) : new ArrayList<String>()).stream()).collect(Collectors.toList());
    			assertEquals(assignments, assignmentsFromString);
    		}
    	}
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
    		destinationPin.setEntityName(destinationNode.getEntityName() + "_in_" + destinationNode.getBehavior().getInPin().size());
    		destinationNode.getBehavior().getInPin().add(destinationPin);
    	}
    	flow.setDestinationPin(destinationPin);
    	flow.setSourcePin(sourcePin);
    	flow.setEntityName(name);
    	dataFlowDiagram.getFlows().add(flow);
    	return flow;
    }
}
