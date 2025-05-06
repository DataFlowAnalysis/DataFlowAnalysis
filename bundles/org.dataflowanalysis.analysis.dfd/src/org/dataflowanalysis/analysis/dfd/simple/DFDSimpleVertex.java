package org.dataflowanalysis.analysis.dfd.simple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.dfd.core.DFDCharacteristicValue;
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
public class DFDSimpleVertex extends AbstractVertex<Node> {
    private final Set<DFDSimpleVertex> previousVertices;
    private final Map<Pin, Flow> mapPinToFlow;

    /**
     * Creates a new vertex with the given referenced node and pin mappings
     * @param node Node that is referenced by the vertex
     * @param previousVertices list of previous vertices
     * @param mapPinToFlow Map containing relationships between the pins of the vertex and the flows connecting the node to
     * other vertices
     */
    public DFDSimpleVertex(Node node, Set<DFDSimpleVertex> previousVertices, Map<Pin, Flow> mapPinToFlow) {
        super(node);
        this.previousVertices = previousVertices;
        this.mapPinToFlow = mapPinToFlow;
    }

    /**
     * Evaluates the given vertex by determining incoming and outgoing data characteristics and node characteristics
     */
    @Override
    public void evaluateDataFlow() {
        if (super.isEvaluated()) {
            return;
        }

        previousVertices.forEach(DFDSimpleVertex::evaluateDataFlow);

        List<CharacteristicValue> vertexCharacteristics = determineNodeCharacteristics();

        List<DataCharacteristic> incomingCharacteristics = previousVertices.stream()
                .map(AbstractVertex::getAllOutgoingDataCharacteristics)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        Map<Pin, Set<Label>> outgoingLabelPerPin = new HashMap<>();
        referencedElement.getBehavior()
                .getAssignment()
                .forEach(it -> handleOutgoingAssignments(it, incomingCharacteristics, outgoingLabelPerPin));

        List<DataCharacteristic> outgoingDataCharacteristics = new ArrayList<>(this.createDataCharacteristicsFromLabels(outgoingLabelPerPin));

        this.setPropagationResult(incomingCharacteristics, outgoingDataCharacteristics, vertexCharacteristics);
    }

    /**
     * Determine node characteristics of the vertex
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
     * Calculates outgoing labels for assignment and adds them into mapOutputPinToOutgoingLabels
     * @param abstractAssignment Assignment to be evaluated
     * @param incomingDataCharacteristics incoming characteristics as list
     * @param outgoingLabelPerPin Maps Output Pins to Outgoing Labels, to be filled by method
     */
    private void handleOutgoingAssignments(AbstractAssignment abstractAssignment, List<DataCharacteristic> incomingDataCharacteristics,
            Map<Pin, Set<Label>> outgoingLabelPerPin) {
        // Takes the labels of all incoming Characteristics whos names match the flows arriving on all input pins of the
        // assignment
        var incomingLabels = incomingDataCharacteristics.stream()
                .filter(it -> {
                    return mapPinToFlow.keySet()
                            .stream()
                            .filter(key -> {
                                if (abstractAssignment instanceof UnsetAssignment || abstractAssignment instanceof SetAssignment)
                                    return false;
                                if (abstractAssignment instanceof Assignment assignment)
                                    return assignment.getInputPins()
                                            .contains(key);
                                else
                                    return abstractAssignment instanceof ForwardingAssignment forwardingAssignment
                                            && forwardingAssignment.getInputPins()
                                                    .contains(key);
                            })
                            .map(key -> mapPinToFlow.get(key)
                                    .getEntityName())
                            .toList()
                            .contains(it.getVariableName());
                })
                .flatMap(it -> it.getAllCharacteristics()
                        .stream()
                        .map(value -> ((DFDCharacteristicValue) value).getLabel()))
                .collect(Collectors.toSet());

        var outPin = abstractAssignment.getOutputPin();
        if (outPin == null) {
            return;
        }
        outgoingLabelPerPin.computeIfAbsent(outPin, k -> new LinkedHashSet<>());

        if (abstractAssignment instanceof ForwardingAssignment forwardingAssignment) {
            outgoingLabelPerPin.get(forwardingAssignment.getOutputPin())
                    .addAll(incomingLabels);
            return;
        } else if (abstractAssignment instanceof SetAssignment setAssignment) {
            outgoingLabelPerPin.get(abstractAssignment.getOutputPin())
                    .addAll(setAssignment.getOutputLabels());
            return;
        } else if (abstractAssignment instanceof UnsetAssignment unsetAssignment) {
            outgoingLabelPerPin.get(abstractAssignment.getOutputPin())
                    .removeAll(unsetAssignment.getOutputLabels());
            return;
        } else if (abstractAssignment instanceof Assignment assignment) {
            if (evaluateTerm(assignment.getTerm(), incomingLabels)) {
                outgoingLabelPerPin.get(assignment.getOutputPin())
                        .addAll(assignment.getOutputLabels());
            } else
                outgoingLabelPerPin.get(assignment.getOutputPin())
                        .removeAll(assignment.getOutputLabels());
        }
    }

