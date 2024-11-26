package org.dataflowanalysis.analysis.tests.dfd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.resource.DFDModelResourceProvider;
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

public class EdgeCaseTest {
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
    public void deadInputPinSkippedCheck() {
        Node store = createNode("store");
        Node fulfilled = createNode("fulfilled");
        Node unfulfilled = createNode("unfulfilled");

        unfulfilled.getBehavior()
                .getInPin()
                .add(ddFactory.createPin());

        createFlow(fulfilled, store, null, null, "fulfilled2store");
        createFlow(unfulfilled, store, null, store.getBehavior()
                .getInPin()
                .get(0), "unfulfilled2store");

        Assignment fulfilledAssignment = ddFactory.createAssignment();
        fulfilledAssignment.setTerm(ddFactory.createTRUE());
        fulfilledAssignment.setOutputPin(fulfilled.getBehavior()
                .getOutPin()
                .get(0));
        fulfilledAssignment.getOutputLabels()
                .add(dataDictionary.getLabelTypes()
                        .get(0)
                        .getLabel()
                        .get(0));
        fulfilled.getBehavior()
                .getAssignment()
                .add(fulfilledAssignment);

        ForwardingAssignment unfulfilledAssignment = ddFactory.createForwardingAssignment();
        unfulfilledAssignment.setOutputPin(unfulfilled.getBehavior()
                .getOutPin()
                .get(0));
        unfulfilledAssignment.getInputPins()
                .add(unfulfilled.getBehavior()
                        .getInPin()
                        .get(0));
        unfulfilled.getBehavior()
                .getAssignment()
                .add(unfulfilledAssignment);

        var analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .useCustomResourceProvider(new DFDModelResourceProvider(dataDictionary, dataFlowDiagram))
                .build();

        var flowGraphs = analysis.findFlowGraphs();

        assertEquals(flowGraphs.getTransposeFlowGraphs()
                .size(), 1);
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
