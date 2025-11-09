package org.dataflowanalysis.analysis.tests.integration.constraint;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.tests.integration.BaseTest;
import org.dataflowanalysis.analysis.utils.LoggerManager;

public class ConstraintTest extends BaseTest {

    private final Logger logger = LoggerManager.getLogger(ConstraintTest.class);

    /**
     * Prints a violation with detailed information about the node where it occurred with its data characteristics and
     * vertex characteristics. The information is printed using the logger's debug function.
     * @param dataFlowQueryResult the result of a data flow query call, a (potentially empty) list of sequence elements
     */
    protected void printViolation(List<? extends AbstractVertex<?>> dataFlowQueryResult) {
        dataFlowQueryResult.forEach(it -> logger.debug(String.format("Constraint violation found: %s", createPrintableNodeInformation(it))));
    }

    /**
     * Prints detailed information of a node with its data and vertex characteristics. The information is printed using the
     * logger's trace function.
     * @param node The sequence element whose information shall be printed
     */
    protected void printNodeInformation(AbstractVertex<?> node) {
        logger.trace(String.format("Analyzing: %s", createPrintableNodeInformation(node)));
    }

    /**
     * Returns a string with detailed information about a node's data and vertex characteristics.
     * @param node Vertex after the label propagation happened
     * @return Returns a String with the vertex's string representation and a list of all related characteristics types and
     * literals
     */
    protected String createPrintableNodeInformation(AbstractVertex<?> node) {
        String template = "%s%s\tNode characteristics: %s%s\tData flow Variables:  %s%s";
        String nodeCharacteristics = createPrintableCharacteristicsList(node.getAllVertexCharacteristics());
        String dataCharacteristics = node.getAllDataCharacteristics()
                .stream()
                .map(e -> String.format("%s [%s]", e.variableName(), createPrintableCharacteristicsList(e.getAllCharacteristics())))
                .collect(Collectors.joining(", "));

        return String.format(template, node, System.lineSeparator(), nodeCharacteristics, System.lineSeparator(), dataCharacteristics,
                System.lineSeparator());
    }

    /**
     * Returns a string with the names of all characteristic types and selected literals of all characteristic values.
     * @param characteristics a list of characteristics values
     * @return a comma separated list of the format "type.literal, type.literal"
     */
    protected String createPrintableCharacteristicsList(List<CharacteristicValue> characteristics) {
        List<String> entries = characteristics.stream()
                .map(it -> String.format("%s.%s", it.getTypeName(), it.getValueName()))
                .toList();
        return String.join(", ", entries);
    }
}
