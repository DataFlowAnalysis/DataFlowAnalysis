package org.dataflowanalysis.analysis.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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

    private Optional<List<DataCharacteristic>> incomingDataCharacteristics;
    private Optional<List<DataCharacteristic>> outgoingDataCharacteristics;
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
     * outgoing data characteristics as well as vertex characteristics.
     */
    public abstract void evaluateDataFlow();

    public abstract UUID getUniqueUUID();

    @Override
    public abstract String toString();

    /**
     * Sets the propagation result of the Vertex to the given result. This method should only be called once on elements
     * that are not evaluated.
     * @param incomingDataCharacteristics Incoming data characteristics that flow into the vertex
     * @param outgoingDataCharacteristics Outgoing data characteristics that flow out of the vertex
     * @param vertexCharacteristics Vertex characteristics present at the node
     */
    protected void setPropagationResult(List<DataCharacteristic> incomingDataCharacteristics, List<DataCharacteristic> outgoingDataCharacteristics,
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
    public List<DataCharacteristic> getAllDataCharacteristics() {
        return this.incomingDataCharacteristics.orElseThrow(IllegalStateException::new);
    }

    /**
     * Returns a list of all incoming data characteristics that are present for the action sequence element
     * @return List of present incoming data characteristics (e.g. the variables at the input pin of the DFD representation)
     */
    public List<DataCharacteristic> getAllIncomingDataCharacteristics() {
        return this.incomingDataCharacteristics.orElseThrow(IllegalStateException::new);
    }

    /**
     * Returns a list of all outgoing data characteristics that are present for the action sequence element
     * @return List of present outgoing data characteristics (e.g. the variables at the output pin of the DFD
     * representation)
     */
    public List<DataCharacteristic> getAllOutgoingDataCharacteristics() {
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
     * Returns a list of vertex characteristics with the given characteristic type
     * <p>
     * See {@link AbstractVertex#getDataCharacteristics(String)} for a similar method for data flow variables
     * @param requiredCharacteristicTypeName Name of the characteristic type
     * @return Returns a list of all vertex characteristics matching the characteristic type
     */
    public List<CharacteristicValue> getVertexCharacteristics(String requiredCharacteristicTypeName) {
        return this.getAllVertexCharacteristics()
                .stream()
                .filter(cv -> cv.getTypeName()
                        .equals(requiredCharacteristicTypeName))
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of vertex characteristic value names with the given characteristic type
     * <p>
     * See {@link AbstractVertex#getDataCharacteristicNames(String)} for a similar method for data flow variables
     * @param requiredCharacteristicTypeName Name of the characteristic type
     * @return Returns a list of all vertex characteristics matching the characteristic type
     */
    public List<String> getVertexCharacteristicNames(String requiredCharacteristicTypeName) {
        return this.getAllVertexCharacteristics()
                .stream()
                .filter(cv -> cv.getTypeName()
                        .equals(requiredCharacteristicTypeName))
                .map(CharacteristicValue::getValueName)
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of data characteristics with the given name
     * <p>
     * See {@link AbstractVertex#getVertexCharacteristics(String)} for a similar method for vertex characteristics
     * @param requiredDataCharacteristicName Name of the data characteristic
     * @return Returns a list of all data characteristics with the given name
     */
    public List<DataCharacteristic> getDataCharacteristics(String requiredDataCharacteristicName) {
        return this.getAllIncomingDataCharacteristics()
                .stream()
                .filter(it -> it.getVariableName()
                        .equals(requiredDataCharacteristicName))
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of data characteristic names with the given required data characteristic name
     * <p>
     * See {@link AbstractVertex#getVertexCharacteristicNames(String)} for a similar method for vertex characteristics
     * @param requiredDataCharacteristicName Name of the data characteristic
     * @return Returns a list of all data characteristics with the given name
     */
    public List<String> getDataCharacteristicNames(String requiredDataCharacteristicName) {
        return this.getAllIncomingDataCharacteristics()
                .stream()
                .filter(it -> it.getVariableName()
                        .equals(requiredDataCharacteristicName))
                .map(DataCharacteristic::variableName)
                .collect(Collectors.toList());
    }

    /**
     * Returns a map containing the characteristic values with the given characteristic type for each data characteristic
     * <p>
     * To get the characteristic value names for the data characteristics use
     * {@link AbstractVertex#getDataCharacteristicNamesMap(String)}
     * @param requiredCharacteristicTypeName Required name of the characteristic type
     * @return Returns a map with characteristic values with the given name for each data characteristic
     */
    public Map<String, List<CharacteristicValue>> getDataCharacteristicMap(String requiredCharacteristicTypeName) {
        return this.getAllIncomingDataCharacteristics()
                .stream()
                .collect(Collectors.toMap(DataCharacteristic::getVariableName, it -> it.getCharacteristicsWithName(requiredCharacteristicTypeName)));
    }

    /**
     * Returns a map containing the characteristic value names with the given characteristic type for each data
     * characteristic
     * <p>
     * To get the characteristic values for the data characteristics use
     * {@link AbstractVertex#getDataCharacteristicMap(String)}
     * @param requiredCharacteristicTypeName Required name of the characteristic type
     * @return Returns a map with characteristic value names with the given name for each data characteristic
     */
    public Map<String, List<String>> getDataCharacteristicNamesMap(String requiredCharacteristicTypeName) {
        return this.getAllIncomingDataCharacteristics()
                .stream()
                .collect(Collectors.toMap(DataCharacteristic::getVariableName, it -> it.getCharacteristicsWithName(requiredCharacteristicTypeName)
                        .stream()
                        .map(CharacteristicValue::getValueName)
                        .toList()));
    }

    /**
     * Returns a string with detailed information about a vertex's data and vertex characteristics
     * @return Returns a String with the node's string representation and a list of all related characteristics types and
     * literals
     */
    public String createPrintableNodeInformation() {
        String template = "Propagated %s%s\tNode characteristics: %s%s\tData flow Variables:  %s%s";
        String nodeCharacteristics = createPrintableCharacteristicsList(this.getAllVertexCharacteristics());
        String dataCharacteristics = this.getAllDataCharacteristics()
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
