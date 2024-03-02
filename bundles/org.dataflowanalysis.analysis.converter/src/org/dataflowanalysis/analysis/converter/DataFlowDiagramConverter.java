package org.dataflowanalysis.analysis.converter;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.converter.webdfd.*;
import org.dataflowanalysis.dfd.datadictionary.*;
import org.dataflowanalysis.dfd.dataflowdiagram.*;
import org.dataflowanalysis.dfd.dataflowdiagram.Process;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Converts data flow diagrams and dictionaries between web editor formats and the application's internal
 * representation. Inherits from {@link Converter} to utilize shared conversion logic while providing specific
 * functionality for handling data flow diagram formats. Supports loading from and storing to files, and conversion
 * between different data representation formats.
 */
public class DataFlowDiagramConverter extends Converter {

    private final Map<Pin, String> inputPinToFlowNameMap = new HashMap<>();
    private final dataflowdiagramFactory dfdFactory;
    private final datadictionaryFactory ddFactory;
    private Map<String, Node> idToNodeMap;

    private final Logger logger = Logger.getLogger(DataFlowDiagramConverter.class);

    private BehaviorConverter behaviorConverter;

    public DataFlowDiagramConverter() {
        dfdFactory = dataflowdiagramFactory.eINSTANCE;
        ddFactory = datadictionaryFactory.eINSTANCE;

        idToNodeMap = new HashMap<>();
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

        createWebLabelTypes(webdfd, idToLabelMap, dataDictionary);

        createWebNodes(webdfd, pinToNodeMap, idToPinMap, idToLabelMap, nodeOutpinBehaviorMap, dataFlowDiagram, dataDictionary);

        createWebFlows(webdfd, pinToNodeMap, idToPinMap, dataFlowDiagram);

        List<Node> nodesInBehavior = nodeOutpinBehaviorMap.keySet().stream().collect(Collectors.toList());

        nodesInBehavior.forEach(node -> {
            Map<Pin, String> outpinBehaviors = nodeOutpinBehaviorMap.get(node);
            outpinBehaviors.forEach((outpin, behavior) -> {
                parseBehavior(node, outpin, behavior, dataFlowDiagram, dataDictionary);
            });
        });

        return new DataFlowDiagramAndDictionary(dataFlowDiagram, dataDictionary);
    }

