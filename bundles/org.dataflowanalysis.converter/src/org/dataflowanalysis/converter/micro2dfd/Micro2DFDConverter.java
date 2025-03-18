package org.dataflowanalysis.converter.micro2dfd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.dataflowanalysis.converter.microsecend.*;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.dataflowanalysis.dfd.dataflowdiagram.dataflowdiagramFactory;

/**
 * Converts MicroSecEnd models to the data flow diagram and dictionary representation
 */
public class Micro2DFDConverter extends Converter {
    private final dataflowdiagramFactory dfdFactory;
    private final datadictionaryFactory ddFactory;

    private Map<String, Node> nodesMap;
    private Map<Node, List<String>> nodeToLabelNamesMap;
    private Map<Node, Map<String, List<String>>> nodeToLabelTypeNamesMap;
    private Map<String, Map<String, Label>> labelMap;
    private Map<String, LabelType> labelTypeMap;
    private Map<Pin, List<Label>> outpinToFlowLabelMap;

    private int idCounter;

    public Micro2DFDConverter() {
        dfdFactory = dataflowdiagramFactory.eINSTANCE;
        ddFactory = datadictionaryFactory.eINSTANCE;

    }

    @Override
    public DataFlowDiagramAndDictionary convert(ConverterModel input) {
        Optional<MicroConverterModel> microModel = input.toType(MicroConverterModel.class);
        if (microModel.isEmpty()) {
            logger.error("Expected MicroConverterModel, but got: " + input.getClass()
                    .getSimpleName());
            throw new IllegalArgumentException("Invalid input for Model Conversion");
        }
        return processMicro(microModel.get()
                .getModel());
    }

    private DataFlowDiagramAndDictionary processMicro(MicroSecEnd micro) {
        nodesMap = new HashMap<>();
        nodeToLabelNamesMap = new HashMap<>();
        labelMap = new HashMap<>();
        labelTypeMap = new HashMap<>();
        nodeToLabelTypeNamesMap = new HashMap<>();
        outpinToFlowLabelMap = new HashMap<>();

        idCounter = 0;

        DataFlowDiagram dfd = dfdFactory.createDataFlowDiagram();
        DataDictionary dd = ddFactory.createDataDictionary();
        dfd.setId(Integer.toString(idCounter++));
        dd.setId(Integer.toString(idCounter++));

        createExternalEntities(micro, dfd);

        createProcesses(micro, dfd);

        LabelType stereotype = ddFactory.createLabelType();
        stereotype.setEntityName("Stereotype");
        stereotype.setId(Integer.toString(idCounter++));
        dd.getLabelTypes()
                .add(stereotype);
        labelTypeMap.put(stereotype.getEntityName(), stereotype);
        labelMap.put(stereotype.getEntityName(), new HashMap<>());

        createBehavior(dd, stereotype);

        createFlows(micro, dfd, dd, stereotype);

        createNodeAssignments();

        createForwardingAssignments();

        return new DataFlowDiagramAndDictionary(dfd, dd);
    }

    private void createProcesses(MicroSecEnd micro, DataFlowDiagram dfd) {
        for (Service service : micro.services()) {
            var process = dfdFactory.createProcess();
            process.setEntityName(service.name());
            process.setId(Integer.toString(idCounter++));

            dfd.getNodes()
                    .add(process);
            nodesMap.put(service.name(), process);
            nodeToLabelNamesMap.put(process, service.stereotypes());
            nodeToLabelTypeNamesMap.put(process, service.taggedValues());
        }
    }

    private void createExternalEntities(MicroSecEnd micro, DataFlowDiagram dfd) {
        for (ExternalEntity ee : micro.externalEntities()) {
            var external = dfdFactory.createExternal();
            external.setEntityName(ee.name());
            external.setId(Integer.toString(idCounter++));

            dfd.getNodes()
                    .add(external);
            nodesMap.put(ee.name(), external);
            nodeToLabelNamesMap.put(external, ee.stereotypes());
            nodeToLabelTypeNamesMap.put(external, ee.taggedValues());
        }
    }

    private void createBehavior(DataDictionary dd, LabelType stereotype) {
        for (Node node : nodesMap.values()) {
            var behaviour = ddFactory.createBehavior();
            behaviour.setId(Integer.toString(idCounter++));
            node.setBehavior(behaviour);

            var assignment = ddFactory.createAssignment();
            assignment.setId(Integer.toString(idCounter++));

            assignment.getOutputLabels()
                    .addAll(createLabels(nodeToLabelNamesMap.get(node), dd, stereotype));

            behaviour.getAssignment()
                    .add(assignment);

            node.getProperties()
                    .addAll(assignment.getOutputLabels());

            node.getProperties()
                    .addAll(createTaggedValueLabels(nodeToLabelTypeNamesMap.get(node), dd));

            dd.getBehavior()
                    .add(behaviour);
        }
    }

