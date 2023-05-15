package org.palladiosimulator.dataflow.confidentiality.analysis.constraint;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.BaseTest;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.AbstractActionSequenceElement;

public class ConstraintTest extends BaseTest {

    private final Logger logger = Logger.getLogger(ConstraintTest.class);
    
    /**
    * Prints a violation with detailed information about the node where it occurred with its data
    * flow variables and characteristics. The information is printed using the logger's debug
    * function.
    * 
    * @param dataFlowQueryResult
    *            the result of a data flow query call, a (potentially empty) list of sequence
    *            elements
    */
   protected void printViolation(List<AbstractActionSequenceElement<?>> dataFlowQueryResult) {
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
   protected void printNodeInformation(AbstractActionSequenceElement<?> node) {
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
   protected String createPrintableNodeInformation(AbstractActionSequenceElement<?> node) {
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
   protected String createPrintableCharacteristicsList(List<CharacteristicValue> characteristics) {
       List<String> entries = characteristics.stream()
           .map(it -> String.format("%s.%s", it.characteristicType()
               .getName(),
                   it.characteristicLiteral()
                       .getName()))
           .toList();
       return String.join(", ", entries);
   }
}


