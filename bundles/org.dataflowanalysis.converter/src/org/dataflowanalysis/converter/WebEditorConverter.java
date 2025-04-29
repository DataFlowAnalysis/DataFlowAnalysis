package org.dataflowanalysis.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.dataflowanalysis.converter.webdfd.Child;
import org.dataflowanalysis.converter.webdfd.Port;
import org.dataflowanalysis.converter.webdfd.Value;
import org.dataflowanalysis.converter.webdfd.WebEditorDfd;
import org.dataflowanalysis.converter.webdfd.WebEditorLabelType;
import org.dataflowanalysis.dfd.datadictionary.AbstractAssignment;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.dataflowanalysis.dfd.dataflowdiagram.dataflowdiagramFactory;

public class WebEditorConverter extends Converter {

    private final dataflowdiagramFactory dfdFactory;
    private final datadictionaryFactory ddFactory;
    private Map<String, Node> idToNodeMap;

    private final Logger logger = Logger.getLogger(WebEditorConverter.class);
    protected final static String DELIMITER_PIN_NAME = "|";
    protected final static String DELIMITER_MULTI_PIN = ",";

    private BehaviorConverter behaviorConverter;

    public WebEditorConverter() {
        dfdFactory = dataflowdiagramFactory.eINSTANCE;
        ddFactory = datadictionaryFactory.eINSTANCE;
    }

    /**
     * Converts a Web Editor DFD format file into a DataFlowDiagramAndDictionary object.
     * @param inputFile The path of the input file in Web Editor DFD format.
     * @return DataFlowDiagramAndDictionary object representing the converted data flow diagram and dictionary.
     */
    public DataFlowDiagramAndDictionary webToDfd(String inputFile) {
        return webToDfd(loadWeb(inputFile).get());
    }

    /**
     * Converts a WebEditorDfd object into a DataFlowDiagramAndDictionary object.
     * @param inputFile The WebEditorDfd object to convert.
     * @return DataFlowDiagramAndDictionary object representing the converted data flow diagram and dictionary.
     */
    public DataFlowDiagramAndDictionary webToDfd(WebEditorDfd inputFile) {
        return processWeb(inputFile);
    }

    /**
     * Loads a WebEditorDfd object from a specified input file.
     * @param inputFile The path of the input file.
     * @return Optional containing the loaded WebEditorDfd object; empty if an error occurs.
     */
    public Optional<WebEditorDfd> loadWeb(String inputFile) {
        objectMapper = new ObjectMapper();
        file = new File(inputFile);
        try {
            WebEditorDfd result = objectMapper.readValue(file, WebEditorDfd.class);
            return Optional.ofNullable(result); // This will never be null given readValue's behavior, but it's a safe usage pattern.
        } catch (IOException e) {
            logger.error("Could not load web dfd:", e);
            return Optional.empty();
        }
    }

    private DataFlowDiagramAndDictionary processWeb(WebEditorDfd webdfd) {
        idToNodeMap = new HashMap<>();
        Map<String, Node> pinToNodeMap = new HashMap<>();
        Map<String, Pin> idToPinMap = new HashMap<>();
        Map<String, Label> idToLabelMap = new HashMap<>();
        Map<Node, Map<Pin, String>> nodeOutpinBehaviorMap = new HashMap<>();

        DataFlowDiagram dataFlowDiagram = dfdFactory.createDataFlowDiagram();
        DataDictionary dataDictionary = ddFactory.createDataDictionary();

        behaviorConverter = new BehaviorConverter(dataDictionary);

        createLabelTypes(webdfd, idToLabelMap, dataDictionary);

        createNodes(webdfd, pinToNodeMap, idToPinMap, idToLabelMap, nodeOutpinBehaviorMap, dataFlowDiagram, dataDictionary);

        createFlows(webdfd, pinToNodeMap, idToPinMap, dataFlowDiagram);

        List<Node> nodesInBehavior = nodeOutpinBehaviorMap.keySet()
                .stream()
                .collect(Collectors.toList());

        nodesInBehavior.forEach(node -> {
            Map<Pin, String> outpinBehaviors = nodeOutpinBehaviorMap.get(node);
            outpinBehaviors.forEach((outpin, behavior) -> {
                parseBehavior(node, outpin, behavior, dataFlowDiagram, dataDictionary);
            });
        });

        return new DataFlowDiagramAndDictionary(dataFlowDiagram, dataDictionary);
    }