    private void createFlows(MicroSecEnd micro, DataFlowDiagram dfd, DataDictionary dd, LabelType stereotype) {
        for (InformationFlow iflow : micro.informationFlows()) {
            var source = nodesMap.get(iflow.sender());
            var dest = nodesMap.get(iflow.receiver());

            var flow = dfdFactory.createFlow();
            flow.setSourceNode(source);
            flow.setDestinationNode(dest);
            flow.setEntityName(iflow.sender());

            var inPin = dest.getBehavior()
                    .getInPin()
                    .stream()
                    .findFirst()
                    .orElseGet(() -> createInPin(dest));

            var outPin = ddFactory.createPin();
            outPin.setId(Integer.toString(idCounter++));
            source.getBehavior()
                    .getOutPin()
                    .add(outPin);

            flow.setDestinationPin(inPin);
            flow.setSourcePin(outPin);
            flow.setId(Integer.toString(idCounter++));
            dfd.getFlows()
                    .add(flow);

            List<Label> flowLabels = new ArrayList<>();
            flowLabels.addAll(createLabels(iflow.stereotypes(), dd, stereotype));
            flowLabels.addAll(createTaggedValueLabels(iflow.taggedValues(), dd));
            outpinToFlowLabelMap.put(outPin, flowLabels);
        }
    }

    private Pin createInPin(Node dest) {
        var inPin = ddFactory.createPin();
        inPin.setId(Integer.toString(idCounter++));
        dest.getBehavior()
                .getInPin()
                .add(inPin);
        return inPin;
    }

    private void createNodeAssignments() {
        for (Node node : nodesMap.values()) {
            var behaviour = node.getBehavior();
            Assignment template = (Assignment) behaviour.getAssignment()
                    .get(0);
            if (!behaviour.getOutPin()
                    .isEmpty()) {
                for (Pin outPin : behaviour.getOutPin()) {
                    Assignment assignment = ddFactory.createAssignment();
                    assignment.setId(Integer.toString(idCounter++));

                    assignment.getInputPins()
                            .addAll(behaviour.getInPin());
                    assignment.setOutputPin(outPin);

                    assignment.getOutputLabels()
                            .addAll(template.getOutputLabels());
                    assignment.getOutputLabels()
                            .addAll(outpinToFlowLabelMap.get(outPin));
                    var ddTrue = ddFactory.createTRUE();
                    ddTrue.setId(Integer.toString(idCounter++));
                    assignment.setTerm(ddTrue);

                    behaviour.getAssignment()
                            .add(assignment);
                }
            }
            behaviour.getAssignment()
                    .remove(template);
        }
    }

    private void createForwardingAssignments() {
        for (Node node : nodesMap.values()) {
            var behaviour = node.getBehavior();
            if (!behaviour.getInPin()
                    .isEmpty()) {
                for (Pin pin : behaviour.getOutPin()) {
                    var assignment = ddFactory.createForwardingAssignment();
                    assignment.setId(Integer.toString(idCounter++));
                    assignment.setOutputPin(pin);
                    assignment.getInputPins()
                            .addAll(behaviour.getInPin());
                    behaviour.getAssignment()
                            .add(assignment);
                }
            }
        }
    }

    private List<Label> createLabels(List<String> labelNames, DataDictionary dd, LabelType labelType) {
        List<Label> labels = new ArrayList<>();
        var labelTypeName = labelType.getEntityName();
        for (String labelName : labelNames) {
            if (labelMap.get(labelTypeName)
                    .containsKey(labelName)) {
                labels.add(labelMap.get(labelTypeName)
                        .get(labelName));
            } else {
                Label label = ddFactory.createLabel();
                label.setEntityName(labelName);
                label.setId(Integer.toString(idCounter++));
                labelType.getLabel()
                        .add(label);
                labels.add(label);
                labelMap.get(labelTypeName)
                        .put(labelName, label);
            }
        }
        return labels;
    }

    private List<Label> createTaggedValueLabels(Map<String, List<String>> taggedValues, DataDictionary dd) {
        List<Label> labels = new ArrayList<>();
        for (String labelTypeName : taggedValues.keySet()) {
            var labelNames = taggedValues.get(labelTypeName);
            LabelType labelType;
            if (labelTypeMap.containsKey(labelTypeName)) {
                labelType = labelTypeMap.get(labelTypeName);
            } else {
                labelType = ddFactory.createLabelType();
                labelType.setEntityName(labelTypeName);
                labelType.setId(Integer.toString(idCounter++));
                dd.getLabelTypes()
                        .add(labelType);
                labelTypeMap.put(labelTypeName, labelType);
                labelMap.put(labelTypeName, new HashMap<>());
            }
            labels.addAll(createLabels(labelNames, dd, labelType));
        }
        return labels;
    }
}
