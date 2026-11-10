package org.dataflowanalysis.analysis.tests.integration.dfd.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dfd.core.DFDCharacteristicValue;
import org.dataflowanalysis.dfd.datadictionary.AND;
import org.dataflowanalysis.dfd.datadictionary.AbstractAssignment;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.Behavior;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.ForwardingAssignment;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelReference;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.NOT;
import org.dataflowanalysis.dfd.datadictionary.OR;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.datadictionary.SetAssignment;
import org.dataflowanalysis.dfd.datadictionary.TRUE;
import org.dataflowanalysis.dfd.datadictionary.Term;
import org.dataflowanalysis.dfd.datadictionary.UnsetAssignment;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.dataflowanalysis.dfd.dataflowdiagram.dataflowdiagramFactory;
import tools.mdsd.modelingfoundations.identifier.Entity;

public class DFDTestUtil {
    private static final dataflowdiagramFactory dfdFactory = dataflowdiagramFactory.eINSTANCE;
    private static final datadictionaryFactory ddFactory = datadictionaryFactory.eINSTANCE;

    /**
     * Creates and returns a new data flow diagram
     * @return data flow diagram
     */
    public static DataFlowDiagram createDataFlowDiagram() {
        return dfdFactory.createDataFlowDiagram();
    }

    /**
     * Creates and returns a new data dictionary
     * @return data dictionary
     */
    public static DataDictionary createDataDictionary() {
        return ddFactory.createDataDictionary();
    }

    /**
     * Creates a basic DFD containing all Assignment and Term types and returns a map for easier access to elements
     * @param dataFlowDiagram Container to be filled
     * @param dataDictionary Container to be filled
     * @return Map mapping entity names to nodes, flows and labels
     */
    public static Map<String, Entity> createBasicDFDandDD(DataFlowDiagram dataFlowDiagram, DataDictionary dataDictionary) {
        Map<String, Entity> mapNameToEntity = new HashMap<>();

        Node a = createNode("a", dataFlowDiagram, dataDictionary);
        Node b = createNode("b", dataFlowDiagram, dataDictionary);
        Node c = createNode("c", dataFlowDiagram, dataDictionary);
        Node d = createNode("d", dataFlowDiagram, dataDictionary);
        Node e = createNode("e", dataFlowDiagram, dataDictionary);

        dataFlowDiagram.getNodes()
                .forEach(it -> mapNameToEntity.put(it.getEntityName(), it));

        createFlow(a, b, null, null, "a2b");
        createFlow(b, c, null, null, "b2c");
        createFlow(c, d, null, null, "c2d");
        createFlow(d, e, null, null, "d2e");

        dataFlowDiagram.getFlows()
                .forEach(it -> mapNameToEntity.put(it.getEntityName(), it));

        createAndAddLabelTypeAndLabel(dataDictionary, null, null);
        createAndAddLabelTypeAndLabel(dataDictionary, null, null);

        dataDictionary.getLabelTypes()
                .stream()
                .flatMap(type -> type.getLabel()
                        .stream())
                .forEach(it -> mapNameToEntity.put(it.getEntityName(), it));

        Label label1 = dataDictionary.getLabelTypes()
                .get(0)
                .getLabel()
                .get(0);
        Label label2 = dataDictionary.getLabelTypes()
                .get(1)
                .getLabel()
                .get(0);

        createAndAddAssignment(a, null, null, List.of(label1, label2), null, SetAssignment.class);
        createAndAddAssignment(b, null, null, null, null, ForwardingAssignment.class);
        createAndAddAssignment(b, null, null, List.of(label2), null, UnsetAssignment.class);

        AND term = ddFactory.createAND();
        OR or = ddFactory.createOR();
        TRUE trueTerm = ddFactory.createTRUE();
        NOT not = ddFactory.createNOT();

        not.setNegatedTerm(ddFactory.createTRUE());
        or.getTerms()
                .add(trueTerm);
        or.getTerms()
                .add(not);
        term.getTerms()
                .add(or);

        LabelReference labelReference = ddFactory.createLabelReference();
        labelReference.setLabel(label1);

        term.getTerms()
                .add(labelReference);

        createAndAddAssignment(c, null, null, List.of(label2), term, Assignment.class);

        OR term2 = ddFactory.createOR();
        NOT not2 = ddFactory.createNOT();
        not2.setNegatedTerm(ddFactory.createTRUE());
        term2.getTerms()
                .add(not2);

        LabelReference labelReference2 = ddFactory.createLabelReference();
        labelReference2.setLabel(label1);

        term2.getTerms()
                .add(labelReference2);

        createAndAddAssignment(d, null, null, List.of(label2), term2, Assignment.class);
        return mapNameToEntity;
    }

