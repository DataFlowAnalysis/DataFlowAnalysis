package org.dataflowanalysis.converter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.TransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.resource.DFDURIResourceProvider;
import org.dataflowanalysis.analysis.dfd.simple.DFDSimpleTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.utils.ResourceUtils;
import org.dataflowanalysis.converter.webdfd.*;
import org.dataflowanalysis.dfd.datadictionary.*;
import org.dataflowanalysis.dfd.dataflowdiagram.*;
import org.dataflowanalysis.dfd.dataflowdiagram.Process;
import org.eclipse.emf.common.util.URI;
import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;

/**
 * Converts data flow diagrams and dictionaries between web editor formats and the application's internal
 * representation. Inherits from {@link Converter} to utilize shared conversion logic while providing specific
 * functionality for handling data flow diagram formats. Supports loading from and storing to files, and conversion
 * between different data representation formats.
 */
public class DataFlowDiagramConverter extends Converter {

	private Map<Pin, List<String>> inputPinToFlowNamesMap;

    private final Logger logger = Logger.getLogger(DataFlowDiagramConverter.class);
    protected final static String DELIMITER_PIN_NAME = "|";
    protected final static String DELIMITER_MULTI_PIN = ",";

    private BehaviorConverter behaviorConverter;

    /**
     * Converts Data Flow Diagram and Data Dictionary provided via paths into a WebEditorDfd object, analyzes it and annotates the propagated labels in the WebDFD.
     * @param inputDataFlowDiagram The path of the data flow diagram.
     * @param inputDataDictionary The path of the data dictionary.
     * @return WebEditorDfd object representing the web editor version of the data flow diagram.
     * @throws StandaloneInitializationException
     */
    public WebEditorDfd dfdToWeb(String project, String inputDataFlowDiagram, String inputDataDictionary, Class<?> activator)
            throws StandaloneInitializationException {
        DataFlowDiagramAndDictionary complete = loadDFD(project, inputDataFlowDiagram, inputDataDictionary, activator);
        return processDfd(complete.dataFlowDiagram(), complete.dataDictionary(), createNodeAnnotationMap(complete, null, null));
    }

    /**
     * Converts a DataFlowDiagramAndDictionary object into a WebEditorDfd object, analyzes it and annotates the propagated labels in the WebDFD.
     * @param complete The DataFlowDiagramAndDictionary object to convert.
     * @return WebEditorDfd object representing the web editor version of the data flow diagram.
     */
    public WebEditorDfd dfdToWeb(DataFlowDiagramAndDictionary complete) {
        return processDfd(complete.dataFlowDiagram(), complete.dataDictionary(), createNodeAnnotationMap(complete, null, null));
    }
    
    /**
     * Converts Data Flow Diagram and Data Dictionary provided via paths into a WebEditorDfd object, analyzes it, checks for the constraints and annotates the WebDFD.
     * @param inputDataFlowDiagram The path of the data flow diagram.
     * @param inputDataDictionary The path of the data dictionary.
     * @return WebEditorDfd object representing the web editor version of the data flow diagram.
     * @throws StandaloneInitializationException
     */
    public WebEditorDfd dfdToWebAndAnalyzeAndAnnotate(String project, String inputDataFlowDiagram, String inputDataDictionary, Class<?> activator, List<Predicate<? super AbstractVertex<?>>> conditions)
            throws StandaloneInitializationException {
        DataFlowDiagramAndDictionary complete = loadDFD(project, inputDataFlowDiagram, inputDataDictionary, activator);       
        
        return processDfd(complete.dataFlowDiagram(), complete.dataDictionary(), createNodeAnnotationMap(complete, conditions, null));
    }

    /**
     * Converts a DataFlowDiagramAndDictionary object into a WebEditorDfd object, analyzes it, checks for the constraints and annotates the WebDFD.
     * @param complete The DataFlowDiagramAndDictionary object to convert.
     * @return WebEditorDfd object representing the web editor version of the data flow diagram.
     */
    public WebEditorDfd dfdToWebAndAnalyzeAndAnnotate(DataFlowDiagramAndDictionary complete, List<Predicate<? super AbstractVertex<?>>> conditions) {
        return processDfd(complete.dataFlowDiagram(), complete.dataDictionary(), createNodeAnnotationMap(complete, conditions, null));
    }
    
    /**
     * Converts Data Flow Diagram and Data Dictionary provided via paths into a WebEditorDfd object, analyzes it with a custom Finder, checks for the constraints and annotates the WebDFD.
     * @param inputDataFlowDiagram The path of the data flow diagram.
     * @param inputDataDictionary The path of the data dictionary.
     * @return WebEditorDfd object representing the web editor version of the data flow diagram.
     * @throws StandaloneInitializationException
     */
    public WebEditorDfd dfdToWebAndAnalyzeAndAnnotateWithCustomTFGFinder(String project, String inputDataFlowDiagram, String inputDataDictionary, Class<?> activator, List<Predicate<? super AbstractVertex<?>>> conditions, Class<? extends TransposeFlowGraphFinder> finderClass)
            throws StandaloneInitializationException {
        DataFlowDiagramAndDictionary complete = loadDFD(project, inputDataFlowDiagram, inputDataDictionary, activator);       
        
        return processDfd(complete.dataFlowDiagram(), complete.dataDictionary(), createNodeAnnotationMap(complete, conditions, finderClass));
    }

