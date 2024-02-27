package org.dataflowanalysis.analysis.converter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.lang.Process;
import org.dataflowanalysis.analysis.converter.microsecend.*;
import org.dataflowanalysis.dfd.datadictionary.*;
import org.dataflowanalysis.dfd.dataflowdiagram.*;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MicroSecEndConverter extends Converter {
    private final dataflowdiagramFactory dfdFactory;
    private final datadictionaryFactory ddFactory;

    private final Map<String, Node> nodesMap;
    private final Map<Node, List<String>> nodeToLabelNames;
    private final Map<String, Label> labelMap;

    public MicroSecEndConverter() {
        dfdFactory = dataflowdiagramFactory.eINSTANCE;
        ddFactory = datadictionaryFactory.eINSTANCE;

        nodesMap = new HashMap<>();
        nodeToLabelNames = new HashMap<>();
        labelMap = new HashMap<>();
    }

    private DataFlowDiagramAndDictionary processMicro(MicroSecEnd micro) {
        DataFlowDiagram dfd = dfdFactory.createDataFlowDiagram();
        DataDictionary dd = ddFactory.createDataDictionary();

        createExternalEntities(micro, dfd);

        createProcesses(micro, dfd);

        LabelType annotation = ddFactory.createLabelType();
        annotation.setEntityName("annotation");
        dd.getLabelTypes().add(annotation);

        createBehavior(dd, annotation);

        createFlows(micro, dfd);

        createNodeAssignments();

        createForwardingAssignments();

        return new DataFlowDiagramAndDictionary(dfd, dd);
    }

    private void createProcesses(MicroSecEnd micro, DataFlowDiagram dfd) {
        for (Service service : micro.services()) {
            var process = dfdFactory.createProcess();
            process.setEntityName(service.name());

            dfd.getNodes().add(process);
            nodesMap.put(service.name(), process);
            nodeToLabelNames.put(process, service.stereotypes());
        }
    }

    private void createExternalEntities(MicroSecEnd micro, DataFlowDiagram dfd) {
        for (ExternalEntity ee : micro.externalEntities()) {
            var external = dfdFactory.createExternal();
            external.setEntityName(ee.name());

            dfd.getNodes().add(external);
            nodesMap.put(ee.name(), external);
            nodeToLabelNames.put(external, ee.stereotypes());
        }
    }

    private void createBehavior(DataDictionary dd, LabelType annotation) {
        for (Node node : nodesMap.values()) {
            var behaviour = ddFactory.createBehaviour();
            node.setBehaviour(behaviour);

            var assignment = ddFactory.createAssignment();

            assignment.getOutputLabels().addAll(createLabels(nodeToLabelNames.get(node), dd, annotation));

            behaviour.getAssignment().add(assignment);

            node.getProperties().addAll(assignment.getOutputLabels());

            dd.getBehaviour().add(behaviour);
        }
    }

    private void createFlows(MicroSecEnd micro, DataFlowDiagram dfd) {
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
    }

    private void createNodeAssignments() {
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
    }

    private void createForwardingAssignments() {
        for (Node node : nodesMap.values()) {
            var behaviour = node.getBehaviour();
            for (Pin pin : behaviour.getOutPin()) {
                var assignment = ddFactory.createForwardingAssignment();
                assignment.setOutputPin(pin);
                assignment.getInputPins().addAll(behaviour.getInPin());
                behaviour.getAssignment().add(assignment);
            }
        }
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

    public DataFlowDiagramAndDictionary microToDfd(String inputFile) {
        return microToDfd(loadMicro(inputFile).get());
    }

    public DataFlowDiagramAndDictionary microToDfd(MicroSecEnd inputFile) {
        return processMicro(inputFile);
    }

    public Optional<MicroSecEnd> loadMicro(String inputFile) {
        objectMapper = new ObjectMapper();
        file = new File(inputFile);
        try {
            MicroSecEnd result = objectMapper.readValue(file, MicroSecEnd.class);
            return Optional.ofNullable(result); // This will never be null given readValue's behavior, but it's a safe usage pattern.
        } catch (IOException e) {
            logger.error("Could not load MicroSecEnd:", e);
            return Optional.empty();
        }
    }

    public Optional<DataFlowDiagramAndDictionary> plantToDFD(String inputFile) {
        String name = inputFile.split("\\.")[0];
        int exitCode = runPythonScript(inputFile, "json", name + ".json");
        if (exitCode == 0) {
            return Optional.ofNullable(microToDfd(name + ".json"));
        } else {
            logger.error("Make sure python3 is installed and set in PATH");
            return Optional.empty();
        }

    }

    // Tested with Python *3.11.5*, requires *argparse*, *ast* and *json* modules
    public int runPythonScript(String in, String format, String out) {
        String[] command = {"python3", "convert_model.py", in, format, "-op", out};

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process;
        try {
            process = processBuilder.start();
            return process.waitFor();
        } catch (IOException | InterruptedException e) {
            logger.error(e);
        }
        return -1;
    }
}