    private void createWebNodes(WebEditorDfd webdfd, Map<String, Node> pinToNodeMap, Map<String, Pin> pinMap, Map<String, Label> idToLabelMap,
            Map<Node, Map<Pin, String>> nodeOutpinBehavior, DataFlowDiagram dataFlowDiagram, DataDictionary dataDictionary) {
        for (Child child : webdfd.model().children()) {
            String[] type = child.type().split(":");
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

                var behaviour = ddFactory.createBehaviour();
                behaviour.setEntityName(name);
                node.setBehaviour(behaviour);
                dataDictionary.getBehaviour().add(behaviour);

                createWebPins(pinToNodeMap, pinMap, nodeOutpinBehavior, child, node);

                List<Label> labelsAtNode = child.labels().stream().map(it -> idToLabelMap.get(it.labelTypeValueId())).toList();
                node.getProperties().addAll(labelsAtNode);

                dataFlowDiagram.getNodes().add(node);
                idToNodeMap.put(child.id(), node);
            }
        }
    }

    private void createWebFlows(WebEditorDfd webdfd, Map<String, Node> pinToNodeMap, Map<String, Pin> pinMap, DataFlowDiagram dataFlowDiagram) {

        webdfd.model().children().stream().filter(child -> child.type().contains("edge:")).forEach(child -> {
            var source = pinToNodeMap.get(child.sourceId());
            var dest = pinToNodeMap.get(child.targetId());

            var flow = dfdFactory.createFlow();
            flow.setSourceNode(source);
            flow.setDestinationNode(dest);
            flow.setEntityName(child.text());
            
            var destPin = pinMap.get(child.targetId());
            var sourcePin = pinMap.get(child.sourceId());
            
            destPin.setEntityName(destPin.getEntityName()+child.text());
            sourcePin.setEntityName(sourcePin.getEntityName()+child.text());
            
            flow.setDestinationPin(destPin);
            flow.setSourcePin(sourcePin);
            flow.setId(child.id());
            dataFlowDiagram.getFlows().add(flow);
        });

    }

    private void createWebPins(Map<String, Node> pinToNodeMap, Map<String, Pin> pinMap, Map<Node, Map<Pin, String>> nodeOutpinBehavior, Child child,
            Node node) {
        for (Port port : child.ports()) {
            switch (port.type()) {
                case "port:dfd-input" -> pinMap.put(port.id(), createWebInPin(node, port));
                case "port:dfd-output" -> pinMap.put(port.id(), createWebOutPin(nodeOutpinBehavior, node, port));
                default -> logger.error("Unrecognized port type");
            }
            pinToNodeMap.put(port.id(), node);
        }
    }

    private Pin createWebOutPin(Map<Node, Map<Pin, String>> nodeOutpinBehavior, Node node, Port port) {
        var outPin = ddFactory.createPin();
        outPin.setId(port.id());
        outPin.setEntityName(node.getEntityName()+"_out_");
        node.getBehaviour().getOutPin().add(outPin);
        if (port.behavior() != null) {
            putValue(nodeOutpinBehavior, node, outPin, port.behavior());
        }
        return outPin;
    }

    private Pin createWebInPin(Node node, Port port) {
        var inPin = ddFactory.createPin();
        inPin.setId(port.id());
        inPin.setEntityName(node.getEntityName()+"_in_");
        node.getBehaviour().getInPin().add(inPin);
        return inPin;
    }

    private void createWebLabelTypes(WebEditorDfd webdfd, Map<String, Label> idToLabelMap, DataDictionary dataDictionary) {
        for (WebEditorLabelType webLabelType : webdfd.labelTypes()) {
            LabelType labelType = ddFactory.createLabelType();
            labelType.setEntityName(webLabelType.name());
            labelType.setId(webLabelType.id());
            for (Value value : webLabelType.values()) {
                createWebLabel(idToLabelMap, labelType, value);
            }
            dataDictionary.getLabelTypes().add(labelType);
        }
    }

    private void createWebLabel(Map<String, Label> idToLabelMap, LabelType labelType, Value value) {
        Label label = ddFactory.createLabel();
        label.setEntityName(value.text());
        label.setId(value.id());
        labelType.getLabel().add(label);
        idToLabelMap.put(label.getId(), label);
    }

    private void createLabelTypesAndValues(List<WebEditorLabelType> labelTypes, DataDictionary dataDictionary) {
        for (LabelType labelType : dataDictionary.getLabelTypes()) {
            List<Value> values = new ArrayList<>();
            for (Label label : labelType.getLabel()) {
                values.add(new Value(label.getId(), label.getEntityName()));
            }
            labelTypes.add(new WebEditorLabelType(labelType.getId(), labelType.getEntityName(), values));
        }
    }

    private WebEditorDfd processDfd(DataFlowDiagram dataFlowDiagram, DataDictionary dataDictionary) {
        List<Child> children = new ArrayList<>();
        List<WebEditorLabelType> labelTypes = new ArrayList<>();

        behaviorConverter = new BehaviorConverter(dataDictionary);

        createLabelTypesAndValues(labelTypes, dataDictionary);

        createFlows(dataFlowDiagram, children);

        createNodes(dataFlowDiagram, children);

        return new WebEditorDfd(new Model("graph", "root", children), labelTypes);
    }

    private void createNodes(DataFlowDiagram dataFlowDiagram, List<Child> children) {
        for (Node node : dataFlowDiagram.getNodes()) {
            String text = node.getEntityName();
            String id = node.getId();
            String type;
            if (node instanceof Process) {
                type = "node:function";
            } else if (node instanceof Store) {
                type = "node:storage";
            } else if (node instanceof External) {
                type = "node:input-output";
            } else {
                type = "error";
                logger.error("Unrecognized node type");
            }

            List<WebEditorLabel> labels = new ArrayList<>();
            for (Label label : node.getProperties()) {
                String labelTypeId = ((LabelType) label.eContainer()).getId();
                String labelId = label.getId();
                labels.add(new WebEditorLabel(labelTypeId, labelId));
            }

            List<Port> ports = new ArrayList<>();

            node.getBehaviour().getInPin().forEach(pin -> ports.add(new Port(null, pin.getId(), "port:dfd-input", new ArrayList<>())));

            Map<Pin, List<AbstractAssignment>> mapPinToAssignments = mapping(node);

            node.getBehaviour().getOutPin().forEach(pin -> ports
                    .add(new Port(createBehaviourString(mapPinToAssignments.get(pin)), pin.getId(), "port:dfd-output", new ArrayList<>())));

            children.add(new Child(text, labels, ports, id, type, null, null, new ArrayList<>()));
        }
    }

    private void createFlows(DataFlowDiagram dataFlowDiagram, List<Child> children) {
        for (Flow flow : dataFlowDiagram.getFlows()) {
            inputPinToFlowNameMap.put(flow.getDestinationPin(), flow.getEntityName());
            children.add(createWebFlow(flow));
        }
    }

    private Child createWebFlow(Flow flow) {
        String id = flow.getId();
        String type = "edge:arrow";
        String sourceId = flow.getSourcePin().getId();
        String targetId = flow.getDestinationPin().getId();
        String text = flow.getEntityName();
        return new Child(text, null, null, id, type, sourceId, targetId, new ArrayList<>());
    }

    private Map<Pin, List<AbstractAssignment>> mapping(Node node) {
        Map<Pin, List<AbstractAssignment>> mapPinToAssignments = new HashMap<>();

        for (AbstractAssignment assignment : node.getBehaviour().getAssignment()) {
            if (mapPinToAssignments.containsKey(assignment.getOutputPin())) {
                mapPinToAssignments.get(assignment.getOutputPin()).add(assignment);
            } else {
                List<AbstractAssignment> list = new ArrayList<>();
                list.add(assignment);
                mapPinToAssignments.put(assignment.getOutputPin(), list);
            }
        }
        return mapPinToAssignments;
    }

    private String createBehaviourString(List<AbstractAssignment> abstractAssignments) {
        if (abstractAssignments == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (AbstractAssignment abstractAssignment : abstractAssignments) {
            if (abstractAssignment instanceof ForwardingAssignment) {
                for (Pin inPin : abstractAssignment.getInputPins()) {
                    builder.append("forward ").append(inputPinToFlowNameMap.get(inPin)).append("\n");
                }
            } else {
                Assignment assignment = (Assignment) abstractAssignment;
                String value = behaviorConverter.termToString(assignment.getTerm());

                for (Label label : assignment.getOutputLabels()) {
                    builder.append("set ").append(((LabelType) label.eContainer()).getEntityName()).append(".").append(label.getEntityName())
                            .append(" = ").append(value).append("\n");
                }
            }
        }
        return builder.toString().trim();
    }

    private void parseBehavior(Node node, Pin outpin, String lines, DataFlowDiagram dfd, DataDictionary dd) {
        String[] behaviorStrings = lines.split("\n");
        var behavior = node.getBehaviour();
        for (String behaviorString : behaviorStrings) {
            if (behaviorString.contains("forward ")) {
                String packet = behaviorString.split(" ")[1];

                Pin inpin = dfd.getFlows().stream().filter(flow -> flow.getDestinationNode() == node)
                        .filter(flow -> flow.getEntityName().equals(packet)).map(Flow::getDestinationPin).findAny().orElse(null);

                var assignment = ddFactory.createForwardingAssignment();
                assignment.setOutputPin(outpin);
                assignment.getInputPins().add(inpin);
                behavior.getAssignment().add(assignment);
            } else if (behaviorString.contains("set ")) {
                String regex = "\\bset\\s+(\\S+)\\s+=\\s+(.+)\\b";

                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(behaviorString);
                if (!matcher.find() || matcher.groupCount() != 2) {
                    logger.error("Invalid behavior string:");
                    logger.error(behaviorStrings);
                    continue;
                }
                String variable = matcher.group(1);
                String typeName = variable.split("\\.")[0];
                String valueName = variable.split("\\.")[1];

                Label value = dd.getLabelTypes().stream().filter(labelType -> labelType.getEntityName().equals(typeName))
                        .flatMap(labelType -> labelType.getLabel().stream()).filter(label -> label.getEntityName().equals(valueName)).findAny()
                        .orElse(null);

                Assignment assignment = ddFactory.createAssignment();

                assignment.getInputPins().addAll(behavior.getInPin());
                assignment.setOutputPin(outpin);
                assignment.getOutputLabels().add(value);

                behavior.getAssignment().add(assignment);

                Term term = behaviorConverter.stringToTerm(matcher.group(2));

                assignment.setTerm(term);
            }
        }
    }

    private void putValue(Map<Node, Map<Pin, String>> nestedHashMap, Node node, Pin pin, String value) {
        nestedHashMap.computeIfAbsent(node, k -> new HashMap<>()).put(pin, value);
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
     * Converts Data Flow Diagram and Data Dictionary provided via paths into a WebEditorDfd object.
     * @param inputDataFlowDiagram The path of the data flow diagram.
     * @param inputDataDictionary The path of the data dictionary.
     * @return WebEditorDfd object representing the web editor version of the data flow diagram.
     */
    public WebEditorDfd dfdToWeb(String inputDataFlowDiagram, String inputDataDictionary) {
        DataFlowDiagramAndDictionary complete = loadDFD(inputDataFlowDiagram, inputDataDictionary);
        return processDfd(complete.dataFlowDiagram(), complete.dataDictionary());
    }

    /**
     * Converts a DataFlowDiagramAndDictionary object into a WebEditorDfd object.
     * @param complete The DataFlowDiagramAndDictionary object to convert.
     * @return WebEditorDfd object representing the web editor version of the data flow diagram.
     */
    public WebEditorDfd dfdToWeb(DataFlowDiagramAndDictionary complete) {
        return processDfd(complete.dataFlowDiagram(), complete.dataDictionary());
    }

    /**
     * Stores a WebEditorDfd object into a specified output file.
     * @param web The WebEditorDfd object to store.
     * @param outputFile The path of the output file.
     */
    public void store(WebEditorDfd web, String outputFile) {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            objectMapper.writeValue(new File(outputFile), web);
        } catch (IOException e) {
            logger.error("Could not store web dfd:", e);
        }
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

    /**
     * Loads a data flow diagram and data dictionary from specified input files and returns them as a combined object.
     * @param inputDataFlowDiagram The path of the input data flow diagram file.
     * @param inputDataDictionary The path of the input data dictionary file.
     * @return DataFlowDiagramAndDictionary object representing the loaded data flow diagram and dictionary.
     */
    public DataFlowDiagramAndDictionary loadDFD(String inputDataFlowDiagram, String inputDataDictionary) {
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION,
                new XMIResourceFactoryImpl());
        resourceSet.getPackageRegistry().put(dataflowdiagramPackage.eNS_URI, dataflowdiagramPackage.eINSTANCE);

        Resource dfdResource = resourceSet.getResource(URI.createFileURI(inputDataFlowDiagram), true);
        Resource ddResource = resourceSet.getResource(URI.createFileURI(inputDataDictionary), true);

        DataFlowDiagram datwFlowDiagram = (DataFlowDiagram) dfdResource.getContents().get(0);
        DataDictionary dataDictionary = (DataDictionary) ddResource.getContents().get(0);

        return new DataFlowDiagramAndDictionary(datwFlowDiagram, dataDictionary);
    }
}
