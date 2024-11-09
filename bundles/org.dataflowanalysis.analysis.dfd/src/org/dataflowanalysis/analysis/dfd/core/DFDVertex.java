package org.dataflowanalysis.analysis.dfd.core;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.dfd.datadictionary.AND;
import org.dataflowanalysis.dfd.datadictionary.AbstractAssignment;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.BinaryOperator;
import org.dataflowanalysis.dfd.datadictionary.ForwardingAssignment;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelReference;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.NOT;
import org.dataflowanalysis.dfd.datadictionary.OR;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.datadictionary.SetAssignment;
import org.dataflowanalysis.dfd.datadictionary.TRUE;
import org.dataflowanalysis.dfd.datadictionary.Term;
import org.dataflowanalysis.dfd.datadictionary.UnsetAssignment;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;

/**
 * This class represents a vertex references a node in the dfd model. Multiple dfd vertices may reference the same node
 */
public class DFDVertex extends AbstractVertex<Node> {
    protected final Map<Pin, DFDVertex> pinDFDVertexMap;
    protected final Map<Pin, Flow> pinFlowMap;

    /**
     * Creates a new vertex with the given referenced node and pin mappings
     * @param node Node that is referenced by the vertex
     * @param pinDFDVertexMap Map containing relationships between the pins of the vertex and previous vertices
     * @param pinFlowMap Map containing relationships between the pins of the vertex and the flows connecting the node to
     * other vertices
     */
    public DFDVertex(Node node, Map<Pin, DFDVertex> pinDFDVertexMap, Map<Pin, Flow> pinFlowMap) {
        super(node);
        this.pinDFDVertexMap = pinDFDVertexMap;
        this.pinFlowMap = pinFlowMap;
    }

    /**
     * Evaluates the given vertex by determining incoming and outgoing data characteristics and node characteristics
     */
    @Override
    public void evaluateDataFlow() {
        if (super.isEvaluated()) {
            return;
        }
        evaluatePreviousVertices();

        List<CharacteristicValue> vertexCharacteristics = determineNodeCharacteristics();

        Map<Pin, List<Label>> inputPinsIncomingLabelMap = new HashMap<>();
        this.getPinFlowMap()
                .keySet()
                .forEach(pin -> this.fillMapOfIncomingLabelsPerPin(pin, inputPinsIncomingLabelMap));

        List<DataCharacteristic> dataCharacteristics = new ArrayList<>(this.createDataCharacteristicsFromLabels(inputPinsIncomingLabelMap));

        Map<Pin, List<Label>> outputPinsOutgoingLabelMap = determineOutputPinOutgoingLabelMap(inputPinsIncomingLabelMap);

        List<DataCharacteristic> outgoingDataCharacteristics = new ArrayList<>(this.createDataCharacteristicsFromLabels(outputPinsOutgoingLabelMap));
        this.setPropagationResult(dataCharacteristics, outgoingDataCharacteristics, vertexCharacteristics);
    }

    /**
     * Determines the outgoing labels for each output pin of the vertex with the given map between incoming pins and their
     * labels
     * @param inputPinsIncomingLabelMap Map containing each input pin with their corresponding labels
     * @return Returns the map of output pins with their labels
     */
    private Map<Pin, List<Label>> determineOutputPinOutgoingLabelMap(Map<Pin, List<Label>> inputPinsIncomingLabelMap) {
        Map<Pin, List<Label>> outputPinsOutgoingLabelMap = new LinkedHashMap<>();
        var assignments = this.getReferencedElement()
                .getBehavior()
                .getAssignment();
        assignments.forEach(assignment -> outputPinsOutgoingLabelMap.putIfAbsent(assignment.getOutputPin(), new ArrayList<>()));
        assignments.forEach(assignment -> handleOutgoingAssignments(assignment, inputPinsIncomingLabelMap, outputPinsOutgoingLabelMap));
        return outputPinsOutgoingLabelMap;
    }

    /**
     * Determine node characteristics of the dfd vertex
     * @return Returns a list of all node characteristics that are applied at the vertex
     */
    private List<CharacteristicValue> determineNodeCharacteristics() {
        List<CharacteristicValue> nodeCharacteristics = new ArrayList<>();
        this.getReferencedElement()
                .getProperties()
                .forEach(label -> nodeCharacteristics.add(new DFDCharacteristicValue((LabelType) label.eContainer(), label)));
        return nodeCharacteristics;
    }

    /**
     * Evaluates previous vertices determined by {@link this#getPinDFDVertexMap()}
     */
    private void evaluatePreviousVertices() {
        Map<Pin, DFDVertex> previousVertices = this.getPinDFDVertexMap();
        previousVertices.keySet()
                .forEach(pin -> previousVertices.get(pin)
                        .evaluateDataFlow());
    }

