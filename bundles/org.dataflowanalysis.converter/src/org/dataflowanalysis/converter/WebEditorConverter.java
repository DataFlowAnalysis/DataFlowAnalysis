package org.dataflowanalysis.converter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.dataflowanalysis.converter.webdfd.Child;
import org.dataflowanalysis.converter.webdfd.Port;
import org.dataflowanalysis.converter.webdfd.Value;
import org.dataflowanalysis.converter.webdfd.WebEditorDfd;
import org.dataflowanalysis.converter.webdfd.WebEditorLabelType;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.dataflowanalysis.dfd.dataflowdiagram.dataflowdiagramFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WebEditorConverter extends Converter{
    
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
                Node node = switch (type[1]) {
                    case "function" -> dfdFactory.createProcess();
                    case "storage" -> dfdFactory.createStore();
                    case "input-output" -> dfdFactory.createExternal();
                    default -> {
                        logger.error("Unrecognized node type: " + type[1]);
                        continue;
                    }
                };

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
            behaviorString = behaviorString.replace(" ", "");
            if (behaviorString.contains("Forwarding")) {
                 var assignment = ddFactory.createForwardingAssignment();
                 assignment.setOutputPin(outpin);
                 
                 String regex = "^Forwarding\\(\\{([^}]*)\\}\\)$";
                 Pattern pattern = Pattern.compile(regex);
                 Matcher matcher = pattern.matcher(behaviorString);
                 if (!matcher.matches()) { 
                     logger.error("Invalid behavior string:");
                     logger.error(behaviorString);
                     continue;
                 }            
                 
                 assignment.getInputPins().addAll(getInPinsFromString(matcher.group(1), node, dfd));
               
                behavior.getAssignment()
                        .add(assignment);
            } else if (behaviorString.contains("Assignment")) {             
                String regex = "^Assignment\\(\\{([^}]*)\\};([^;]+);\\{([^}]*)\\}\\)$";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(behaviorString);
                if (!matcher.matches()) {
                    logger.error("Invalid behavior string:");
                    logger.error(behaviorString);
                    continue;
                }              
                
                String inPinsAsString = matcher.group(1);
                String term = matcher.group(2); 
                String outLabelsAsString = matcher.group(3);
                
                Assignment assignment = ddFactory.createAssignment();
                assignment.setOutputPin(outpin);
                assignment.setTerm(behaviorConverter.stringToTerm(term));
                assignment.getInputPins().addAll(getInPinsFromString(inPinsAsString, node, dfd));
                Arrays.asList(outLabelsAsString.split(",")).forEach(typeValuePair -> {
                	if (typeValuePair == null || typeValuePair.trim().equals("")) return;
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
                     assignment.getOutputLabels()
                     .add(value);
                });
              
                behavior.getAssignment()
                        .add(assignment);
            }
        }
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

         Map<Pin,List<String>>pinToFlowNames = new HashMap<>();
         for (var flow : flowsToNode) {
             fillPinToFlowNamesMap(pinToFlowNames,flow);  
         }
         
         pinNames.forEach(pinName -> {
             List<String> incomingFlowNames = Arrays.asList(pinName.split(Pattern.quote(DELIMITER_PIN_NAME)));
             pinToFlowNames.keySet().forEach(key -> {
                if (pinToFlowNames.get(key).containsAll(incomingFlowNames)) inPins.add(key);
             });
         });        
        
        return inPins;
    }
    
    private void fillPinToFlowNamesMap(Map<Pin,List<String>> map,Flow flow) {
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
