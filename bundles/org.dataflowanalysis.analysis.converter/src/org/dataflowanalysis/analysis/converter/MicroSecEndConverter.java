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

/**
 * Converts MicroSecEnd models to the data flow diagram and dictionary representation. Inherits from {@link Converter}
 * to utilize shared conversion logic while providing specific functionality for handling MicroSecEnd models.
 */
public class MicroSecEndConverter extends Converter {
    private final dataflowdiagramFactory dfdFactory;
    private final datadictionaryFactory ddFactory;

    private final Map<String, Node> nodesMap;
    private final Map<Node, List<String>> nodeToLabelNames;
    private final Map<Node, Map<String, List<String>>> nodeToLabelTypeNames;
    private final Map<String,Map<String, Label>> labelMap;
    private final Map<String, LabelType> labelTypeMap;

    public MicroSecEndConverter() {
        dfdFactory = dataflowdiagramFactory.eINSTANCE;
        ddFactory = datadictionaryFactory.eINSTANCE;

        nodesMap = new HashMap<>();
        nodeToLabelNames = new HashMap<>();
        labelMap = new HashMap<>();
        labelTypeMap = new HashMap<>();
        nodeToLabelTypeNames = new HashMap<>();
    }

    /**
     * Converts MicroSecEnd model to DataFlowDiagramAndDictionary.
     * @param inputFile File path of the MicroSecEnd model.
     * @return DataFlowDiagramAndDictionary representation.
     */
    public DataFlowDiagramAndDictionary microToDfd(String inputFile) {
        return microToDfd(loadMicro(inputFile).get());
    }

    /**
     * Converts MicroSecEnd model to DataFlowDiagramAndDictionary.
     * @param inputFile MicroSecEnd object to be converted.
     * @return DataFlowDiagramAndDictionary representation.
     */
    public DataFlowDiagramAndDictionary microToDfd(MicroSecEnd inputFile) {
        return processMicro(inputFile);
    }

    /**
     * Deserializes MicroSecEnd file into an object.
     * @param inputFile File path containing MicroSecEnd model.
     * @return Optional of MicroSecEnd if deserialization is successful, otherwise empty.
     */
    public Optional<MicroSecEnd> loadMicro(String inputFile) {
        objectMapper = new ObjectMapper();
        file = new File(inputFile);
        try {
            MicroSecEnd result = objectMapper.readValue(file, MicroSecEnd.class);
            return Optional.ofNullable(result);
        } catch (IOException e) {
            logger.error("Could not load MicroSecEnd:", e);
            return Optional.empty();
        }
    }

    /**
     * Converts PlantUML file to DataFlowDiagramAndDictionary via Python script.
     * @param inputFile Path to PlantUML file.
     * @return Optional of DataFlowDiagramAndDictionary if successful, otherwise empty.
     */
    public Optional<DataFlowDiagramAndDictionary> plantToDFD(String inputFile) {
        String name = inputFile.split("\\.")[0];
        int exitCode = runPythonScript(inputFile, "json", name + ".json");
        if (exitCode == 0) {
            return Optional.ofNullable(microToDfd(name + ".json"));
        } else {
            return Optional.empty();
        }

    }

    // Tested with Python *3.11.5*, requires *argparse*, *ast* and *json* modules
    /**
     * Runs Python script for model conversion.
     * @param in Input file path.
     * @param format Desired output format.
     * @param out Output file path.
     * @return Exit code of the process (0 for success, -1 for error).
     */
    public int runPythonScript(String in, String format, String out) {
        String[] command = {"python3", "convert_model.py", in, format, "-op", out};

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process;
        try {
            process = processBuilder.start();
            return process.waitFor();
        } catch (IOException | InterruptedException e) {
            logger.error("Make sure python3 is installed and set in PATH", e);
        }
        return -1;
    }

    private DataFlowDiagramAndDictionary processMicro(MicroSecEnd micro) {
        DataFlowDiagram dfd = dfdFactory.createDataFlowDiagram();
        DataDictionary dd = ddFactory.createDataDictionary();

        createExternalEntities(micro, dfd);

        createProcesses(micro, dfd);

        LabelType stereotype = ddFactory.createLabelType();
        stereotype.setEntityName("Stereotype");
        dd.getLabelTypes().add(stereotype);
        labelTypeMap.put(stereotype.getEntityName(),stereotype);
        labelMap.put(stereotype.getEntityName(), new HashMap<>());

        createBehavior(dd, stereotype);

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
            nodeToLabelTypeNames.put(process, service.taggedValues());
        }
    }

    private void createExternalEntities(MicroSecEnd micro, DataFlowDiagram dfd) {
        for (ExternalEntity ee : micro.externalEntities()) {
            var external = dfdFactory.createExternal();
            external.setEntityName(ee.name());

            dfd.getNodes().add(external);
            nodesMap.put(ee.name(), external);
            nodeToLabelNames.put(external, ee.stereotypes());
            nodeToLabelTypeNames.put(external, ee.taggedValues());
        }
    }

    private void createBehavior(DataDictionary dd, LabelType stereotype) {
        for (Node node : nodesMap.values()) {
            var behaviour = ddFactory.createBehaviour();
            node.setBehaviour(behaviour);

            var assignment = ddFactory.createAssignment();

            assignment.getOutputLabels().addAll(createStereotypeLabels(nodeToLabelNames.get(node), dd, stereotype));

            behaviour.getAssignment().add(assignment);

            node.getProperties().addAll(assignment.getOutputLabels());
            
            node.getProperties().addAll(createTaggedValueLabels(nodeToLabelTypeNames.get(node), dd));
                        
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

    private List<Label> createStereotypeLabels(List<String> labelNames, DataDictionary dd, LabelType stereotype) {
        List<Label> labels = new ArrayList<>();
        var stereotypeName=stereotype.getEntityName();
        for (String labelName : labelNames) {
            if (labelMap.get(stereotypeName).containsKey(labelName)) {
                labels.add(labelMap.get(stereotypeName).get(labelName));
            } else {
                Label label = ddFactory.createLabel();
                label.setEntityName(labelName);
                stereotype.getLabel().add(label);
                labels.add(label);
                labelMap.get(stereotype.getEntityName()).put(labelName, label);
            }
        }
        return labels;
    }
    private List<Label> createTaggedValueLabels(Map<String, List<String>> taggedValues, DataDictionary dd) {
        List<Label> labels = new ArrayList<>();
        for(String labelTypeName : taggedValues.keySet()) {
            var labelNames = taggedValues.get(labelTypeName);
            LabelType labelType;
            if (labelTypeMap.containsKey(labelTypeName)) {
                labelType=labelTypeMap.get(labelTypeName);
            }
            else {
                labelType = ddFactory.createLabelType();
                labelType.setEntityName(labelTypeName);
                dd.getLabelTypes().add(labelType);
                labelTypeMap.put(labelTypeName, labelType);
                labelMap.put(labelTypeName,new HashMap<>());
            }
            for (String labelName : labelNames) {
                if (labelMap.get(labelTypeName).containsKey(labelName)) {
                    labels.add(labelMap.get(labelTypeName).get(labelName));
                } else {
                    Label label = ddFactory.createLabel();
                    label.setEntityName(labelName);
                    labelType.getLabel().add(label);
                    labels.add(label);
                    labelMap.get(labelTypeName).put(labelName, label);
                }
            }
        }
        return labels;
    }

}