    /**
     * Fills map mapping input pins to incoming labels
     * @param pin Pin to be evaluated
     * @param inputPinsIncomingLabelMap Map to be filled with incoming labels on pin
     */
    private void fillMapOfIncomingLabelsPerPin(Pin pin, Map<Pin, List<Label>> inputPinsIncomingLabelMap) {
        for (var previousVertex : this.getPinDFDVertexMap()
                .values()) {
            for (var dataFlowCharacteristics : previousVertex.getAllOutgoingDataCharacteristics()) {
                if (dataFlowCharacteristics.getVariableName()
                        .equals(this.getPinFlowMap()
                                .get(pin)
                                .getSourcePin()
                                .getId())) {
                    inputPinsIncomingLabelMap.putIfAbsent(pin, new ArrayList<>());
                    for (var characteristicValue : dataFlowCharacteristics.getAllCharacteristics()) {
                        inputPinsIncomingLabelMap.get(pin)
                                .add(((DFDCharacteristicValue) characteristicValue).getLabel());
                    }
                }
            }
        }
    }

    /**
     * Calculates outgoing labels for assignment and adds them into mapOutputPinToOutgoingLabels
     * @param abstractAssignment Assignment to be evaluated
     * @param inputPinsIncomingLabelMap Maps Input Pins to Incoming Labels
     * @param outputPinsOutgoingLabelMap Maps Output Pins to Outgoing Labels, to be filled by method
     */
    private void handleOutgoingAssignments(AbstractAssignment abstractAssignment, Map<Pin, List<Label>> inputPinsIncomingLabelMap,
            Map<Pin, List<Label>> outputPinsOutgoingLabelMap) {
        List<Label> incomingLabels = combineLabelsOnAllInputPins(abstractAssignment, inputPinsIncomingLabelMap);

        if (abstractAssignment instanceof ForwardingAssignment forwardingAssignment) {
            outputPinsOutgoingLabelMap.get(forwardingAssignment.getOutputPin())
                    .addAll(incomingLabels);
            return;
        } else if (abstractAssignment instanceof SetAssignment setAssignment) {
            outputPinsOutgoingLabelMap.get(abstractAssignment.getOutputPin())
                    .addAll(setAssignment.getOutputLabels());
            return;
        } else if (abstractAssignment instanceof UnsetAssignment unsetAssignment) {
            outputPinsOutgoingLabelMap.get(abstractAssignment.getOutputPin())
                    .removeAll(unsetAssignment.getOutputLabels());
            return;
        } else if (abstractAssignment instanceof Assignment assignment) {
            if (evaluateTerm(assignment.getTerm(), incomingLabels)) {
                outputPinsOutgoingLabelMap.get(assignment.getOutputPin())
                        .addAll(assignment.getOutputLabels());
            } else
                outputPinsOutgoingLabelMap.get(abstractAssignment.getOutputPin())
                        .removeAll(assignment.getOutputLabels());
        }

    }

    /**
     * Create data characteristics from Map mapping Input/Output Pin to labels. Important: The name of the data
     * characteristic is equal to the id of the pin. Any changes in the data characteristics naming scheme will require
     * changes in the evaluation logic
     * @param pinToLabelMap Map mapping Input/Output Pin to labels
     * @return List of created data characteristics
     */
    private List<DataCharacteristic> createDataCharacteristicsFromLabels(Map<Pin, List<Label>> pinToLabelMap) {
        return pinToLabelMap.keySet()
                .stream()
                .map(pin -> new DataCharacteristic(pin.getId(), this.getCharacteristicValuesForPin(pin, pinToLabelMap)))
                .toList();
    }

    /**
     * Determines the characteristic values present for a pin, given the mapping
     * @param pin Pin of which the characteristic values shall be calculated
     * @param pinToLabelMap Mapping of a pin to the assigned labels
     * @return Returns a list of characteristic values assigned to the given pin
     */
    private List<CharacteristicValue> getCharacteristicValuesForPin(Pin pin, Map<Pin, List<Label>> pinToLabelMap) {
        return pinToLabelMap.get(pin)
                .stream()
                .map(label -> new DFDCharacteristicValue((LabelType) label.eContainer(), label))
                .filter(distinctByKey(CharacteristicValue::getValueId))
                .collect(Collectors.toList());
    }

    /**
     * Combines all Incoming Labels from relevant input pins
     * @param assignment Assignment to determine relevant input pins
     * @param inputPinsIncomingLabelMap Maps all input pins to all incoming labels
     * @return List of relevant labels
     */
    private static List<Label> combineLabelsOnAllInputPins(AbstractAssignment abstractAssignment, Map<Pin, List<Label>> inputPinsIncomingLabelMap) {
        List<Label> allLabel = new ArrayList<>();
        if (abstractAssignment instanceof SetAssignment || abstractAssignment instanceof UnsetAssignment)
            return allLabel;
        else if (abstractAssignment instanceof Assignment assignment) {
            for (var inputPin : assignment.getInputPins()) {
                allLabel.addAll(inputPinsIncomingLabelMap.getOrDefault(inputPin, new ArrayList<>()));
            }
        } else if (abstractAssignment instanceof ForwardingAssignment forwardingAssignment) {
            for (var inputPin : forwardingAssignment.getInputPins()) {
                allLabel.addAll(inputPinsIncomingLabelMap.getOrDefault(inputPin, new ArrayList<>()));
            }
        }

        return allLabel;
    }

