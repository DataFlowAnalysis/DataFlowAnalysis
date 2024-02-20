package org.dataflowanalysis.analysis.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.converter.microsecend.*;
import org.dataflowanalysis.dfd.datadictionary.*;
import org.dataflowanalysis.dfd.dataflowdiagram.*;

public class MicroSecEndProcessor {
    private final dataflowdiagramFactory dfdFactory;
    private final datadictionaryFactory ddFactory;

    private Map<String, Node> nodesMap;
    private final Map<Node, List<String>> nodeToLabelNames;
    private final Map<String, Label> labelMap;


    public MicroSecEndProcessor() {
        dfdFactory = dataflowdiagramFactory.eINSTANCE;
        ddFactory = datadictionaryFactory.eINSTANCE;

        nodesMap = new HashMap<>();
        nodeToLabelNames = new HashMap<>();
        labelMap = new HashMap<>();
    }

    public DataFlowDiagramAndDictionary processMicro(MicroSecEnd micro) {
        DataFlowDiagram dfd = dfdFactory.createDataFlowDiagram();
        DataDictionary dd = ddFactory.createDataDictionary();

        for (ExternalEntity ee : micro.externalEntities()) {
            var external = dfdFactory.createExternal();
            external.setEntityName(ee.name());

            dfd.getNodes().add(external);
            nodesMap.put(ee.name(), external);
            nodeToLabelNames.put(external, ee.stereotypes());
        }

        for (Service service : micro.services()) {
            var process = dfdFactory.createProcess();
            process.setEntityName(service.name());

            dfd.getNodes().add(process);
            nodesMap.put(service.name(), process);
            nodeToLabelNames.put(process, service.stereotypes());
        }

        LabelType annotation = ddFactory.createLabelType();
        annotation.setEntityName("annotation");
        dd.getLabelTypes().add(annotation);

        for (Node node : nodesMap.values()) {
            var behaviour = ddFactory.createBehaviour();
            node.setBehaviour(behaviour);

            var assignment = ddFactory.createAssignment();

            assignment.getOutputLabels().addAll(createLabels(nodeToLabelNames.get(node), dd, annotation));

            behaviour.getAssignment().add(assignment);

            node.getProperties().addAll(assignment.getOutputLabels());

            dd.getBehaviour().add(behaviour);
        }

        for (InformationFlow iflow : micro.informationFlows()) {
            var source = nodesMap.get(iflow.sender());
            var dest = nodesMap.get(iflow.receiver());

            var flow = dfdFactory.createFlow();
            flow.setSourceNode(source);
            flow.setDestinationNode(dest);
            flow.setEntityName(iflow.sender());

            var inPin = ddFactory.createPin();
            var outPin = ddFactory.createPin();
            source.getBehaviour().getOutPin().add(outPin);
            dest.getBehaviour().getInPin().add(inPin);

            flow.setDestinationPin(inPin);
            flow.setSourcePin(outPin);
            dfd.getFlows().add(flow);
        }

        // NodeAssigment
        for (Node node : nodesMap.values()) {
            var behaviour = node.getBehaviour();
            Assignment template = (Assignment) behaviour.getAssignment().get(0);
            if (!behaviour.getOutPin().isEmpty()) {
                for (Pin outPin : behaviour.getOutPin()) {
                    Assignment assignment = ddFactory.createAssignment();

                    assignment.getInputPins().addAll(behaviour.getInPin());
                    assignment.setOutputPin(outPin);

                    assignment.getOutputLabels().addAll(template.getOutputLabels());
                    assignment.setTerm(ddFactory.createTRUE());

                    behaviour.getAssignment().add(assignment);
                }

                behaviour.getAssignment().remove(template);
            }
        }

        // ForwardAssignment
        for (Node node : nodesMap.values()) {
            var behaviour = node.getBehaviour();
            for (Pin pin : behaviour.getOutPin()) {
                var assignment = ddFactory.createForwardingAssignment();
                assignment.setOutputPin(pin);
                assignment.getInputPins().addAll(behaviour.getInPin());
                behaviour.getAssignment().add(assignment);
            }
        }

        return new DataFlowDiagramAndDictionary(dfd, dd);
    }

    private List<Label> createLabels(List<String> labelNames, DataDictionary dd, LabelType annotation) {
        List<Label> labels = new ArrayList<>();
        for (String labelName : labelNames) {
            if (labelMap.containsKey(labelName)) {
                labels.add(labelMap.get(labelName));
            } else {
                Label label = ddFactory.createLabel();
                label.setEntityName(labelName);
                annotation.getLabel().add(label);
                labels.add(label);
                labelMap.put(labelName, label);
            }
        }
        return labels;
    }
}
