package org.dataflowanalysis.analysis.converter;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

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

public class DataFlowDiagramConverter extends Converter {

    private final Map<Pin, String> mapInputPinToFlowName = new HashMap<>();
    private final dataflowdiagramFactory dfdFactory;
    private final datadictionaryFactory ddFactory;
    private Map<String, Node> nodesMap;

    private final Logger logger = Logger.getLogger(MicroSecEndConverter.class);

    public DataFlowDiagramConverter() {
        dfdFactory = dataflowdiagramFactory.eINSTANCE;
        ddFactory = datadictionaryFactory.eINSTANCE;

        nodesMap = new HashMap<>();
    }

    private DataFlowDiagramAndDictionary processWeb(WebEditorDfd webdfd) {
        nodesMap = new HashMap<>();
        Map<String, Node> pinToNodeMap = new HashMap<>();
        Map<String, Pin> pinMap = new HashMap<>();
        Map<String, Label> idToLabelMap = new HashMap<>();
        Map<Node, Map<Pin, String>> nodeOutpinBehavior = new HashMap<>();

        DataFlowDiagram dataFlowDiagram = dfdFactory.createDataFlowDiagram();
        DataDictionary dataDictionary = ddFactory.createDataDictionary();

        createWebLabelTypesAndValues(webdfd, idToLabelMap, dataDictionary);

        createWebNodes(webdfd, pinToNodeMap, pinMap, idToLabelMap, nodeOutpinBehavior, dataFlowDiagram, dataDictionary);

        createWebFlows(webdfd, pinToNodeMap, pinMap, dataFlowDiagram);

        for (Node node : nodesMap.values()) {
            if (nodeOutpinBehavior.containsKey(node)) {
                for (Pin outpin : nodeOutpinBehavior.get(node).keySet()) {
                    parseBehavior(node, outpin, nodeOutpinBehavior.get(node).get(outpin), dataFlowDiagram, dataDictionary);
                }
            }
        }

        return new DataFlowDiagramAndDictionary(dataFlowDiagram, dataDictionary);
    }

    private void createWebNodes(WebEditorDfd webdfd, Map<String, Node> pinToNodeMap, Map<String, Pin> pinMap, Map<String, Label> idToLabelMap,
            Map<Node, Map<Pin, String>> nodeOutpinBehavior, DataFlowDiagram dataFlowDiagram, DataDictionary dataDictionary) {
        for (Child child : webdfd.model().children()) {
            String[] type = child.type().split(":");
            String name = child.text();

            if (type[0].equals("node")) {
                Node node;
                switch (type[1]) {
                    case "function":
                        node = dfdFactory.createProcess();
                        break;
                    case "storage":
                        node = dfdFactory.createStore();
                        break;
                    case "input-output":
                        node = dfdFactory.createExternal();
                        break;
                    default:
                        logger.error("Unrecognized node type: " + type[1]);
                        continue;

                }
                node.setEntityName(name);
                node.setId(child.id());

                var behaviour = ddFactory.createBehaviour();
                node.setBehaviour(behaviour);
                dataDictionary.getBehaviour().add(behaviour);

                createWebPins(pinToNodeMap, pinMap, nodeOutpinBehavior, child, node);

                List<Label> labelsAtNode = new ArrayList<>();
                for (WebEditorLabel webLabel : child.labels()) {
                    labelsAtNode.add(idToLabelMap.get(webLabel.labelTypeValueId()));
                }
                node.getProperties().addAll(labelsAtNode);

                dataFlowDiagram.getNodes().add(node);
                nodesMap.put(child.id(), node);
            }
        }
    }

    private void createWebFlows(WebEditorDfd webdfd, Map<String, Node> pinToNodeMap, Map<String, Pin> pinMap, DataFlowDiagram dataFlowDiagram) {
        for (Child child : webdfd.model().children()) {
            String[] type = child.type().split(":");

            if (type[0].equals("edge")) {
                var source = pinToNodeMap.get(child.sourceId());
                var dest = pinToNodeMap.get(child.targetId());

                var flow = dfdFactory.createFlow();
                flow.setSourceNode(source);
                flow.setDestinationNode(dest);
                flow.setEntityName(child.text());

                flow.setDestinationPin(pinMap.get(child.targetId()));
                flow.setSourcePin(pinMap.get(child.sourceId()));
                flow.setId(child.id());
                dataFlowDiagram.getFlows().add(flow);
            }
        }
    }