    /**
     * Filters objects to be distinct by key
     * @param <T> Type of the Object to be evaluated
     * @param keyExtractor Mapping of an element to the value that is required to be unique
     * @return Returns a predicate evaluating whether the value has been seen
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        KeySetView<Object, Boolean> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    /**
     * Evaluate Assignment Term with List of Incoming Labels
     * @param term Term to be evaluated
     * @param inputLabel Incoming Label
     * @return Evaluation
     */
    private static boolean evaluateTerm(Term term, List<Label> inputLabel) {
        if (term instanceof TRUE) {
            return true;
        } else if (term instanceof NOT notTerm) {
            return !evaluateTerm(notTerm.getNegatedTerm(), inputLabel);
        } else if (term instanceof LabelReference) {
            return inputLabel.contains(((LabelReference) term).getLabel());
        } else if (term instanceof BinaryOperator binaryTerm) {
            if (binaryTerm instanceof AND) {
                return evaluateTerm(binaryTerm.getTerms()
                        .get(0), inputLabel) && evaluateTerm(
                                binaryTerm.getTerms()
                                        .get(1),
                                inputLabel);
            } else if (binaryTerm instanceof OR) {
                return evaluateTerm(binaryTerm.getTerms()
                        .get(0), inputLabel) || evaluateTerm(
                                binaryTerm.getTerms()
                                        .get(1),
                                inputLabel);
            }
        }

        return false;
    }

    /**
     * Goes through the previous vertices and replaces equal vertices by the same vertex
     * @param vertices Set of unique vertices that are used to replace equal vertices
     */
    public void unify(Set<DFDVertex> vertices) {
        for (var key : this.getPinDFDVertexMap()
                .keySet()) {
            for (var vertex : vertices) {
                if (vertex.equals(this.getPinDFDVertexMap()
                        .get(key))) {
                    this.getPinDFDVertexMap()
                            .put(key, vertex);
                }
            }
            vertices.add(this.getPinDFDVertexMap()
                    .get(key));
        }
        this.getPreviousElements()
                .forEach(vertex -> ((DFDVertex) vertex).unify(vertices));
    }

    /**
     * Creates a clone of the vertex without considering data characteristics nor vertex characteristics
     */
    public DFDVertex copy(Map<DFDVertex, DFDVertex> mapping) {
        Map<Pin, DFDVertex> copiedPinDFDVertexMap = new HashMap<>();
        this.pinDFDVertexMap.keySet()
                .forEach(key -> {
                    var oldVertex = this.pinDFDVertexMap.get(key);
                    var newVertice = mapping.getOrDefault(oldVertex, this.pinDFDVertexMap.get(key)
                            .copy(mapping));
                    copiedPinDFDVertexMap.put(key, newVertice);
                    mapping.putIfAbsent(oldVertex, newVertice);
                });
        return new DFDVertex(this.referencedElement, copiedPinDFDVertexMap, new HashMap<>(this.pinFlowMap));
    }

    @Override
    public UUID getUniqueUUID() {
        StringBuilder previousElements = new StringBuilder();
        for (var previousElement : this.getPinDFDVertexMap().entrySet()) {
            previousElements.append(previousElement.getKey().getEntityName()).append(previousElement.getValue().getUniqueUUID());
        }
        String identifier = this.referencedElement.getId() + previousElements;
        return UUID.nameUUIDFromBytes(identifier.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", this.referencedElement.getEntityName(), this.referencedElement.getId());
    }

    @Override
    public boolean equals(Object other) {
        if (super.equals(other))
            return true;
        if (!(other instanceof DFDVertex vertex))
            return false;
        if (!this.referencedElement.equals(vertex.getReferencedElement()))
            return false;
        for (var key : this.getPinDFDVertexMap()
                .keySet()) {
            if (!this.getPinDFDVertexMap()
                    .get(key)
                    .equals(vertex.getPinDFDVertexMap()
                            .get(key)))
                return false;
        }
        return true;
    }

    @Override
    public List<AbstractVertex<?>> getPreviousElements() {
        return (new HashSet<AbstractVertex<?>>(this.pinDFDVertexMap.values())).stream()
                .toList();
    }

    /**
     * Returns the mapping between pins of the node and the connected previous vertices
     * @return Return the mapping between pins and previous vertices
     */
    public Map<Pin, DFDVertex> getPinDFDVertexMap() {
        return pinDFDVertexMap;
    }

    /**
     * Returns the mapping between pins of the node and the connected input flows connecting the vertex to the previous
     * vertices
     * @return Returns the mapping between pins and incoming flows
     */
    public Map<Pin, Flow> getPinFlowMap() {
        return pinFlowMap;
    }

    /**
     * Returns the name of the dfd vertex
     * @return Returns the name of the vertex
     */
    public String getName() {
        return this.getReferencedElement()
                .getEntityName();
    }
}
