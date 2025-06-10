package org.dataflowanalysis.converter.dfd2web;

import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.TransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.core.DFDFlowGraphCollection;
import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.analysis.dfd.simple.DFDSimpleTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.converter.Converter;
import org.dataflowanalysis.converter.ConverterModel;
import org.dataflowanalysis.converter.web2dfd.BehaviorConverter;
import org.dataflowanalysis.converter.web2dfd.WebEditorConverterModel;
import org.dataflowanalysis.converter.web2dfd.model.Annotation;
import org.dataflowanalysis.converter.web2dfd.model.Child;
import org.dataflowanalysis.converter.web2dfd.model.Model;
import org.dataflowanalysis.converter.web2dfd.model.Port;
import org.dataflowanalysis.converter.web2dfd.model.Value;
import org.dataflowanalysis.converter.web2dfd.model.WebEditorDfd;
import org.dataflowanalysis.converter.web2dfd.model.WebEditorLabel;
import org.dataflowanalysis.converter.web2dfd.model.WebEditorLabelType;
import org.dataflowanalysis.dfd.datadictionary.*;
import org.dataflowanalysis.dfd.dataflowdiagram.*;
import org.dataflowanalysis.dfd.dataflowdiagram.Process;

/**
 * Converts data flow diagrams and dictionaries to the web editor format
 */
public class DFD2WebConverter extends Converter {
    private final Logger logger = Logger.getLogger(DFD2WebConverter.class);

    private final static String DELIMITER_PIN_NAME = "|";
    private final static String DELIMITER_MULTI_PIN = ",";
    private final static String DELIMITER_MULTI_LABEL = ",";
    private final static String CONTROL_FLOW_NAME = "~";

    private Optional<List<AnalysisConstraint>> constraints = Optional.empty();
    private Optional<Class<? extends TransposeFlowGraphFinder>> transposeFlowGraphFinder = Optional.empty();
    private boolean readOnly = false;

    private Map<Pin, List<String>> inputPinToFlowNamesMap;
    private BehaviorConverter behaviorConverter;

    @Override
    public WebEditorConverterModel convert(ConverterModel input) {
        Optional<DataFlowDiagramAndDictionary> dfdModel = input.toType(DataFlowDiagramAndDictionary.class);
        if (dfdModel.isEmpty()) {
            logger.error("Expected DataFlowDiagramAndDictionary, but got: " + input.getClass()
                    .getSimpleName());
            throw new IllegalArgumentException("Invalid input for Model Conversion");
        }
        WebEditorDfd webEditorDfd = processDfd(dfdModel.get()
                .dataFlowDiagram(),
                dfdModel.get()
                        .dataDictionary(),
                createNodeAnnotationMap(dfdModel.get(), this.constraints.orElse(null), this.transposeFlowGraphFinder.orElse(null)));
        return new WebEditorConverterModel(webEditorDfd);
    }

    /**
     * Sets the conditions, when a vertex should receive an annotation
     * @param conditions Conditions that determine whether a vertex receives an annotation
     */
    public void setConditions(List<AnalysisConstraint> constraints) {
        this.constraints = Optional.ofNullable(constraints);
    }

    /**
     * Sets the transpose flow graph finder class that is used to create annotations
     * @param transposeFlowGraphFinder Transpose graph finder class that is used to create annotations
     */
    public void setTransposeFlowGraphFinder(Class<? extends TransposeFlowGraphFinder> transposeFlowGraphFinder) {
        this.transposeFlowGraphFinder = Optional.ofNullable(transposeFlowGraphFinder);
    }

    /**
     * Sets whether the resulting web dfd should be in read-only or not
     * @param readOnly Determines whether the read only mode is set
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Creates the node annotations by analyzing the DFD
     * @param complete DFD / DD combination
     * @param conditions List of constraints (optional)
     * @param finderClass Custom TFG Finder (optional)
     * @return Returns the annotations that should be added to nodes in the data flow diagram
     */
    private Map<Node, List<Annotation>> createNodeAnnotationMap(DataFlowDiagramAndDictionary complete,
            List<AnalysisConstraint> conditions, Class<? extends TransposeFlowGraphFinder> finderClass) {
        var collection = getTransposeFlowGraphs(complete, finderClass).stream()
                .map(AbstractTransposeFlowGraph::evaluate)
                .toList();

        Map<Node, List<Annotation>> mapNodeToAnnotations = new HashMap<>();
        
        complete.dataFlowDiagram().getNodes().forEach(node -> mapNodeToAnnotations.put(node, new ArrayList<>()));
        
        collection.forEach(tfg -> tfg.getVertices()
                .forEach(vertex -> {
                    var node = vertex.getReferencedElement();
                    mapNodeToAnnotations.get(node).addAll(createLabelAnnotationsForOneVertex((AbstractVertex<Node>) vertex, tfg.hashCode()));
                }));       

        if (conditions == null)
            return mapNodeToAnnotations;
        
        conditions.forEach(condition -> {
            condition.findViolations(new DFDFlowGraphCollection(null, collection)).forEach(violation -> {
                violation.getMatchedVertices().stream().forEach(it -> {
                    var node = (Node) it.getReferencedElement();
                    mapNodeToAnnotations.putIfAbsent(node, new ArrayList<>());
                    String message = "Constraint " + condition.getName() + " violated";
                    mapNodeToAnnotations.get(node).add(new Annotation(message, "bolt", stringToColorHex(condition.getName()), violation.getTransposeFlowGraph().hashCode()));
                });
            });             
        });
        return mapNodeToAnnotations;
    }
    
