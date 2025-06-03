package org.dataflowanalysis.converter.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.converter.dfd2web.DFD2WebConverter;
import org.dataflowanalysis.converter.dfd2web.DataFlowDiagramAndDictionary;
import org.dataflowanalysis.converter.web2dfd.model.Annotation;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.Behavior;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.ForwardingAssignment;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.dataflowanalysis.dfd.dataflowdiagram.dataflowdiagramFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AnnotationsTest {
    private DFD2WebConverter dfd2WebConverter;
    private final dataflowdiagramFactory dfdFactory = dataflowdiagramFactory.eINSTANCE;
    private final datadictionaryFactory ddFactory = datadictionaryFactory.eINSTANCE;
    private DataFlowDiagram dataFlowDiagram;
    private DataDictionary dataDictionary;
    private Node b;
    private LabelType type;

    @BeforeEach
    public void init() {
        dfd2WebConverter = new DFD2WebConverter();
        dataFlowDiagram = dfdFactory.createDataFlowDiagram();
        dataDictionary = ddFactory.createDataDictionary();

        Node a = createNode("a");
        b = createNode("b");
        Node c = createNode("c");
        createFlow(a, b, null, null, "a2b");
        createFlow(b, c, null, null, "b2c");

        type = ddFactory.createLabelType();
        type.setEntityName("type");
        Label label = ddFactory.createLabel();
        label.setEntityName("value");
        type.getLabel()
                .add(label);
        dataDictionary.getLabelTypes()
                .add(type);

        Assignment assignment = ddFactory.createAssignment();
        assignment.setOutputPin(a.getBehavior()
                .getOutPin()
                .get(0));
        assignment.setTerm(ddFactory.createTRUE());
        assignment.getOutputLabels()
                .add(label);
        a.getBehavior()
                .getAssignment()
                .add(assignment);

        ForwardingAssignment forwardingAssignment = ddFactory.createForwardingAssignment();
        forwardingAssignment.getInputPins()
                .addAll(b.getBehavior()
                        .getInPin());
        forwardingAssignment.setOutputPin(b.getBehavior()
                .getOutPin()
                .get(0));
        b.getBehavior()
                .getAssignment()
                .add(forwardingAssignment);
    }

    @Test
    public void testPropagatedLabelsAnnotation() {
        Map<String, Annotation> nodeNameToAnnotationMap = new HashMap<>();
        var webDfd = dfd2WebConverter.convert(new DataFlowDiagramAndDictionary(dataFlowDiagram, dataDictionary));
        webDfd.getModel()
                .model()
                .children()
                .stream()
                .filter(child -> child.type()
                        .startsWith("node"))
                .forEach(node -> {
                    nodeNameToAnnotationMap.put(node.text(), node.annotation());
                });
        assertEquals("PropagatedLabels:type.value", nodeNameToAnnotationMap.get("a")
                .message()
                .replace(" ", "")
                .replace("\n", ""));
        assertEquals("#FFFFFF", nodeNameToAnnotationMap.get("a")
                .color());
        assertEquals("tag", nodeNameToAnnotationMap.get("a")
                .icon());
        assertEquals("PropagatedLabels:type.value", nodeNameToAnnotationMap.get("b")
                .message()
                .replace(" ", "")
                .replace("\n", ""));
        assertEquals(null, nodeNameToAnnotationMap.get("c"));
    }

    @Test
    public void testViolationsAnnotation() {
        Label label = ddFactory.createLabel();
        label.setEntityName("violation");
        type.getLabel()
                .add(label);

        b.getProperties()
                .add(label);
        List<Predicate<? super AbstractVertex<?>>> conditions = new ArrayList<>();
        conditions.add(this::condition);

        Map<String, Annotation> nodeNameToAnnotationMap = new HashMap<>();
        dfd2WebConverter.setConditions(conditions);
        var webDfd = dfd2WebConverter.convert(new DataFlowDiagramAndDictionary(dataFlowDiagram, dataDictionary));
        webDfd.getModel()
                .model()
                .children()
                .stream()
                .filter(child -> child.type()
                        .startsWith("node"))
                .forEach(node -> {
                    nodeNameToAnnotationMap.put(node.text(), node.annotation());
                });
        assertEquals("PropagatedLabels:type.value", nodeNameToAnnotationMap.get("a")
                .message()
                .replace(" ", "")
                .replace("\n", ""));
        assertEquals("PropagatedLabels:type.valueViolation:Constraint0violated.", nodeNameToAnnotationMap.get("b")
                .message()
                .replace(" ", "")
                .replace("\n", ""));
        assertEquals("#ff0000", nodeNameToAnnotationMap.get("b")
                .color());
        assertEquals("bolt", nodeNameToAnnotationMap.get("b")
                .icon());
        assertNull(nodeNameToAnnotationMap.get("c"));
    }

    private boolean condition(AbstractVertex<?> node) {
        List<String> properties = node.getVertexCharacteristics("type")
                .stream()
                .map(CharacteristicValue::getValueName)
                .toList();
        List<String> incomingLabeList = node.getAllIncomingDataCharacteristics()
                .stream()
                .flatMap(it -> it.getAllCharacteristics()
                        .stream()
                        .map(c -> c.getValueName()))
                .collect(Collectors.toList());

        return properties.contains("violation") && incomingLabeList.contains("value");
    }

    private Node createNode(String name) {
        Node node = dfdFactory.createProcess();
        node.setEntityName(name);
        Behavior behaviour = ddFactory.createBehavior();
        behaviour.setEntityName(name + "_behaviour");
        node.setBehavior(behaviour);
        dataFlowDiagram.getNodes()
                .add(node);
        dataDictionary.getBehavior()
                .add(behaviour);
        return node;
    }

    private Flow createFlow(Node sourceNode, Node destinationNode, Pin sourcePin, Pin destinationPin, String name) {
        Flow flow = dfdFactory.createFlow();
        flow.setDestinationNode(destinationNode);
        flow.setSourceNode(sourceNode);
        if (sourcePin == null) {
            sourcePin = ddFactory.createPin();
            sourcePin.setEntityName(sourceNode.getEntityName() + "_out_" + sourceNode.getBehavior()
                    .getOutPin()
                    .size());
            sourceNode.getBehavior()
                    .getOutPin()
                    .add(sourcePin);
        }
        if (destinationPin == null) {
            destinationPin = ddFactory.createPin();
            destinationPin.setEntityName(destinationNode.getEntityName() + "_in_" + destinationNode.getBehavior()
                    .getInPin()
                    .size());
            destinationNode.getBehavior()
                    .getInPin()
                    .add(destinationPin);
        }
        flow.setDestinationPin(destinationPin);
        flow.setSourcePin(sourcePin);
        flow.setEntityName(name);
        dataFlowDiagram.getFlows()
                .add(flow);
        return flow;
    }
}