    /**
     * Creates a new label type and label and adds it to the datadictionary
     * @param dataDictionary Container for new label type
     * @param typeName Name of the new label type, or null for typeX (where X-1 equals number of existing types)
     * @param valueName Name of the new label, or null for valueX
     */
    public static void createAndAddLabelTypeAndLabel(DataDictionary dataDictionary, String typeName, String valueName) {
        LabelType type = ddFactory.createLabelType();
        type.setEntityName(typeName == null ? "type" + dataDictionary.getLabelTypes()
                .size() : typeName);
        dataDictionary.getLabelTypes()
                .add(type);

        Label label = ddFactory.createLabel();
        label.setEntityName(valueName == null ? "label" + dataDictionary.getLabelTypes()
                .size() : valueName);
        type.getLabel()
                .add(label);
    }

    /**
     * Creates a new assignment for the behavior of the node
     * @param node Node for which the assignment is created
     * @param inPins Required in pins, or null for all existing in pins
     * @param outPin Out pin, or null for any outPin
     * @param label List of labels the assignment passes if required
     * @param term Term for Assignment (non abstract) if required
     * @param assignmentType Type of assignment to be created
     */
    public static void createAndAddAssignment(Node node, List<Pin> inPins, Pin outPin, List<Label> label, Term term,
            Class<? extends AbstractAssignment> assignmentType) {
        AbstractAssignment assignment;

        if (assignmentType.equals(ForwardingAssignment.class)) {
            assignment = ddFactory.createForwardingAssignment();
            ((ForwardingAssignment) assignment).getInputPins()
                    .addAll(inPins == null ? node.getBehavior()
                            .getInPin() : inPins);
        } else if (assignmentType.equals(SetAssignment.class)) {
            assignment = ddFactory.createSetAssignment();
            ((SetAssignment) assignment).getOutputLabels()
                    .addAll(label);
        } else if (assignmentType.equals(UnsetAssignment.class)) {
            assignment = ddFactory.createUnsetAssignment();
            ((UnsetAssignment) assignment).getOutputLabels()
                    .addAll(label);
        } else if (assignmentType.equals(Assignment.class)) {
            assignment = ddFactory.createAssignment();
            ((Assignment) assignment).getOutputLabels()
                    .addAll(label);
            ((Assignment) assignment).getInputPins()
                    .addAll(inPins == null ? node.getBehavior()
                            .getInPin() : inPins);
            ((Assignment) assignment).setTerm(term);
        } else {
            throw new IllegalArgumentException();
        }

        assignment.setOutputPin(outPin == null ? node.getBehavior()
                .getOutPin()
                .get(0) : outPin);
        node.getBehavior()
                .getAssignment()
                .add(assignment);
    }

    /**
     * Creates a Node with behavior and adds both to the containers
     * @param name Name of the new node
     * @param dataFlowDiagram Container for new node
     * @param dataDictionary Container for new behavior
     * @return created Node
     */
    public static Node createNode(String name, DataFlowDiagram dataFlowDiagram, DataDictionary dataDictionary) {
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

    /**
     * Creates a Flow and if required the in/out pins for the flow and adds them to the containers
     * @param sourceNode Source node of the flow
     * @param destinationNode Destination node of the flow
     * @param sourcePin Source Pin, or null to create a new one
     * @param destinationPin Destination Pin, or null to create a new one
     * @param name Name of the new flow
     * @return created flow
     */
    public static Flow createFlow(Node sourceNode, Node destinationNode, Pin sourcePin, Pin destinationPin, String name) {
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
        ((DataFlowDiagram) sourceNode.eContainer()).getFlows()
                .add(flow);
        return flow;
    }

    /**
     * Gets all incoming labels on a DFDVertex or DFDSimpleVertex
     * @param vertex DFDVertex or DFDSimpleVertex
     * @return all incoming Labels
     */
    public static List<Label> getAllIncomingLabel(AbstractVertex<?> vertex) {
        return vertex.getAllIncomingDataCharacteristics()
                .stream()
                .flatMap(it -> it.getAllCharacteristics()
                        .stream())
                .map(DFDCharacteristicValue.class::cast)
                .map(it -> it.getLabel())
                .toList();
    }

    /**
     * Gets all outgoing labels on a DFDVertex or DFDSimpleVertex
     * @param vertex DFDVertex or DFDSimpleVertex
     * @return all outgoing Labels
     */
    public static List<Label> getAllOutgoingLabel(AbstractVertex<?> vertex) {
        return vertex.getAllOutgoingDataCharacteristics()
                .stream()
                .flatMap(it -> it.getAllCharacteristics()
                        .stream())
                .map(DFDCharacteristicValue.class::cast)
                .map(it -> it.getLabel())
                .toList();
    }
}