    /**
     * Converts a DataFlowDiagramAndDictionary object into a WebEditorDfd object, analyzes it with a custom Finder, checks for the constraints and annotates the WebDFD.
     * @param complete The DataFlowDiagramAndDictionary object to convert.
     * @param conditions List of constraints
     * @param finderClass Custom TFG Finder
     * @return WebEditorDfd object representing the web editor version of the data flow diagram.
     */
    public WebEditorDfd dfdToWebAndAnalyzeAndAnnotateWithCustomTFGFinder(DataFlowDiagramAndDictionary complete, List<Predicate<? super AbstractVertex<?>>> conditions, Class<? extends TransposeFlowGraphFinder> finderClass) {
        return processDfd(complete.dataFlowDiagram(), complete.dataDictionary(), createNodeAnnotationMap(complete, conditions, finderClass));
    }
    
    /**
     * Stores a WebEditorDfd object into a specified output file.
     * @param web The WebEditorDfd object to store.
     * @param outputFile The path of the output file.
     */
    public void storeWeb(WebEditorDfd web, String outputFile) {
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
     * Loads a data flow diagram and data dictionary from specified input files and returns them as a combined object.
     * @param inputDataFlowDiagram The path of the input data flow diagram file.
     * @param inputDataDictionary The path of the input data dictionary file.
     * @return DataFlowDiagramAndDictionary object representing the loaded data flow diagram and dictionary.
     */
    public DataFlowDiagramAndDictionary loadDFD(String project, String inputDataFlowDiagram, String inputDataDictionary, Class<?> activator)
            throws StandaloneInitializationException {
        StandaloneInitializerBuilder.builder()
                .registerProjectURI(activator, project)
                .build()
                .init();

        URI dfdURI = ResourceUtils.createRelativePluginURI(inputDataFlowDiagram, project);
        URI ddURI = ResourceUtils.createRelativePluginURI(inputDataDictionary, project);

        var provider = new DFDURIResourceProvider(dfdURI, ddURI);
        provider.loadRequiredResources();
        return new DataFlowDiagramAndDictionary(provider.getDataFlowDiagram(), provider.getDataDictionary());
    }
    
    /**
     * Creates the node annotations by analyzing the DFD
     * @param complete DFD / DD combination
     * @param conditions List of constraints (optional)
     * @param finderClass Custom TFG Finder (optional)
     * @return
     */
    private Map<Node, Annotation> createNodeAnnotationMap (DataFlowDiagramAndDictionary complete, List<Predicate<? super AbstractVertex<?>>> conditions, Class<? extends TransposeFlowGraphFinder> finderClass) {
    	TransposeFlowGraphFinder finder;
    	if (finderClass == null) finder = new DFDTransposeFlowGraphFinder(complete.dataDictionary(), complete.dataFlowDiagram());
    	else {
    		if (finderClass.equals(DFDSimpleTransposeFlowGraphFinder.class))
            	finder = new DFDSimpleTransposeFlowGraphFinder(complete.dataDictionary(), complete.dataFlowDiagram());
            else
                finder = new DFDTransposeFlowGraphFinder(complete.dataDictionary(), complete.dataFlowDiagram());
    			 
    	}
         var collection = finder.findTransposeFlowGraphs();
         collection = collection.stream().map(AbstractTransposeFlowGraph::evaluate).toList();
         
         Map<Node, Annotation> mapNodeToAnnotations = new HashMap<>();
         Map<Node, Set<String>> mapNodeToPropagatedLabels = new HashMap<>();
         collection.stream().forEach(tfg -> tfg.getVertices().forEach(vertex -> {
         	Node node = (Node) vertex.getReferencedElement();        	
         	mapNodeToPropagatedLabels.putIfAbsent(node, new HashSet<>());
         	var label = mapNodeToPropagatedLabels.get(node);
         	vertex.getAllOutgoingDataCharacteristics().forEach(characteristic -> characteristic.getAllCharacteristics().forEach(value -> {
         		label.add(value.getTypeName() + "." + value.getValueName());
         	}));         	
         }));
         
         mapNodeToPropagatedLabels.keySet().forEach(key -> {
        	StringBuilder builder = new StringBuilder();
	      	builder.append("PropagatedLabels:").append("\n");
	      	
	      	mapNodeToPropagatedLabels.get(key).forEach(value -> {
	      		builder.append(value).append("\n");
	      	});
	      	if (!mapNodeToPropagatedLabels.get(key).isEmpty())mapNodeToAnnotations.put(key, new Annotation(builder.toString(), "tag", "#FFFFFF"));
        });
        
         
         if (conditions == null) return mapNodeToAnnotations;
         
         DFDConfidentialityAnalysis analysis = new DFDConfidentialityAnalysis(null, null, null);
         for (int i = 0; i < conditions.size(); i++) {
         	var condition = conditions.get(i);
         	if (condition == null) continue;
         	for (var tfg : collection) {         		
         		var violations = analysis.queryDataFlow(tfg, condition);
         		for (var vertex : violations) {
         			Node node = (Node)vertex.getReferencedElement();   	
                 	StringBuilder builder = new StringBuilder();
                 	if (mapNodeToAnnotations.get(node) != null) builder.append(mapNodeToAnnotations.get(node).message()).append("\n");
                 	builder.append("Violation: Constraint ").append(i).append(" violated.").append("\n");
                 	mapNodeToAnnotations.put(node, new Annotation(builder.toString(), "bolt", "#ff0000"));
         		}
         	}
         }
         return mapNodeToAnnotations;
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

    private WebEditorDfd processDfd(DataFlowDiagram dataFlowDiagram, DataDictionary dataDictionary, Map<Node, Annotation> mapNodeToAnnotation) {
        inputPinToFlowNamesMap = new HashMap<>();
        List<Child> children = new ArrayList<>();
        List<WebEditorLabelType> labelTypes = new ArrayList<>();

        behaviorConverter = new BehaviorConverter(dataDictionary);

        createLabelTypesAndValues(labelTypes, dataDictionary);

        createFlows(dataFlowDiagram, children);

        createNodes(dataFlowDiagram, children, mapNodeToAnnotation);

        return new WebEditorDfd(new Model("graph", "root", children), labelTypes, "edit");
    }

    private void createNodes(DataFlowDiagram dataFlowDiagram, List<Child> children, Map<Node, Annotation> mapNodeToAnnotation) {
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

            node.getBehaviour()
                    .getInPin()
                    .forEach(pin -> ports.add(new Port(null, pin.getId(), "port:dfd-input", new ArrayList<>())));

            Map<Pin, List<AbstractAssignment>> mapPinToAssignments = mapping(node);

            node.getBehaviour()
                    .getOutPin()
                    .forEach(pin -> ports
                            .add(new Port(createBehaviourString(mapPinToAssignments.get(pin)), pin.getId(), "port:dfd-output", new ArrayList<>())));
            if (mapNodeToAnnotation == null)
            	children.add(new Child(text, labels, ports, id, type, null, null, null, new ArrayList<>()));
            else 
            	children.add(new Child(text, labels, ports, id, type, null, null, mapNodeToAnnotation.get(node), new ArrayList<>()));
        }
    }

    private void createFlows(DataFlowDiagram dataFlowDiagram, List<Child> children) {
        for (Flow flow : dataFlowDiagram.getFlows()) {
        	fillPinToFlowNamesMap(inputPinToFlowNamesMap,flow);
            children.add(createFlow(flow));
        }
    }

    private Child createFlow(Flow flow) {
        String id = flow.getId();
        String type = "edge:arrow";
        String sourceId = flow.getSourcePin()
                .getId();
        String targetId = flow.getDestinationPin()
                .getId();
        String text = flow.getEntityName();
        return new Child(text, null, null, id, type, sourceId, targetId, null, new ArrayList<>());
    }

    private Map<Pin, List<AbstractAssignment>> mapping(Node node) {
        Map<Pin, List<AbstractAssignment>> mapPinToAssignments = new HashMap<>();

        for (AbstractAssignment assignment : node.getBehaviour()
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

    private String createBehaviourString(List<AbstractAssignment> abstractAssignments) {
        if (abstractAssignments == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (AbstractAssignment abstractAssignment : abstractAssignments) {
            if (abstractAssignment instanceof ForwardingAssignment) {
                builder.append("Forwarding({");                
                builder.append(getStringFromInputPins(abstractAssignment.getInputPins()));
                builder.append("})");
            } else {
                Assignment assignment = (Assignment) abstractAssignment;
                
                builder.append("Assignment({");
                builder.append(getStringFromInputPins(assignment.getInputPins()));
                builder.append("};");
                builder.append(behaviorConverter.termToString(assignment.getTerm()));
                builder.append(";{");
                
                List<String> outLabelAsString = new ArrayList<>();
                assignment.getOutputLabels().forEach(label -> {
                	outLabelAsString.add(((LabelType)label.eContainer()).getEntityName() + "." + label.getEntityName());
                });
                builder.append(String.join(DELIMITER_MULTI_PIN, outLabelAsString));
                
                builder.append("})");                
            }
            builder.append("\n");
        }
        return builder.toString()
                .trim();
    }
    
    private String getStringFromInputPins (List<Pin> inputPins) {
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
