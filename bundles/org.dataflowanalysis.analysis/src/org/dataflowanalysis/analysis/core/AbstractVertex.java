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

    private Optional<List<DataFlowVariable>> incomingDataCharacteristics;
    private Optional<List<DataFlowVariable>> outgoingDataCharacteristics;
    private Optional<List<CharacteristicValue>> vertexCharacteristics;

    /**
     * Constructs a new action sequence element with empty data characteristics and node characteristics
     */
    public AbstractVertex(T referencedElement) {
        this.referencedElement = referencedElement;
        this.incomingDataCharacteristics = Optional.empty();
        this.outgoingDataCharacteristics = Optional.empty();
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
     * @param incomingDataCharacteristics Incoming data characteristics that flow into the vertex
     * @param outgoingDataCharacteristics Outgoing data characteristics that flow out of the vertex
     * @param vertexCharacteristics Vertex characteristics present at the node
     */
    protected void setPropagationResult(List<DataFlowVariable> incomingDataCharacteristics, List<DataFlowVariable> outgoingDataCharacteristics,
            List<CharacteristicValue> vertexCharacteristics) {
        if (this.isEvaluated()) {
            logger.error("Cannot set propagation result of already evaluated vertex");
            throw new IllegalArgumentException();
        }
        this.incomingDataCharacteristics = Optional.of(new ArrayList<>(incomingDataCharacteristics));
        this.outgoingDataCharacteristics = Optional.of(new ArrayList<>(outgoingDataCharacteristics));
        this.vertexCharacteristics = Optional.of(new ArrayList<>(vertexCharacteristics));
    }

    /**
     * Returns whether the action sequence element has been evaluated
     * @return Returns true, if the node is evaluated. Otherwise, the method returns false
     */
    public boolean isEvaluated() {
        return this.incomingDataCharacteristics.isPresent() && this.outgoingDataCharacteristics.isPresent() && this.vertexCharacteristics.isPresent();
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
     * Returns a list of all data characteristics that are present for the action sequence element
     * @return List of present data characteristics
     */
    public List<DataFlowVariable> getAllDataCharacteristics() {
        return this.incomingDataCharacteristics.orElseThrow(IllegalStateException::new);
    }

    /**
     * Returns a list of all incoming data characteristics that are present for the action sequence element
     * @return List of present incoming data characteristics (e.g. the variables at the input pin of the DFD representation)
     */
    public List<DataFlowVariable> getAllIncomingDataCharacteristics() {
        return this.incomingDataCharacteristics.orElseThrow(IllegalStateException::new);
    }

    /**
     * Returns a list of all outgoing data characteristics that are present for the action sequence element
     * @return List of present outgoing data characteristics (e.g. the variables at the output pin of the DFD representation)
     */
    public List<DataFlowVariable> getAllOutgoingDataCharacteristics() {
        return this.outgoingDataCharacteristics.orElseThrow(IllegalStateException::new);
    }

    /**
     * Returns a list of all present node characteristics for the action sequence element
     * @return List of present node characteristics
     */
    public List<CharacteristicValue> getAllVertexCharacteristics() {
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
     * See {@link AbstractVertex#getDataFlowVariables(String)} for a similar method for data flow
     * variables
     * @param characteristicType Name of the characteristic type
     * @return Returns a list of all node characteristics matching the characteristic type
     */
    public List<CharacteristicValue> getNodeCharacteristics(String characteristicType) {
        return this.getAllVertexCharacteristics().stream()
                .filter(cv -> cv.getTypeName().equals(characteristicType))
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of node characteristic value names with the given characteristic type
     * <p>
     * See {@link AbstractVertex#getDataFlowVariableNames(String)} for a similar method for data flow
     * variables
     * @param characteristicType Name of the characteristic type
     * @return Returns a list of all node characteristics matching the characteristic type
     */
    public List<String> getNodeCharacteristicNames(String characteristicType) {
        return this.getAllVertexCharacteristics().stream()
                .filter(cv -> cv.getTypeName().equals(characteristicType))
                .map(CharacteristicValue::getValueName)
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of data flow variables with the given name
     * <p>
     * See {@link AbstractVertex#getNodeCharacteristics(String)} for a similar method for node characteristics
     * @param dataFlowVariable Name of the data flow variable
     * @return Returns a list of all data flow variables with the given name
     */
    public List<DataFlowVariable> getDataFlowVariables(String dataFlowVariable) {
        return this.getAllIncomingDataCharacteristics().stream()
                .filter(it -> it.variableName().equals(dataFlowVariable))
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of data flow variable names with the given name
     * <p>
     * See {@link AbstractVertex#getNodeCharacteristicNames(String)} for a similar method for node characteristics
     * @param dataFlowVariable Name of the data flow variable
     * @return Returns a list of all data flow variables with the given name
     */
    public List<String> getDataFlowVariableNames(String dataFlowVariable) {
        return this.getAllIncomingDataCharacteristics().stream()
                .map(DataFlowVariable::variableName)
                .filter(s -> s.equals(dataFlowVariable))
                .collect(Collectors.toList());
    }

    /**
     * Returns a map containing the data flow characteristics with the given characteristic type for each data flow variable
     * <p>
     * To get the characteristic value names for the data flow variables use {@link AbstractVertex#getDataFlowCharacteristicNames(String)}
     * @param characteristicType Name of the characteristic type
     * @return Returns a map with data flow characteristics with the given name for each data flow variable
     */
    public Map<String, List<CharacteristicValue>> getDataFlowCharacteristics(String characteristicType) {
        return this.getAllIncomingDataCharacteristics().stream()
                .collect(Collectors.toMap(DataFlowVariable::variableName, it -> it.getCharacteristicsWithName(characteristicType)));
    }

    /**
     * Returns a map containing the data flow characteristics with the given characteristic type for each data flow variable
     * <p>
     * To get the characteristic values for the data flow variables use {@link AbstractVertex#getDataFlowCharacteristics(String)}
     * @param characteristicType Name of the characteristic type
     * @return Returns a map with data flow characteristics with the given name for each data flow variable
     */
    public Map<String, List<String>> getDataFlowCharacteristicNames(String characteristicType) {
        return this.getAllIncomingDataCharacteristics().stream()
                .collect(Collectors.toMap(DataFlowVariable::variableName,
                        it -> it.getCharacteristicsWithName(characteristicType).stream()
                                .map(CharacteristicValue::getValueName)
                                .toList()));
    }

    /**
     * Returns a string with detailed information about a node's characteristics, data flow variables and the variables'
     * characteristics
     * @return a string with the node's string representation and a list of all related characteristics types and literals
     */
    public String createPrintableNodeInformation() {
        String template = "Propagated %s%s\tNode characteristics: %s%s\tData flow Variables:  %s%s";
        String nodeCharacteristics = createPrintableCharacteristicsList(this.getAllVertexCharacteristics());
        String dataCharacteristics = this.getAllDataCharacteristics().stream()
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
