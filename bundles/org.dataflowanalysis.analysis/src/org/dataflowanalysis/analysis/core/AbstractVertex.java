package org.dataflowanalysis.analysis.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;

/**
 * This class represents an abstract vertex in a {@link AbstractPartialFlowGraph}. An abstract vertex represents an
 * element in a partial flow graph and links to an element. An element referenced in this way may be referenced multiple
 * times by different abstract vertices. Furthermore, the abstract vertex saved incoming and outgoing data flow
 * variables and the characteristics present at the vertex.
 * @param <T> Type parameter representing the type of the stored object
 */
public abstract class AbstractVertex<T> {
    private final Logger logger = Logger.getLogger(AbstractVertex.class);

    protected final T referencedElement;

    private Optional<List<DataFlowVariable>> incomingDataFlowVariables;
    private Optional<List<DataFlowVariable>> outgoingDataFlowVariables;
    private Optional<List<CharacteristicValue>> vertexCharacteristics;

    /**
     * Constructs a new action sequence element with empty data flow variables and node characteristics
     */
    public AbstractVertex(T referencedElement) {
        this.referencedElement = referencedElement;
        this.incomingDataFlowVariables = Optional.empty();
        this.outgoingDataFlowVariables = Optional.empty();
        this.vertexCharacteristics = Optional.empty();
    }

    /**
     * Evaluates the data flow at a vertex by looking and evaluating previous elements, then setting the incoming and
     * outgoing data flow variables as well as vertex characteristics.
     */
    public abstract void evaluateDataFlow();

    @Override
    public abstract String toString();

    /**
     * Sets the propagation result of the Vertex to the given result. This method should only be called once on elements
     * that are not evaluated.
     * @param incomingDataFlowVariables Incoming data flow variables that flow into the vertex
     * @param outgoingDataFlowVariables Outgoing data flow variables that flow out of the vertex
     * @param vertexCharacteristics Vertex characteristics present at the node
     */
    protected void setPropagationResult(List<DataFlowVariable> incomingDataFlowVariables, List<DataFlowVariable> outgoingDataFlowVariables,
            List<CharacteristicValue> vertexCharacteristics) {
        if (this.isEvaluated()) {
            logger.error("Cannot set propagation result of already evaluated vertex");
            throw new IllegalArgumentException();
        }
        this.incomingDataFlowVariables = Optional.of(new ArrayList<>(incomingDataFlowVariables));
        this.outgoingDataFlowVariables = Optional.of(new ArrayList<>(outgoingDataFlowVariables));
        this.vertexCharacteristics = Optional.of(new ArrayList<>(vertexCharacteristics));
    }

    /**
     * Returns whether the action sequence element has been evaluated
     * @return Returns true, if the node is evaluated. Otherwise, the method returns false
     */
    public boolean isEvaluated() {
        return this.incomingDataFlowVariables.isPresent() && this.outgoingDataFlowVariables.isPresent() && this.vertexCharacteristics.isPresent();
    }

    /**
     * Returns the referenced model element by the abstract vertex. Multiple vertices may reference the same model element,
     * but one vertex always references exactly one model element.
     * @return Returns the reference element by the vertex
     */
    public T getReferencedElement() {
        return referencedElement;
    }

    /**
     * Returns a list of all data flow variables that are present for the action sequence element
     * @return List of present data flow variables
     */
    public List<DataFlowVariable> getAllDataFlowVariables() {
        return this.incomingDataFlowVariables.orElseThrow(IllegalStateException::new);
    }

    /**
     * Returns a list of all incoming data flow variables that are present for the action sequence element
     * @return List of present incoming data flow variables (e.g. the variables at the input pin of the DFD representation)
     */
    public List<DataFlowVariable> getAllIncomingDataFlowVariables() {
        return this.incomingDataFlowVariables.orElseThrow(IllegalStateException::new);
    }

    /**
     * Returns a list of all outgoing data flow variables that are present for the action sequence element
     * @return List of present outgoing data flow variables (e.g. the variables at the output pin of the DFD representation)
     */
    public List<DataFlowVariable> getAllOutgoingDataFlowVariables() {
        return this.outgoingDataFlowVariables.orElseThrow(IllegalStateException::new);
    }

    /**
     * Returns a list of all present node characteristics for the action sequence element
     * @return List of present node characteristics
     */
    public List<CharacteristicValue> getAllNodeCharacteristics() {
        return this.vertexCharacteristics.orElseThrow(IllegalStateException::new);
    }

    /**
     * Returns the list of previous elements that precede the vertex
     * @return Returns a list of all preceding vertices of the vertex.
     */
    public abstract List<? extends AbstractVertex<?>> getPreviousElements();