    private void createNodes(WebEditorDfd webdfd, Map<String, Node> pinToNodeMap, Map<String, Pin> pinMap, Map<String, Label> idToLabelMap,
            Map<Node, Map<Pin, String>> nodeOutpinBehavior, DataFlowDiagram dataFlowDiagram, DataDictionary dataDictionary) {
        for (Child child : webdfd.model()
                .children()) {
            String[] type = child.type()
                    .split(":");
            String name = child.text();

            if (type[0].equals("node")) {
                Optional<Node> nodeOptional = switch (type[1]) {
                    case "function" -> Optional.of(dfdFactory.createProcess());
                    case "storage" -> Optional.of(dfdFactory.createStore());
                    case "input-output" -> Optional.of(dfdFactory.createExternal());
                    default -> {
                        logger.error("Unrecognized node type: " + type[1]);
                        yield Optional.empty();
                    }
                };
                Node node = nodeOptional.get();

                node.setEntityName(name);
                node.setId(child.id());

                var behaviour = ddFactory.createBehavior();
                behaviour.setEntityName(name);
                node.setBehavior(behaviour);
                dataDictionary.getBehavior()
                        .add(behaviour);

                createPins(pinToNodeMap, pinMap, nodeOutpinBehavior, child, node);

                List<Label> labelsAtNode = child.labels()
                        .stream()
                        .map(it -> idToLabelMap.get(it.labelTypeValueId()))
                        .toList();
                node.getProperties()
                        .addAll(labelsAtNode);

                dataFlowDiagram.getNodes()
                        .add(node);
                idToNodeMap.put(child.id(), node);
            }
        }
    }

    private void createFlows(WebEditorDfd webdfd, Map<String, Node> pinToNodeMap, Map<String, Pin> pinMap, DataFlowDiagram dataFlowDiagram) {

        webdfd.model()
                .children()
                .stream()
                .filter(child -> child.type()
                        .contains("edge:"))
                .forEach(child -> {
                    var source = pinToNodeMap.get(child.sourceId());
                    var dest = pinToNodeMap.get(child.targetId());

                    var flow = dfdFactory.createFlow();
                    flow.setSourceNode(source);
                    flow.setDestinationNode(dest);
                    flow.setEntityName(child.text());

                    var destPin = pinMap.get(child.targetId());
                    var sourcePin = pinMap.get(child.sourceId());

                    destPin.setEntityName(destPin.getEntityName() + child.text());
                    sourcePin.setEntityName(sourcePin.getEntityName() + child.text());

                    flow.setDestinationPin(destPin);
                    flow.setSourcePin(sourcePin);
                    flow.setId(child.id());
                    dataFlowDiagram.getFlows()
                            .add(flow);
                });

    }

    private void createPins(Map<String, Node> pinToNodeMap, Map<String, Pin> pinMap, Map<Node, Map<Pin, String>> nodeOutpinBehavior, Child child,
            Node node) {
        for (Port port : child.ports()) {
            switch (port.type()) {
                case "port:dfd-input" -> pinMap.put(port.id(), createInPin(node, port));
                case "port:dfd-output" -> pinMap.put(port.id(), createOutPin(nodeOutpinBehavior, node, port));
                default -> logger.error("Unrecognized port type");
            }
            pinToNodeMap.put(port.id(), node);
        }
    }

    private Pin createOutPin(Map<Node, Map<Pin, String>> nodeOutpinBehavior, Node node, Port port) {
        var outPin = ddFactory.createPin();
        outPin.setId(port.id());
        outPin.setEntityName(node.getEntityName() + "_out_");
        node.getBehavior()
                .getOutPin()
                .add(outPin);
        if (port.behavior() != null) {
            putValue(nodeOutpinBehavior, node, outPin, port.behavior());
        }
        return outPin;
    }

    private Pin createInPin(Node node, Port port) {
        var inPin = ddFactory.createPin();
        inPin.setId(port.id());
        inPin.setEntityName(node.getEntityName() + "_in_");
        node.getBehavior()
                .getInPin()
                .add(inPin);
        return inPin;
    }

    private void createLabelTypes(WebEditorDfd webdfd, Map<String, Label> idToLabelMap, DataDictionary dataDictionary) {
        for (WebEditorLabelType webLabelType : webdfd.labelTypes()) {
            LabelType labelType = ddFactory.createLabelType();
            labelType.setEntityName(webLabelType.name());
            labelType.setId(webLabelType.id());
            for (Value value : webLabelType.values()) {
                createLabel(idToLabelMap, labelType, value);
            }
            dataDictionary.getLabelTypes()
                    .add(labelType);
        }
    }

    private void createLabel(Map<String, Label> idToLabelMap, LabelType labelType, Value value) {
        Label label = ddFactory.createLabel();
        label.setEntityName(value.text());
        label.setId(value.id());
        labelType.getLabel()
                .add(label);
        idToLabelMap.put(label.getId(), label);
    }

