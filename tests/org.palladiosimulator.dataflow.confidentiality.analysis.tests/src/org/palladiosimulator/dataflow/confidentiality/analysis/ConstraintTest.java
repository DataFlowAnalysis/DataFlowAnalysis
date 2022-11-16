package org.palladiosimulator.dataflow.confidentiality.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.AbstractPCMActionSequenceElement;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.palladiosimulator.pcm.repository.Parameter;

public class ConstraintTest extends AnalysisFeatureTest {

    private final Logger logger = Logger.getLogger(ConstraintTest.class);

    /**
     * Tests, whether the analysis correctly identifies violations for the travel planner model
     * <p>
     * Fails if the analysis does not propagate the correct characteristics for each ActionSequence
     */
    @DisplayName("Find violations for the example model")
    @MethodSource("violationTestProvider")
    @ParameterizedTest
    public void violationTest(StandalonePCMDataFlowConfidentialtyAnalysis analysis,
            Predicate<AbstractActionSequenceElement<?>> contraint, boolean noViolations) {

        // Change this to DEBUG if you're only interested in the found violations
        logger.setLevel(Level.TRACE);

        var sequences = analysis.findAllSequences();
        var sequence = sequences.get(0);
        sequence.elements().stream()
        .map(AbstractPCMActionSequenceElement.class::cast)
        .forEach(it -> System.out.printf("Node %s has parameters %s%n", it, param(it.getParameter())));
        System.out.println("----------------------------------");
        System.out.println("----------------------------------");
        System.out.println("----------------------------------");
        System.out.println("----------------------------------");
        var propagationResult = analysis.evaluateDataFlows(sequences);

        var result = analysis.queryDataFlow(propagationResult.get(0), contraint);
        printViolation(result);
        assertEquals(noViolations, result.isEmpty());
    }
    
    private String param(List<Parameter> parameter) {
    	StringJoiner builder = new StringJoiner(",");
    	parameter.forEach(it -> builder.add(it.getParameterName()));
    	return "[" + builder.toString() + "]";
    }

    private Stream<Arguments> violationTestProvider() {
        Predicate<AbstractActionSequenceElement<?>> travelPlannerContraint = node -> travelPlannerCondition(node);
        Predicate<AbstractActionSequenceElement<?>> internationalOnlineShopContraint = node -> internationalOnlineShopCondition(
                node);
        return Stream.of(Arguments.of(travelPlannerAnalysis, travelPlannerContraint, true),
                Arguments.of(internationalOnlineShopAnalysis, internationalOnlineShopContraint, false));
    }

    /**
     * Indicates whether an element in an action sequence violates the constraint of the travel
     * planner model
     * 
     * @param node
     *            Element of the action sequence
     * @return Returns true, if the constraint is violated. Otherwise, the method returns false.
     */
    private boolean travelPlannerCondition(AbstractActionSequenceElement<?> node) {
        List<Literal> assignedRoles = node.getNodeCharacteristicsWithName("AssignedRoles");
        List<Literal> grantedRoles = node.getDataFlowCharacteristicsWithName("GrantedRoles");
        printNodeInformation(node);

        return assignedRoles.stream()
            .noneMatch(ar -> grantedRoles.contains(ar));
    }

    /**
     * Indicates whether an element in an action sequence violates the constraint of the
     * international online shop model
     * 
     * @param node
     *            Element of the action sequence
     * @return Returns true, if the constraint is violated. Otherwise, the method returns false.
     */
    private boolean internationalOnlineShopCondition(AbstractActionSequenceElement<?> node) {
        List<Literal> serverLocation = node.getNodeCharacteristicsWithName("ServerLocation");
        List<Literal> dataSensitivity = node.getDataFlowCharacteristicsWithName("DataSensitivity");
        printNodeInformation(node);

        return dataSensitivity.stream()
            .anyMatch(l -> l.getName()
                .equals("Personal")) && serverLocation.stream()
                    .anyMatch(l -> l.getName()
                        .equals("nonEU"));
    }

    /**
     * Prints a violation with detailed information about the node where it occurred with its data
     * flow variables and characteristics. The information is printed using the logger's debug
     * function.
     * 
     * @param dataFlowQueryResult
     *            the result of a data flow query call, a (potentially empty) list of sequence
     *            elements
     */
    private void printViolation(List<AbstractActionSequenceElement<?>> dataFlowQueryResult) {
        dataFlowQueryResult.forEach(it -> logger
            .debug(String.format("Constraint violation found: %s", createPrintableNodeInformation(it))));
    }

    /**
     * Prints detailed information of a node with its data flow variables and characteristics. The
     * information is printed using the logger's trace function.
     * 
     * @param node
     *            The sequence element whose information shall be printed
     */
    private void printNodeInformation(AbstractActionSequenceElement<?> node) {
        logger.trace(String.format("Analyzing: %s", createPrintableNodeInformation(node)));
    }

    /**
     * Returns a string with detailed information about a node's characteristics, data flow
     * variables and the variables' characteristics.
     * 
     * @param node
     *            a sequence element after the label propagation happened
     * @return a string with the node's string representation and a list of all related
     *         characteristics types and literals
     */
    private String createPrintableNodeInformation(AbstractActionSequenceElement<?> node) {
        String template = "%s%s\tNode characteristics: %s%s\tData flow Variables:  %s%s";
        String nodeCharacteristics = createPrintableCharacteristicsList(node.getAllNodeCharacteristics());
        String dataCharacteristics = node.getAllDataFlowVariables()
            .stream()
            .map(e -> String.format("%s [%s]", e.variableName(),
                    createPrintableCharacteristicsList(e.getAllCharacteristics())))
            .collect(Collectors.joining(", "));

        return String.format(template, node.toString(), System.lineSeparator(), nodeCharacteristics,
                System.lineSeparator(), dataCharacteristics, System.lineSeparator());
    }

    /**
     * Returns a string with the names of all characteristic types and selected literals of all
     * characteristic values.
     * 
     * @param characteristics
     *            a list of characteristics values
     * @return a comma separated list of the format "type.literal, type.literal"
     */
    private String createPrintableCharacteristicsList(List<CharacteristicValue> characteristics) {
        List<String> entries = characteristics.stream()
            .map(it -> String.format("%s.%s", it.characteristicType()
                .getName(),
                    it.characteristicLiteral()
                        .getName()))
            .toList();
        return String.join(", ", entries);
    }
}