    /**
     * Returns whether the vertex is a source (e.g. does not have a source)
     * @return Returns true, if the vertex is a source. Otherwise, the method returns false
     */
    public boolean isSource() {
        return this.getPreviousElements().isEmpty();
    }

    /**
     * Returns a list of characteristic literals that are set for a given characteristic type in the list of all node
     * characteristics
     * <p>
     * See {@link AbstractVertex#getDataFlowCharacteristicNamesWithType(String)} for a similar method for data flow variables
     * @param name Name of the characteristic type
     * @return Returns a list of all characteristic literals matching the characteristic type
     */
    public List<String> getNodeCharacteristicNamesWithName(String name) {
        return this.getAllNodeCharacteristics().stream().filter(cv -> cv.getTypeName().equals(name))
                .map(CharacteristicValue::getValueName)
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of characteristic literals that are set for a given characteristic type in the list of all node
     * characteristics  
     * <p>
     * See {@link AbstractVertex#getDataFlowCharacteristicIdsWithType(String)} for a similar method for data flow variables
     * @param name Name of the characteristic type
     * @return Returns a list of all characteristic literals matching the characteristic type
     */
    public List<String> getNodeCharacteristicIdsWithName(String name) {
        return this.getAllNodeCharacteristics().stream().filter(cv -> cv.getTypeName().equals(name))
                .map(CharacteristicValue::getValueId)
                .collect(Collectors.toList());
    }

    /**
     * Returns a List of IDs of characteristics and data flow variables that are set for a given characteristic type in the
     * list of all data flow variables
     * <p>
     * See {@link AbstractVertex#getNodeCharacteristicIdsWithName(String)} for a similar method for node characteristics
     * @param type Name of the characteristic type
     * @return Returns a list of all characteristic literals matching the characteristic type
     */
    public List<List<String>> getDataFlowCharacteristicIdsWithType(String type) {
        List<List<String>> dfCharIds = new ArrayList<>();
        for (DataFlowVariable df : this.getAllDataFlowVariables()) {
            List<String> charValueIds = new ArrayList<>();
            for (CharacteristicValue charValue : df.getAllCharacteristics()) {
                if (charValue.getTypeName().equals(type)) {
                    charValueIds.add(charValue.getValueId());
                }
            }
            dfCharIds.add(charValueIds);
        }

        return dfCharIds;
    }

    /**
     * Returns a List of names of characteristics and dataflow variables that are set for a given characteristic type in the
     * list of all data flow variables
     * <p>
     * See {@link AbstractVertex#getNodeCharacteristicNamesWithName(String)} for a similar method for node characteristics
     * @param type Name of the characteristic type
     * @return Returns a list of all characteristic literals matching the characteristic type
     */
    public List<List<String>> getDataFlowCharacteristicNamesWithType(String type) {
        List<List<String>> dfCharIds = new ArrayList<>();
        for (DataFlowVariable df : this.getAllDataFlowVariables()) {
            List<String> charValueIds = new ArrayList<>();
            for (CharacteristicValue charValue : df.getAllCharacteristics()) {
                if (charValue.getTypeName().equals(type)) {
                    charValueIds.add(charValue.getValueName());
                }
            }
            dfCharIds.add(charValueIds);
        }

        return dfCharIds;
    }

    /**
     * Returns a string with detailed information about a node's characteristics, data flow variables and the variables'
     * characteristics
     * @return a string with the node's string representation and a list of all related characteristics types and literals
     */
    public String createPrintableNodeInformation() {
        String template = "Propagated %s%s\tNode characteristics: %s%s\tData flow Variables:  %s%s";
        String nodeCharacteristics = createPrintableCharacteristicsList(this.getAllNodeCharacteristics());
        String dataCharacteristics = this.getAllDataFlowVariables().stream()
                .map(e -> String.format("%s [%s]", e.variableName(), createPrintableCharacteristicsList(e.getAllCharacteristics())))
                .collect(Collectors.joining(", "));

        return String.format(template, this, System.lineSeparator(), nodeCharacteristics, System.lineSeparator(), dataCharacteristics,
                System.lineSeparator());
    }

    /**
     * Returns a string with the names of all characteristic types and selected literals of all characteristic values.
     * @param characteristics a list of characteristics values
     * @return a comma separated list of the format "type.literal, type.literal"
     */
    public String createPrintableCharacteristicsList(List<CharacteristicValue> characteristics) {
        List<String> entries = characteristics.stream().map(it -> String.format("%s.%s", it.getTypeName(), it.getValueName())).toList();
        return String.join(", ", entries);
    }
}
