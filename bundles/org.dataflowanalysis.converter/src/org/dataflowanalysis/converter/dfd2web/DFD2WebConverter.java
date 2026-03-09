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
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.TransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.core.DFDFlowGraphCollection;
import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.simple.DFDSimpleTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.DataCharacteristicsSelector;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.converter.Converter;
import org.dataflowanalysis.converter.ConverterModel;
import org.dataflowanalysis.converter.web2dfd.BehaviorConverter;
import org.dataflowanalysis.converter.web2dfd.WebEditorConverterModel;
import org.dataflowanalysis.converter.web2dfd.model.Annotation;
import org.dataflowanalysis.converter.web2dfd.model.Child;
import org.dataflowanalysis.converter.web2dfd.model.Model;
import org.dataflowanalysis.converter.web2dfd.model.Port;
import org.dataflowanalysis.converter.web2dfd.model.Position;
import org.dataflowanalysis.converter.web2dfd.model.Value;
import org.dataflowanalysis.converter.web2dfd.model.Violation;
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
    private final Logger logger = LoggerManager.getLogger(DFD2WebConverter.class);

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
        
        var complete = dfdModel.get();
        var constraints = this.constraints.orElse(null);
        var finderClass = this.transposeFlowGraphFinder.orElse(null);
        var violationTuples = analyzeViolations(complete, constraints, finderClass);
        
        WebEditorDfd webEditorDfd = processDfd(dfdModel.get()
                .dataFlowDiagram(),
                dfdModel.get()
                        .dataDictionary(),
                createNodeAnnotationMap(complete, constraints, finderClass, violationTuples),
                violationTuples);
        return new WebEditorConverterModel(webEditorDfd);
    }

    /**
     * Sets the constraints, when a vertex should receive an annotation
     * @param constraints constraints that determine whether a vertex receives an annotation
     */
    public void setConstraints(List<AnalysisConstraint> constraints) {
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
     * Creates a mapping from each node in the data flow diagram to its list of annotations.
     * <p/>
     * Annotations include label propagation information for each vertex as well as violation
     * markers for any constraint violations found in the provided violation tuples.
     * @param complete DFD / DD combination
     * @param constraints List of constraints (optional)
     * @param finderClass Custom TFG finder class (optional)
     * @param violationTuples Pre-computed list of constraint violations
     * @return Map from each {@link Node} to its list of {@link Annotation} objects
     */
    private Map<Node, List<Annotation>> createNodeAnnotationMap(DataFlowDiagramAndDictionary complete, 
            List<AnalysisConstraint> constraints,
            Class<? extends TransposeFlowGraphFinder> finderClass,
            List<ViolationTuple> violationTuples) {

		Map<Node, List<Annotation>> mapNodeToAnnotations = new HashMap<>();
		
		var tfgResults = getTransposeFlowGraphs(complete, finderClass).stream()
			.map(AbstractTransposeFlowGraph::evaluate)
			.toList();
		
		complete.dataFlowDiagram().getNodes().forEach(node -> mapNodeToAnnotations.put(node, new ArrayList<>()));
		
		tfgResults.forEach(tfg -> tfg.getVertices().forEach(vertex -> {
			var node = (Node) vertex.getReferencedElement();
			mapNodeToAnnotations.get(node).addAll(createLabelAnnotationsForOneVertex((AbstractVertex<Node>) vertex, tfg.hashCode()));
		}));
		
		for (ViolationTuple tuple : violationTuples) {
			String constraintName = tuple.constraint().getName();
			String message = "Constraint " + constraintName + " violated";
			String color = stringToColorHex(constraintName);
			int tfgHash = tuple.result().getTransposeFlowGraph().hashCode();
			
			tuple.result().getMatchedVertices().forEach(vertex -> {
				Node node = (Node) vertex.getReferencedElement();
				mapNodeToAnnotations.get(node).add(new Annotation(message, "bolt", color, tfgHash));
			});
		}
		
		return mapNodeToAnnotations;
	}
    
    /**
     * Analyzes the given data flow diagram for constraint violations.
     * <p/>
     * Evaluates all transpose flow graphs and checks each provided constraint against them,
     * returning a flat list of all violations found as {@link ViolationTuple} objects.
     * Returns an empty list if no constraints are provided.
     * @param complete DFD / DD combination to analyze
     * @param constraints List of constraints to check, may be null or empty
     * @param finderClass Custom TFG finder class (optional)
     * @return List of {@link ViolationTuple} objects, each pairing a violated constraint with its result
     */
    public List<ViolationTuple> analyzeViolations(DataFlowDiagramAndDictionary complete, 
                                                 List<AnalysisConstraint> constraints, 
                                                 Class<? extends TransposeFlowGraphFinder> finderClass) {
        if (constraints == null || constraints.isEmpty()) {
            return new ArrayList<>();
        }

        var evaluatedGraphs = getTransposeFlowGraphs(complete, finderClass).stream()
                .map(AbstractTransposeFlowGraph::evaluate)
                .toList();

        var collection = new DFDFlowGraphCollection(null, evaluatedGraphs);

        return constraints.stream()
                .flatMap(constraint -> constraint.findViolations(collection).stream()
                        .map(result -> {
                        	return new ViolationTuple(constraint, result);}
                        ))
                .collect(Collectors.toList());
    }


    private List<Annotation> createLabelAnnotationsForOneVertex(AbstractVertex<Node> vertex, int tfg) {
        List<Annotation> annotations = new ArrayList<>();

        if (!vertex.getAllOutgoingDataCharacteristics()
                .isEmpty()) {
            String outgoing = vertex.getAllOutgoingDataCharacteristics()
                    .stream()
                    .flatMap(ch -> ch.getAllCharacteristics()
                            .stream())
                    .map(v -> v.getTypeName() + "." + v.getValueName())
                    .collect(Collectors.joining(", "));
            annotations.add(new Annotation("Propagated: " + outgoing, "tag", "#FFFFFF", tfg));
        }

        if (!vertex.getAllIncomingDataCharacteristics()
                .isEmpty()) {
            String incoming = vertex.getAllIncomingDataCharacteristics()
                    .stream()
                    .flatMap(ch -> ch.getAllCharacteristics()
                            .stream())
                    .map(v -> v.getTypeName() + "." + v.getValueName())
                    .collect(Collectors.joining(", "));
            annotations.add(new Annotation("Incoming: " + incoming, "tag", "#FFFFFF", tfg));
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

    private WebEditorDfd processDfd(DataFlowDiagram dataFlowDiagram, DataDictionary dataDictionary, Map<Node, List<Annotation>> mapNodeToAnnotation, List<ViolationTuple> violationTuples) {
        inputPinToFlowNamesMap = new HashMap<>();
        List<Child> children = new ArrayList<>();
        List<WebEditorLabelType> labelTypes = new ArrayList<>();

        behaviorConverter = new BehaviorConverter(dataDictionary);

        createLabelTypesAndValues(labelTypes, dataDictionary);

        createFlows(dataFlowDiagram, children);

        createNodes(dataFlowDiagram, children, mapNodeToAnnotation);
        
        // Convert violation tuples into web editor violation objects for the output model
        List<Violation> webViolations = createViolations(violationTuples);
        
        return new WebEditorDfd(new Model("graph", "root", children), labelTypes, readOnly ? "view" : "edit", new ArrayList<>(), webViolations);
    }
    
    /**
     * Converts a list of {@link ViolationTuple} objects into {@link Violation} objects
     * for use in the web editor format.
     * <p/>
     * For each violation, identifies the inducing vertices — the nodes where the violating
     * characteristic was first introduced — using {@link DataCharacteristicsSelector#isAddedToCharacteristics(AbstractVertex)}.
     * @param violationTuples List of violation tuples to convert
     * @return List of {@link Violation} objects containing constraint, flow graph, violated vertex,
     *         and inducing vertex information
     */
    private List<Violation> createViolations(List<ViolationTuple> violationTuples) {
    	List<Violation> violations = violationTuples.stream()
                .map(tuple -> {
                	DSLResult result = tuple.result();
                	AnalysisConstraint constraint = tuple.constraint();
                	
                	List<? extends AbstractVertex<?>> violatedVertices = result.getMatchedVertices(); 
                	List<? extends AbstractVertex<?>> tfg = result.getTransposeFlowGraph().getVertices();
                	
                	List<AbstractSelector> selectors = constraint.getDataSourceSelectors().getSelectors();
                	List<AbstractVertex<?>> inducingVertices = new ArrayList<>();
                	
                	for (AbstractSelector selector : selectors) {
                		for (AbstractVertex<?> vertex : tfg) {
                			boolean isNewlyAdded = ((DataCharacteristicsSelector) selector).isAddedToCharacteristics(vertex);
                			if (isNewlyAdded) {
                				inducingVertices.add(vertex);
                			}
                		}
                	}
                	
                	logger.info("violation found: " + 
                			"\nconstaint " + constraint.toString() + 
                			"\ntfg " + tfg.toString() +
                			"\nviolated " + violatedVertices +
                			"\ninducing " + inducingVertices.toString());
                	
                	return new Violation(constraint.toString(), tfg.toString(), violatedVertices.toString(), inducingVertices.toString());
                }
               )
                .toList();
    	return violations;
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
                    .forEach(pin -> ports.add(new Port(null, pin.getId(), "port:dfd-input", new ArrayList<>(), null, null)));

            Map<Pin, List<AbstractAssignment>> mapPinToAssignments = mapping(node);

            node.getBehavior()
                    .getOutPin()
                    .forEach(pin -> ports.add(new Port(createBehaviorString(mapPinToAssignments.get(pin)), pin.getId(), "port:dfd-output",
                            new ArrayList<>(), null, null)));
            if (mapNodeToAnnotation == null)
                children.add(new Child(text, labels, ports, id, type, null, null, null, new ArrayList<>(), null, null, null));
            else
                children.add(new Child(text, labels, ports, id, type, null, null, mapNodeToAnnotation.get(node), new ArrayList<>(),
                        new Position(0, 0), null, null));
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
        return new Child(text, null, null, id, type, sourceId, targetId, null, new ArrayList<>(), null, null, new ArrayList<>());
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
            hash = new byte[] {(byte) 0x80, (byte) 0x80, (byte) 0x80, 0};
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