    /**
     * Create data characteristics from Map mapping Input/Output Pin to labels. Important: The name of the data
     * characteristic is equal to the name of the flow.
     * @param pinToLabelMap Map mapping Input/Output Pin to labels
     * @return List of created data characteristics
     */
    private List<DataCharacteristic> createDataCharacteristicsFromLabels(Map<Pin, Set<Label>> pinToLabelMap) {
        return pinToLabelMap.keySet()
                .stream()
                .map(pin -> new DataCharacteristic(mapPinToFlow.get(pin)
                        .getEntityName(), new ArrayList<CharacteristicValue>(this.getCharacteristicValuesForPin(pin, pinToLabelMap))))
                .filter(it -> it.getAllCharacteristics()
                        .size() > 0)
                .toList();
    }

    /**
     * Determines the characteristic values present for a pin, given the mapping
     * @param pin Pin of which the characteristic values shall be calculated
     * @param pinToLabelMap Mapping of a pin to the assigned labels
     * @return Returns a list of characteristic values assigned to the given pin
     */
    private List<CharacteristicValue> getCharacteristicValuesForPin(Pin pin, Map<Pin, Set<Label>> pinToLabelMap) {
        return pinToLabelMap.get(pin)
                .stream()
                .map(label -> new DFDCharacteristicValue((LabelType) label.eContainer(), label))
                .filter(distinctByKey(CharacteristicValue::getValueId))
                .collect(Collectors.toList());
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
    private static boolean evaluateTerm(Term term, Set<Label> inputLabel) {
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
    public void unify(Set<DFDSimpleVertex> vertices) {
        List<DFDSimpleVertex> toRemove = new ArrayList<>();
        List<DFDSimpleVertex> toAdd = new ArrayList<>();
        previousVertices.forEach(it -> {
            for (var vertex : vertices) {
                if (it.equals(vertex)) {
                    toRemove.add(it);
                    toAdd.add(vertex);
                }
            }
        });
        previousVertices.removeAll(toRemove);
        previousVertices.addAll(toAdd);
        vertices.addAll(previousVertices);

        this.getPreviousElements()
                .forEach(vertex -> ((DFDSimpleVertex) vertex).unify(vertices));
    }

    /**
     * Creates a clone of the vertex without considering data characteristics nor vertex characteristics
     */
    public DFDSimpleVertex copy(Map<DFDSimpleVertex, DFDSimpleVertex> mapping) {
        Set<DFDSimpleVertex> previousVerticesNew = new LinkedHashSet<>();
        this.previousVertices.forEach(it -> {
            var newVertice = mapping.getOrDefault(it, it.copy(mapping));
            previousVerticesNew.add(newVertice);
            mapping.putIfAbsent(it, newVertice);
        });
        return new DFDSimpleVertex(this.referencedElement, previousVerticesNew, new HashMap<>(this.mapPinToFlow));
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", this.referencedElement.getEntityName(), this.referencedElement.getId());
    }

    @Override
    public List<AbstractVertex<Node>> getPreviousElements() {
        return previousVertices.stream()
                .map(it -> (AbstractVertex<Node>) it)
                .toList();
    }

    public boolean equalsSemantically(DFDSimpleVertex other) {
        if (this.equals(other))
            return true;
        if (!this.mapPinToFlow.equals(other.getPinFlowMap()))
            return false;
        if (this.previousVertices.size() == 0 && other.previousVertices.size() == 0)
            return true;
        return this.previousVertices.stream()
                .allMatch(previousVertex -> {
                    return other.getPreviousElements()
                            .stream()
                            .map(DFDSimpleVertex.class::cast)
                            .anyMatch(it -> it.equalsSemantically(previousVertex));
                });
    }

    /**
     * Returns the mapping between pins of the node and the connected input flows connecting the vertex to the previous
     * vertices
     * @return Returns the mapping between pins and incoming flows
     */
    public Map<Pin, Flow> getPinFlowMap() {
        return mapPinToFlow;
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