    private List<Annotation> createLabelAnnotationsForOneVertex(AbstractVertex<Node> vertex, int tfg) {
        List<Annotation> annotations = new ArrayList<>();
        StringBuilder builder = new StringBuilder("Propagated:");
        if (!vertex.getAllOutgoingDataCharacteristics().isEmpty()) {
            vertex.getAllOutgoingDataCharacteristics()
                    .forEach(characteristic -> characteristic.getAllCharacteristics()
                            .forEach(value -> builder.append(value.getTypeName() + "." + value.getValueName()).append(",")));
            annotations.add(new Annotation(builder.toString(), "tag", "#FFFFFF", tfg));
        }
        if (!vertex.getAllIncomingDataCharacteristics().isEmpty()) {
            builder.setLength(0);
            builder.append("Incoming:");
            vertex.getAllIncomingDataCharacteristics()
            .forEach(characteristic -> characteristic.getAllCharacteristics()
                    .forEach(value -> builder.append(value.getTypeName() + "." + value.getValueName()).append(",")));
            annotations.add(new Annotation(builder.toString(), "tag", "#FFFFFF", tfg));             
        }
        return annotations;
    }

    private static List<? extends AbstractTransposeFlowGraph> getTransposeFlowGraphs(DataFlowDiagramAndDictionary complete,
            Class<? extends TransposeFlowGraphFinder> finderClass) {
        TransposeFlowGraphFinder finder;
        if (finderClass == null)
            finder = new DFDTransposeFlowGraphFinder(complete.dataDictionary(), complete.dataFlowDiagram());
        else {
            if (finderClass.equals(DFDSimpleTransposeFlowGraphFinder.class))
                finder = new DFDSimpleTransposeFlowGraphFinder(complete.dataDictionary(), complete.dataFlowDiagram());
            else
                finder = new DFDTransposeFlowGraphFinder(complete.dataDictionary(), complete.dataFlowDiagram());

        }
        return finder.findTransposeFlowGraphs();
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

    private WebEditorDfd processDfd(DataFlowDiagram dataFlowDiagram, DataDictionary dataDictionary, Map<Node, List<Annotation>> mapNodeToAnnotation) {
        inputPinToFlowNamesMap = new HashMap<>();
        List<Child> children = new ArrayList<>();
        List<WebEditorLabelType> labelTypes = new ArrayList<>();

        behaviorConverter = new BehaviorConverter(dataDictionary);

        createLabelTypesAndValues(labelTypes, dataDictionary);

        createFlows(dataFlowDiagram, children);

        createNodes(dataFlowDiagram, children, mapNodeToAnnotation);

        return new WebEditorDfd(new Model("graph", "root", children), labelTypes, readOnly ? "view" : "edit", new ArrayList<>());
    }

    private void createNodes(DataFlowDiagram dataFlowDiagram, List<Child> children, Map<Node, List<Annotation>> mapNodeToAnnotation) {
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
                String labelId = label.getId();
                String labelTypeId = ((LabelType) label.eContainer()).getId();

                labels.add(new WebEditorLabel(labelTypeId, labelId));
            }

            List<Port> ports = new ArrayList<>();

            node.getBehavior()
                    .getInPin()
                    .forEach(pin -> ports.add(new Port(null, pin.getId(), "port:dfd-input", new ArrayList<>())));

            Map<Pin, List<AbstractAssignment>> mapPinToAssignments = mapping(node);

            node.getBehavior()
                    .getOutPin()
                    .forEach(pin -> ports
                            .add(new Port(createBehaviorString(mapPinToAssignments.get(pin)), pin.getId(), "port:dfd-output", new ArrayList<>())));
            if (mapNodeToAnnotation == null)
                children.add(new Child(text, labels, ports, id, type, null, null, null, new ArrayList<>()));
            else
                children.add(new Child(text, labels, ports, id, type, null, null, mapNodeToAnnotation.get(node), new ArrayList<>()));
        }
    }

    private void createFlows(DataFlowDiagram dataFlowDiagram, List<Child> children) {
        var controlFlowNameMap = createControlFlowNameMap(dataFlowDiagram);
        for (Flow flow : dataFlowDiagram.getFlows()) {
            fillPinToFlowNamesMap(inputPinToFlowNamesMap, flow, controlFlowNameMap);
            children.add(createFlow(flow, controlFlowNameMap));
        }
    }

    private HashMap<Flow, String> createControlFlowNameMap(DataFlowDiagram dataFlowDiagram) {
        var controlFlowNameMap = new HashMap<Flow, String>();
        dataFlowDiagram.getNodes()
                .forEach(node -> {
                    var controlFlows = dataFlowDiagram.getFlows()
                            .stream()
                            .filter(flow -> flow.getDestinationNode()
                                    .equals(node))
                            .filter(flow -> flow.getEntityName()
                                    .isEmpty())
                            .toList();

                    String controlFlowName = CONTROL_FLOW_NAME;
                    for (var flow : controlFlows) {
                        controlFlowNameMap.put(flow, controlFlowName);
                        controlFlowName += CONTROL_FLOW_NAME;
                    }
                });
        return controlFlowNameMap;
    }

    private Child createFlow(Flow flow, HashMap<Flow, String> controlFlowNameMap) {
        String id = flow.getId();
        String type = "edge:arrow";
        String sourceId = flow.getSourcePin()
                .getId();
        String targetId = flow.getDestinationPin()
                .getId();
        String text = controlFlowNameMap.getOrDefault(flow, flow.getEntityName());
        return new Child(text, null, null, id, type, sourceId, targetId, null, new ArrayList<>());
    }

    private Map<Pin, List<AbstractAssignment>> mapping(Node node) {
        Map<Pin, List<AbstractAssignment>> mapPinToAssignments = new HashMap<>();

        for (AbstractAssignment assignment : node.getBehavior()
                .getAssignment()) {
            if (mapPinToAssignments.containsKey(assignment.getOutputPin())) {
                mapPinToAssignments.get(assignment.getOutputPin())
                        .add(assignment);
            } else {
                List<AbstractAssignment> list = new ArrayList<>();
                list.add(assignment);
                mapPinToAssignments.put(assignment.getOutputPin(), list);
            }
        }
        return mapPinToAssignments;
    }

    private String createBehaviorString(List<AbstractAssignment> abstractAssignments) {
        if (abstractAssignments == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (AbstractAssignment abstractAssignment : abstractAssignments) {
            if (abstractAssignment instanceof ForwardingAssignment forwardingAssignment) {
                builder.append("forward ");
                builder.append(getStringFromInputPins(forwardingAssignment.getInputPins()));
            } else if (abstractAssignment instanceof SetAssignment setAssignment) {
                builder.append("set ");
                builder.append(getStringFromOutLabels(setAssignment.getOutputLabels()));
            } else if (abstractAssignment instanceof UnsetAssignment unsetAssignment) {
                builder.append("unset ");
                builder.append(getStringFromOutLabels(unsetAssignment.getOutputLabels()));
            } else if (abstractAssignment instanceof Assignment assignment) {
                builder.append("assign ");
                builder.append(getStringFromOutLabels(assignment.getOutputLabels()));
                builder.append(" if ");
                builder.append(behaviorConverter.termToString(assignment.getTerm()));
                if (!assignment.getInputPins()
                        .isEmpty()) {
                    builder.append(" from ");
                    builder.append(getStringFromInputPins(assignment.getInputPins()));
                }
            }
            builder.append("\n");
        }
        return builder.toString()
                .trim();
    }

    private String getStringFromInputPins(List<Pin> inputPins) {
        List<String> pinNamesAsString = new ArrayList<>();

        inputPins.forEach(pin -> {
            var flowNames = inputPinToFlowNamesMap.get(pin)
                    .stream()
                    .sorted()
                    .toList();
            var pinName = String.join(DELIMITER_PIN_NAME, flowNames);
            pinNamesAsString.add(pinName);
        });
        return String.join(DELIMITER_MULTI_PIN, pinNamesAsString);
    }

    private String getStringFromOutLabels(List<Label> outLabels) {
        List<String> outLabelsAsStrings = new ArrayList<>();

        outLabels.forEach(label -> outLabelsAsStrings.add(((LabelType) label.eContainer()).getEntityName() + "." + label.getEntityName()));

        return String.join(DELIMITER_MULTI_LABEL, outLabelsAsStrings);
    }

    private void fillPinToFlowNamesMap(Map<Pin, List<String>> map, Flow flow, HashMap<Flow, String> controlFlowNameMap) {
        if (map.containsKey(flow.getDestinationPin())) {
            map.get(flow.getDestinationPin())
                    .add(controlFlowNameMap.getOrDefault(flow, flow.getEntityName()));
        } else {
            List<String> flowNames = new ArrayList<>();
            flowNames.add(controlFlowNameMap.getOrDefault(flow, flow.getEntityName()));
            map.put(flow.getDestinationPin(), flowNames);
        }
    }

    private static String stringToColorHex(String input) {
        byte[] hash;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            hash = new byte[] {(byte)0x80, (byte)0x80, (byte)0x80, 0};
        }
        float hue = (hash[0] & 0xFF) / 255f;
        float saturation = 0.5f + ((hash[1] & 0xFF) / 255f) * 0.5f;
        float brightness = 0.3f + ((hash[2] & 0xFF) / 255f) * 0.5f;
        saturation = Math.max(0.5f, Math.min(saturation, 1.0f));
        brightness = Math.max(0.3f, Math.min(brightness, 0.8f));
        Color color = Color.getHSBColor(hue, saturation, brightness);
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }
}