    private void createWebPins(Map<String, Node> pinToNodeMap, Map<String, Pin> pinMap, Map<Node, Map<Pin, String>> nodeOutpinBehavior, Child child,
            Node node) {
        for (Port port : child.ports()) {
            if (port.type().equals("port:dfd-input")) {
                var inPin = ddFactory.createPin();
                inPin.setId(port.id());
                node.getBehaviour().getInPin().add(inPin);
                pinMap.put(port.id(), inPin);
            } else if (port.type().equals("port:dfd-output")) {
                var outPin = ddFactory.createPin();
                outPin.setId(port.id());
                node.getBehaviour().getOutPin().add(outPin);
                pinMap.put(port.id(), outPin);
                if (port.behavior() != null) {
                    putValue(nodeOutpinBehavior, node, outPin, port.behavior());
                }
            }
            pinToNodeMap.put(port.id(), node);
        }
    }

    private void createWebLabelTypesAndValues(WebEditorDfd webdfd, Map<String, Label> idToLabelMap, DataDictionary dataDictionary) {
        for (WebEditorLabelType webLabelType : webdfd.labelTypes()) {
            LabelType labelType = ddFactory.createLabelType();
            labelType.setEntityName(webLabelType.name());
            labelType.setId(webLabelType.id());
            for (Value value : webLabelType.values()) {
                Label label = ddFactory.createLabel();
                label.setEntityName(value.text());
                label.setId(value.id());
                labelType.getLabel().add(label);
                idToLabelMap.put(label.getId(), label);
            }
            dataDictionary.getLabelTypes().add(labelType);
        }
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
            
            node.getBehaviour().getOutPin()
            .forEach(pin -> ports.add(new Port(createBehaviourString(mapPinToAssignments.get(pin)), pin.getId(), "port:dfd-output", new ArrayList<>())));

            children.add(new Child(text, labels, ports, id, type, null, null, new ArrayList<>()));
        }
    }

    private void createFlows(DataFlowDiagram dataFlowDiagram, List<Child> children) {
        for (Flow flow : dataFlowDiagram.getFlows()) {
            String id = flow.getId();
            String type = "edge:arrow";
            String sourceId = flow.getSourcePin().getId();
            String targetId = flow.getDestinationPin().getId();
            String text = flow.getEntityName();
            mapInputPinToFlowName.put(flow.getDestinationPin(), text);
            children.add(new Child(text, null, null, id, type, sourceId, targetId, new ArrayList<>()));
        }
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
        StringBuilder builder = new StringBuilder();
        if (abstractAssignments != null) {
            for (AbstractAssignment abstractAssignment : abstractAssignments) {
                if (abstractAssignment instanceof ForwardingAssignment) {
                    for (Pin inPin : abstractAssignment.getInputPins()) {
                        builder.append("forward ").append(mapInputPinToFlowName.get(inPin)).append("\n");
                    }
                } else {
                    Assignment assignment = (Assignment) abstractAssignment;
                    String value = getTermValue(assignment.getTerm()) ? "TRUE" : "FALSE";

                    for (Label label : assignment.getOutputLabels()) {
                        try {
                            builder.append("set ").append(((LabelType) label.eContainer()).getEntityName()).append(".").append(label.getEntityName())
                                    .append(" = ").append(value).append("\n");
                        } catch (IllegalArgumentException ex) {
                            System.out.println(
                                    "Caution!! WebEditor cant handle complex Assignments yet. Only TRUE or NOT(TRUE) supported. Everything else ignored");
                        }
                    }
                }
            }
            return builder.toString().trim();
        }
        return null;

    }

    // Currently only supports True or False
    private boolean getTermValue(Term term) throws IllegalArgumentException {
        if (term instanceof TRUE)
            return true;
        if (term instanceof NOT) {
            return !getTermValue(((NOT) term).getNegatedTerm());
        }
        throw new IllegalArgumentException();
    }

    private void parseBehavior(Node node, Pin outpin, String lines, DataFlowDiagram dfd, DataDictionary dd) {
        String[] behaviorStrings = lines.split("\n");
        var behavior = node.getBehaviour();
        for (String behaviorString : behaviorStrings) {
            if (behaviorString.contains("forward ")) {
                String packet = behaviorString.split(" ")[1];
                Pin inpin = null;
                for (Flow flow : dfd.getFlows()) {
                    if (flow.getDestinationNode() == node) {
                        if (flow.getEntityName().equals(packet)) {
                            inpin = flow.getDestinationPin();
                        }
                    }
                }

                var assignment = ddFactory.createForwardingAssignment();
                assignment.setOutputPin(outpin);
                assignment.getInputPins().add(inpin);
                behavior.getAssignment().add(assignment);
            } else if (behaviorString.contains("set ")) {
                String regex = "\\bset\\s+(\\S+)\\s+=\\s+(.+)\\b";

                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(behaviorString);
                matcher.find();

                String variable = matcher.group(1);
                String typeName = variable.split("\\.")[0];
                String valueName = variable.split("\\.")[1];

                Label value = null;
                for (LabelType labelType : dd.getLabelTypes()) {
                    if (labelType.getEntityName().equals(typeName)) {
                        for (Label label : labelType.getLabel()) {
                            if (label.getEntityName().equals(valueName)) {
                                value = label;
                            }
                        }
                    }
                }
                Assignment assignment = ddFactory.createAssignment();

                assignment.getInputPins().addAll(behavior.getInPin());
                assignment.setOutputPin(outpin);
                assignment.getOutputLabels().add(value);

                behavior.getAssignment().add(assignment);

                List<String> expressions = Arrays.asList(matcher.group(2).split(" "));
                Stack<String> stack = new Stack<>();
                stack.addAll(expressions);

                Term processedTerm = null;
                while (!stack.isEmpty()) {
                    String current = stack.pop();
                    if (current.equals("TRUE")) {
                        var ddTrue = ddFactory.createTRUE();
                        if (processedTerm == null) {
                            processedTerm = ddTrue;
                        }
                    } else if (current.equals("FALSE")) {
                        var ddFalse = ddFactory.createNOT();
                        ddFalse.setNegatedTerm(ddFactory.createTRUE());
                        if (processedTerm == null) {
                            processedTerm = ddFalse;
                        }
                    }
                }
                assignment.setTerm(processedTerm);
            }
        }
    }

    private void putValue(Map<Node, Map<Pin, String>> nestedHashMap, Node key1, Pin key2, String value) {
        nestedHashMap.computeIfAbsent(key1, k -> new HashMap<>()).put(key2, value);
    }

    public DataFlowDiagramAndDictionary webToDfd(String inputFile) {
        return webToDfd(loadWeb(inputFile));
    }

    public DataFlowDiagramAndDictionary webToDfd(WebEditorDfd inputFile) {
        return processWeb(inputFile);
    }

    public WebEditorDfd dfdToWeb(String inputDataFlowDiagram, String inputDataDictionary) {
        DataFlowDiagramAndDictionary complete = loadDFD(inputDataFlowDiagram, inputDataDictionary);
        return processDfd(complete.dataFlowDiagram(), complete.dataDictionary());
    }

    public WebEditorDfd dfdToWeb(DataFlowDiagramAndDictionary complete) {
        return processDfd(complete.dataFlowDiagram(), complete.dataDictionary());
    }

    public void store(WebEditorDfd web, String outputFile) {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            objectMapper.writeValue(new File(outputFile), web);
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public WebEditorDfd loadWeb(String inputFile) {
        objectMapper = new ObjectMapper();
        file = new File(inputFile);
        try {
            return objectMapper.readValue(file, WebEditorDfd.class);
        } catch (IOException e) {
            logger.error(e);
            return null;
        }
    }

    public DataFlowDiagramAndDictionary loadDFD(String inputDataFlowDiagram, String inputDataDictionary) {
        ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
        rs.getPackageRegistry().put(dataflowdiagramPackage.eNS_URI, dataflowdiagramPackage.eINSTANCE);

        Resource dfdResource = rs.getResource(URI.createFileURI(inputDataFlowDiagram), true);
        Resource ddResource = rs.getResource(URI.createFileURI(inputDataDictionary), true);

        DataFlowDiagram dfd = (DataFlowDiagram) dfdResource.getContents().get(0);
        DataDictionary dd = (DataDictionary) ddResource.getContents().get(0);

        return new DataFlowDiagramAndDictionary(dfd, dd);
    }
}
