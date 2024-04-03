package org.dataflowanalysis.analysis.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;

/**
 * This class represents an abstract vertex in a {@link AbstractTransposeFlowGraph}. An abstract vertex represents an
 * element in a transpose flow graph and links to an element. An element referenced in this way may be referenced
 * multiple times by different abstract vertices. Furthermore, the abstract vertex saved incoming and outgoing data flow
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
        return this.getPreviousElements()
                .isEmpty();
    }

    /**
     * Returns a list of node characteristic with the given characteristic type
     * <p>
     * See {@link AbstractVertex#getDataFlowVariablesWithName(String)} for a similar method for data flow variables
     * @param characteristicType Name of the characteristic type
     * @return Returns a list of all node characteristics matching the characteristic type
     */
    public List<CharacteristicValue> getNodeCharacteristicsWithName(String characteristicType) {
        return this.getAllNodeCharacteristics()
                .stream()
                .filter(cv -> cv.getTypeName()
                        .equals(characteristicType))
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of data flow variables with the given name
     * <p>
     * See {@link AbstractVertex#getNodeCharacteristicsWithName(String)} for a similar method for node characteristics
     * @param dataFlowVariable Name of the data flow variable
     * @return Returns a list of all data flow variables with the given name
     */
    public List<DataFlowVariable> getDataFlowVariablesWithName(String dataFlowVariable) {
        return this.getAllIncomingDataFlowVariables()
                .stream()
                .filter(it -> it.getVariableName()
                        .equals(dataFlowVariable))
                .collect(Collectors.toList());
    }

    /**
     * Returns a map containing the data flow characteristics with the given characteristic type for each data flow variable
     * @param characteristicType Name of the characteristic type
     * @return Returns a map with data flow characteristics with the given name for each data flow variable
     */
    public Map<String, List<CharacteristicValue>> getDataFlowCharacteristicsWithName(String characteristicType) {
        return this.getAllIncomingDataFlowVariables()
                .stream()
                .collect(Collectors.toMap(DataFlowVariable::getVariableName, it -> it.getCharacteristicsWithName(characteristicType)));
    }

    /**
     * Returns a string with detailed information about a node's characteristics, data flow variables and the variables'
     * characteristics
     * @return a string with the node's string representation and a list of all related characteristics types and literals
     */
    public String createPrintableNodeInformation() {
        String template = "Propagated %s%s\tNode characteristics: %s%s\tData flow Variables:  %s%s";
        String nodeCharacteristics = createPrintableCharacteristicsList(this.getAllNodeCharacteristics());
        String dataCharacteristics = this.getAllDataFlowVariables()
                .stream()
                .map(e -> String.format("%s [%s]", e.variableName(), createPrintableCharacteristicsList(e.getAllCharacteristics())))
                .collect(Collectors.joining(", "));

        return String.format(template, this, System.lineSeparator(), nodeCharacteristics, System.lineSeparator(), dataCharacteristics,
                System.lineSeparator());
    }

    /**
     * Returns a string with the names of all characteristic types and selected literals of all characteristic values.
     * @param characteristics a list of characteristics values
     * @return a comma separated list of the format: "CharacteristicType.CharacteristicLiteral,
     * CharacteristicType.CharacteristicLiteral"
     */
    public String createPrintableCharacteristicsList(List<CharacteristicValue> characteristics) {
        List<String> entries = characteristics.stream()
                .map(it -> String.format("%s.%s", it.getTypeName(), it.getValueName()))
                .toList();
        return String.join(", ", entries);
    }
}
