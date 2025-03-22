package org.dataflowanalysis.analysis.tests.dfd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.analysis.dfd.resource.DFDModelResourceProvider;
import org.dataflowanalysis.dfd.datadictionary.Behavior;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.ForwardingAssignment;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.datadictionary.SetAssignment;
import org.dataflowanalysis.dfd.datadictionary.UnsetAssignment;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.dataflowanalysis.dfd.dataflowdiagram.dataflowdiagramFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.mdsd.modelingfoundations.identifier.Entity;

public class AssignmentsTest {
    private final dataflowdiagramFactory dfdFactory = dataflowdiagramFactory.eINSTANCE;
    private final datadictionaryFactory ddFactory = datadictionaryFactory.eINSTANCE;
    private DataFlowDiagram dataFlowDiagram;
    private DataDictionary dataDictionary;

    @BeforeEach
    public void init() {
        dataFlowDiagram = dfdFactory.createDataFlowDiagram();
        dataDictionary = ddFactory.createDataDictionary();

        LabelType type = ddFactory.createLabelType();
        type.setEntityName("type");
        Label label = ddFactory.createLabel();
        label.setEntityName("value");
        type.getLabel()
                .add(label);
        dataDictionary.getLabelTypes()
                .add(type);

    }

    @Test
    public void testTFGBuildingWithSetAssignments() {
        // Test whether Set Assignment starts TFG of 2 Nodes
        Node a = createNode("a");
        Node b = createNode("b");

        createFlow(a, b, null, null, "a2b");

        SetAssignment setAssignment = ddFactory.createSetAssignment();
        setAssignment.setOutputPin(a.getBehavior()
                .getOutPin()
                .get(0));

        a.getBehavior()
                .getAssignment()
                .add(setAssignment);

        var analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .useCustomResourceProvider(new DFDModelResourceProvider(dataDictionary, dataFlowDiagram))
                .build();
        var tfg = analysis.findFlowGraphs();
        tfg.evaluate();

        assertEquals(tfg.getTransposeFlowGraphs()
                .size(), 1);
        assertEquals(tfg.getTransposeFlowGraphs()
                .get(0)
                .getVertices()
                .size(), 2);

        // Test whether new node with set assignments creates unconnected TFG
        Node c = createNode("c");
        createFlow(c, a, null, null, "c2a");

        SetAssignment setAssignment2 = ddFactory.createSetAssignment();
        setAssignment2.setOutputPin(c.getBehavior()
                .getOutPin()
                .get(0));

        c.getBehavior()
                .getAssignment()
                .add(setAssignment2);

        analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .useCustomResourceProvider(new DFDModelResourceProvider(dataDictionary, dataFlowDiagram))
                .build();
        tfg = analysis.findFlowGraphs();
        tfg.evaluate();

        assertEquals(tfg.getTransposeFlowGraphs()
                .size(), 2);

        // Tests whether assignment with input Pins connects the 2 tfgs
        ForwardingAssignment forwardingAssignment = ddFactory.createForwardingAssignment();
        forwardingAssignment.setOutputPin(a.getBehavior()
                .getOutPin()
                .get(0));
        forwardingAssignment.getInputPins()
                .addAll(a.getBehavior()
                        .getInPin());

        a.getBehavior()
                .getAssignment()
                .add(forwardingAssignment);

        analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .useCustomResourceProvider(new DFDModelResourceProvider(dataDictionary, dataFlowDiagram))
                .build();
        tfg = analysis.findFlowGraphs();
        tfg.evaluate();

        assertEquals(tfg.getTransposeFlowGraphs()
                .size(), 1);
    }

    @Test
    public void testSetAndUnsetBehavior() {
        // Test whether Set Assignment sets Label
        Node a = createNode("a");
        Node b = createNode("b");

        createFlow(a, b, null, null, "a2b");

        SetAssignment setAssignment = ddFactory.createSetAssignment();
        setAssignment.setOutputPin(a.getBehavior()
                .getOutPin()
                .get(0));
        setAssignment.getOutputLabels()
                .add(dataDictionary.getLabelTypes()
                        .get(0)
                        .getLabel()
                        .get(0));

        a.getBehavior()
                .getAssignment()
                .add(setAssignment);

        var analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .useCustomResourceProvider(new DFDModelResourceProvider(dataDictionary, dataFlowDiagram))
                .build();
        var tfg = analysis.findFlowGraphs();
        tfg.evaluate();

        tfg.getTransposeFlowGraphs()
                .forEach(fg -> {
                    fg.getVertices()
                            .forEach(vertex -> {
                                if (((Entity) vertex.getReferencedElement()).getEntityName()
                                        .equals("a")) {
                                    assertEquals(getAllCharacteristicValues((DFDVertex) vertex).size(), 1);
                                }
                            });
                });

        // Test whether Unset Assignment removes Label
        UnsetAssignment unsetAssignment = ddFactory.createUnsetAssignment();
        unsetAssignment.setOutputPin(a.getBehavior()
                .getOutPin()
                .get(0));
        unsetAssignment.getOutputLabels()
                .add(dataDictionary.getLabelTypes()
                        .get(0)
                        .getLabel()
                        .get(0));

        a.getBehavior()
                .getAssignment()
                .add(unsetAssignment);

        analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .useCustomResourceProvider(new DFDModelResourceProvider(dataDictionary, dataFlowDiagram))
                .build();
        tfg = analysis.findFlowGraphs();
        tfg.evaluate();

        tfg.getTransposeFlowGraphs()
                .forEach(fg -> {
                    fg.getVertices()
                            .forEach(vertex -> {
                                if (((Entity) vertex.getReferencedElement()).getEntityName()
                                        .equals("a")) {
                                    assertEquals(getAllCharacteristicValues((DFDVertex) vertex).size(), 0);
                                }
                            });
                });

        // Test Whether the same works for other assignments
        a.getBehavior()
                .getAssignment()
                .remove(unsetAssignment);

        Node c = createNode("c");
        createFlow(b, c, null, null, "b2c");

        ForwardingAssignment forwardingAssignment = ddFactory.createForwardingAssignment();
        forwardingAssignment.setOutputPin(b.getBehavior()
                .getOutPin()
                .get(0));
        forwardingAssignment.getInputPins()
                .addAll(b.getBehavior()
                        .getInPin());

        b.getBehavior()
                .getAssignment()
                .add(forwardingAssignment);

        unsetAssignment.setOutputPin(b.getBehavior()
                .getOutPin()
                .get(0));

        b.getBehavior()
                .getAssignment()
                .add(unsetAssignment);

        analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .useCustomResourceProvider(new DFDModelResourceProvider(dataDictionary, dataFlowDiagram))
                .build();
        tfg = analysis.findFlowGraphs();
        tfg.evaluate();

        tfg.getTransposeFlowGraphs()
                .forEach(fg -> {
                    fg.getVertices()
                            .forEach(vertex -> {
                                if (((Entity) vertex.getReferencedElement()).getEntityName()
                                        .equals("b")) {
                                    assertEquals(getAllCharacteristicValues((DFDVertex) vertex).size(), 0);
                                }
                            });
                });
    }

    private List<CharacteristicValue> getAllCharacteristicValues(DFDVertex vertex) {
        return vertex.getAllOutgoingDataCharacteristics()
                .stream()
                .flatMap(it -> it.getAllCharacteristics()
                        .stream())
                .collect(Collectors.toList());
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