    private void parseBehavior(Node node, Pin outpin, String lines, DataFlowDiagram dfd, DataDictionary dd) {
        String[] behaviorStrings = lines.split("\n");
        var behavior = node.getBehavior();
        for (String behaviorString : behaviorStrings) {
            AbstractAssignment abstractAssignment;
            try {
            	if (behaviorString.startsWith("forward")) {
                    var assignment = ddFactory.createForwardingAssignment();
                    var inPins = getInPinsFromString(behaviorString.replaceFirst("forward ", "").trim(), node, dfd);
                    assignment.getInputPins()
                            .addAll(inPins);
                    abstractAssignment = assignment;
                } else if (behaviorString.startsWith("set")) {
                    var assignment = ddFactory.createSetAssignment();
                    var outLabels = getLabelFromString(behaviorString.replaceFirst("set ", "").trim(), dd);
                    assignment.getOutputLabels()
                            .addAll(outLabels);
                    abstractAssignment = assignment;
                } else if (behaviorString.startsWith("unset")) {
                    var assignment = ddFactory.createUnsetAssignment();
                    var outLabels = getLabelFromString(behaviorString.replaceFirst("unset ", "").trim(), dd);
                    assignment.getOutputLabels()
                            .addAll(outLabels);
                    abstractAssignment = assignment;
                } else if (behaviorString.contains("assign")) {
                    var assignment = ddFactory.createAssignment();
                    var outLabels = getLabelFromString(behaviorString.replaceFirst("assign", "")
                            .split(" if ")[0].trim(), dd);
                    var remainder = behaviorString.replaceFirst("assign", "")
                            .split(" if ")[1].trim();
                    if (remainder.contains(" from ")) {
                        var inputPins = getInPinsFromString(remainder.split(" from ")[1].trim(), node, dfd);
                        assignment.getInputPins()
                                .addAll(inputPins);
                        remainder = remainder.split(" from ")[0].trim();
                    }
                    var term = behaviorConverter.stringToTerm(remainder);
                    assignment.setTerm(term);
                    assignment.getOutputLabels()
                            .addAll(outLabels);
                    abstractAssignment = assignment;
                } else {
                    logger.error("Unrecognized assignment: " + behaviorString);
                    continue;
                }
                abstractAssignment.setOutputPin(outpin);
                behavior.getAssignment()
                        .add(abstractAssignment);
            } catch (ArrayIndexOutOfBoundsException e) {
                logger.error("Assignment string is invalid: " + behaviorString);
            }
        }

    }

    private List<Label> getLabelFromString(String string, DataDictionary dd) {
        var labels = new ArrayList<Label>();
        Arrays.asList(string.split(","))
                .forEach(typeValuePair -> {
                    if (typeValuePair == null || typeValuePair.trim()
                            .equals(""))
                        return;
                    String typeName = typeValuePair.split("\\.")[0];
                    String valueName = typeValuePair.split("\\.")[1];

                    Label value = dd.getLabelTypes()
                            .stream()
                            .filter(labelType -> labelType.getEntityName()
                                    .equals(typeName))
                            .flatMap(labelType -> labelType.getLabel()
                                    .stream())
                            .filter(label -> label.getEntityName()
                                    .equals(valueName))
                            .findAny()
                            .orElse(null);
                    labels.add(value);
                });
        return labels;
    }

    private void putValue(Map<Node, Map<Pin, String>> nestedHashMap, Node node, Pin pin, String value) {
        nestedHashMap.computeIfAbsent(node, k -> new HashMap<>())
                .put(pin, value);
    }

    private List<Pin> getInPinsFromString(String pinString, Node node, DataFlowDiagram dfd) {
        List<Pin> inPins = new ArrayList<>();
        List<String> pinNames = Arrays.asList(pinString.split(DELIMITER_MULTI_PIN + "\\s*"));

        List<Flow> flowsToNode = dfd.getFlows()
                .stream()
                .filter(flow -> flow.getDestinationNode() == node)
                .toList();

        Map<Pin, List<String>> pinToFlowNames = new HashMap<>();
        for (var flow : flowsToNode) {
            fillPinToFlowNamesMap(pinToFlowNames, flow);
        }

        pinNames.forEach(pinName -> {
            List<String> incomingFlowNames = Arrays.asList(pinName.split(Pattern.quote(DELIMITER_PIN_NAME)));
            pinToFlowNames.keySet()
                    .forEach(key -> {
                        if (pinToFlowNames.get(key)
                                .containsAll(incomingFlowNames))
                            inPins.add(key);
                    });
        });

        return inPins;
    }

    private void fillPinToFlowNamesMap(Map<Pin, List<String>> map, Flow flow) {
        if (map.containsKey(flow.getDestinationPin())) {
            map.get(flow.getDestinationPin())
                    .add(flow.getEntityName());
        } else {
            List<String> flowNames = new ArrayList<>();
            flowNames.add(flow.getEntityName());
            map.put(flow.getDestinationPin(), flowNames);
        }
    }

}
