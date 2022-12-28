package org.palladiosimulator.dataflow.confidentiality.analysis.constraint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.palladiosimulator.dataflow.confidentiality.analysis.BaseTest;
import org.palladiosimulator.dataflow.confidentiality.analysis.ListAppender;
import org.palladiosimulator.dataflow.confidentiality.analysis.StandalonePCMDataFlowConfidentialtyAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.ActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.DatabaseActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.PCMActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.UserActionSequenceElement;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;

public class ConstraintTest extends BaseTest {

    private final Logger logger = Logger.getLogger(ConstraintTest.class);

    
    /**
     * Indicates whether an element in an action sequence violates the constraint of the travel
     * planner model
     * 
     * @param node
     *            Element of the action sequence
     * @return Returns true, if the constraint is violated. Otherwise, the method returns false.
     */
    private boolean travelPlannerCondition(AbstractActionSequenceElement<?> node) {
    	List<String> assignedRoles = node.getNodeCharacteristicsWithName("AssignedRoles").stream()
    			.map(it -> it.getName())
    			.collect(Collectors.toList());
    	Map<DataFlowVariable, List<Literal>> grantedRoles = node.getDataFlowCharacteristicsWithName("GrantedRoles");
    	
        printNodeInformation(node);
        
        return grantedRoles.entrySet().stream().map(dfd -> {
        	return !dfd.getValue().isEmpty() && dfd.getValue().stream()
        			.distinct()
        			.filter(it -> assignedRoles.contains(it.getName()))
        			.collect(Collectors.toList())
        			.isEmpty();
        }).anyMatch(Boolean::valueOf);
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
        List<Literal> dataSensitivity = node.getDataFlowCharacteristicsWithName("DataSensitivity").values().stream()
        		.flatMap(it -> it.stream()).collect(Collectors.toList());
        printNodeInformation(node);

        return dataSensitivity.stream()
            .anyMatch(l -> l.getName()
                .equals("Personal")) && serverLocation.stream()
                    .anyMatch(l -> l.getName()
                        .equals("nonEU"));
    }
    
    
    private boolean dataStoreCondition(AbstractActionSequenceElement<?> node) {
    	List<Literal> assignedRoles = node.getNodeCharacteristicsWithName("AssignedRole");
    	Map<DataFlowVariable, List<Literal>> grantedRoles = node.getDataFlowCharacteristicsWithName("GrantedRole");
    	
        printNodeInformation(node);
        
        if (assignedRoles.isEmpty()) {
        	return false;
        }
        
        return !grantedRoles.entrySet().stream()
        		.allMatch(df -> df.getValue().stream().allMatch(it -> assignedRoles.contains(it)));
    }
    
    private boolean returnCondition(AbstractActionSequenceElement<?> node) {
    	List<Literal> assignedNode = node.getNodeCharacteristicsWithName("AssignedRole");
    	List<Literal> assignedVariables = node.getDataFlowCharacteristicsWithName("AssignedRole").values().stream()
    			.flatMap(it -> it.stream())
    			.collect(Collectors.toList());
    	
        printNodeInformation(node);
        if (assignedNode.isEmpty() || assignedVariables.isEmpty()) {
        	return false;
        }
        assignedNode.removeAll(assignedVariables);
        return !assignedNode.isEmpty();
    }
    
    @Test
    @DisplayName("Test whether cycles in datastores are detected")
    public void testCycleDataStores() {
    	var usageModelPath = Paths.get("models", "CycleDatastoreTest", "default.usagemodel");
    	var allocationPath = Paths.get("models", "CycleDatastoreTest", "default.allocation");
    	StandalonePCMDataFlowConfidentialtyAnalysis analysis = super.initializeAnalysis(usageModelPath, allocationPath);
    	
    	Logger logger = Logger.getLogger(PCMActionSequence.class);
    	logger.setLevel(Level.DEBUG);
    	ListAppender appender = new ListAppender();
    	logger.addAppender(appender);
    	
    	List<ActionSequence> sequences = analysis.findAllSequences();
    	assertThrows(IllegalStateException.class, () -> analysis.evaluateDataFlows(sequences));
    	assertTrue(appender.loggedLevel(Level.ERROR));
    }
    
    @Test
    @DisplayName("Test whether read only datastores are detected")
    public void testReadOnlyDatastore() {
    	var usageModelPath = Paths.get("models", "ReadOnlyDatastore", "default.usagemodel");
    	var allocationPath = Paths.get("models", "ReadOnlyDatastore", "default.allocation");
    	StandalonePCMDataFlowConfidentialtyAnalysis analysis = super.initializeAnalysis(usageModelPath, allocationPath);
    	
    	Logger logger = Logger.getLogger(DatabaseActionSequenceElement.class);
    	logger.setLevel(Level.DEBUG);
    	ListAppender appender = new ListAppender();
    	logger.addAppender(appender);
    	
    	List<ActionSequence> sequences = analysis.findAllSequences();
    	analysis.evaluateDataFlows(sequences);
    	
    	assertTrue(appender.loggedLevel(Level.WARN));
    }
    
    /**
     * Test determining whether node characteristics work correctly
     */
    @Test
    @DisplayName("Test whether node characteristics works correctly")
    public void testNodeCharacteristics() {
    	var usageModelPath = Paths.get("models", "NodeCharacteristicsTest", "default.usagemodel");
    	var allocationPath = Paths.get("models", "NodeCharacteristicsTest", "default.allocation");
    	StandalonePCMDataFlowConfidentialtyAnalysis analysis = super.initializeAnalysis(usageModelPath, allocationPath);
    	
    	List<ActionSequence> sequences = analysis.findAllSequences();
    	List<ActionSequence> propagatedSequences = analysis.evaluateDataFlows(sequences);
    	
    	logger.setLevel(Level.TRACE);
    	var results = analysis.queryDataFlow(propagatedSequences.get(0), node -> {
    		printNodeInformation(node);
    		if (node instanceof UserActionSequenceElement<?>) {
    			return node.getAllNodeCharacteristics().size() != 1;
    		} else {
            	return node.getAllNodeCharacteristics().size() != 2;
    		}
        });
        printViolation(results);
        assertTrue(results.isEmpty());
    }
    
    /**
     * Test determining whether node characteristics work correctly
     */
    @Test
    @DisplayName("Test whether node characteristics with composite components works correctly")
    public void testCompositeCharacteristics() {
    	var usageModelPath = Paths.get("models", "CompositeCharacteristicsTest", "default.usagemodel");
    	var allocationPath = Paths.get("models", "CompositeCharacteristicsTest", "default.allocation");
    	StandalonePCMDataFlowConfidentialtyAnalysis analysis = super.initializeAnalysis(usageModelPath, allocationPath);
    	
    	List<ActionSequence> sequences = analysis.findAllSequences();
    	List<ActionSequence> propagatedSequences = analysis.evaluateDataFlows(sequences);
    	
    	logger.setLevel(Level.TRACE);
    	var results = analysis.queryDataFlow(propagatedSequences.get(0), node -> {
    		printNodeInformation(node);
    		if (node instanceof UserActionSequenceElement<?>) {
    			return node.getAllNodeCharacteristics().size() != 1;
    		} else {
            	return node.getAllNodeCharacteristics().size() != 3;
    		}
        });
        printViolation(results);
        assertTrue(results.isEmpty());
    }
    
    private Stream<Arguments> provideTestConstraintResults() {
    	Predicate<AbstractActionSequenceElement<?>> travelPlannerConstraint = node -> travelPlannerCondition(node);
    	Predicate<AbstractActionSequenceElement<?>> internationalOnlineShopConstraint = node -> internationalOnlineShopCondition(node);

    	StandalonePCMDataFlowConfidentialtyAnalysis mutipleAssembliesAnalysis = 
    			super.initializeAnalysis(Paths.get("models", "MultipleAssembliesTest", "default.usagemodel"), Paths.get("models", "MultipleAssembliesTest", "default.allocation"));
    	
    	StandalonePCMDataFlowConfidentialtyAnalysis dataStoreAnalysis = 
    			super.initializeAnalysis(Paths.get("models", "DatastoreTest", "default.usagemodel"), Paths.get("models", "DatastoreTest", "default.allocation"));
    	Predicate<AbstractActionSequenceElement<?>> dataStoreConstraint = node -> dataStoreCondition(node);
    	
    	StandalonePCMDataFlowConfidentialtyAnalysis returnAnalysis = 
    			super.initializeAnalysis(Paths.get("models", "ReturnTestModel", "default.usagemodel"), Paths.get("models", "ReturnTestModel", "default.allocation"));
    	Predicate<AbstractActionSequenceElement<?>> returnConstraint = node -> returnCondition(node);
    	
    	return Stream.of(
    			Arguments.of(travelPlannerAnalysis, travelPlannerConstraint, 
    					List.of(
    						new ConstraintData("_vorK8fVeEeuMKba1Qn68bg", 
    							List.of(new CharacteristicValueData("AssignedRoles", "Airline")), 
    							Map.of(
    								"flight", List.of(new CharacteristicValueData("GrantedRoles", "User"), new CharacteristicValueData("GrantedRoles", "Airline")),
    								"ccd", List.of(new CharacteristicValueData("GrantedRoles", "User"))
    							)),
    						
    						new ConstraintData("_7HCu4PViEeuMKba1Qn68bg",
    								List.of(new CharacteristicValueData("AssignedRoles", "Airline")), 
    								Map.of(
    									"flight", List.of(new CharacteristicValueData("GrantedRoles", "User"), new CharacteristicValueData("GrantedRoles", "Airline")),
    									"ccd", List.of(new CharacteristicValueData("GrantedRoles", "User")),
    									"RETRUN", List.of(new CharacteristicValueData("GrantedRoles", "User"), new CharacteristicValueData("GrantedRoles", "Airline"))
    								))
    					)),
    			Arguments.of(internationalOnlineShopAnalysis, internationalOnlineShopConstraint,
    					List.of(
    						new ConstraintData("_oGmXgYTjEeywmO_IpTxeAg", 
    							List.of(new CharacteristicValueData("ServerLocation", "nonEU")), 
    							Map.of(
    								"userData", List.of(new CharacteristicValueData("DataSensitivity", "Personal"))
    							))
    					)),
    			Arguments.of(mutipleAssembliesAnalysis, internationalOnlineShopConstraint,
    					List.of(
    						new ConstraintData("_dQ568HQSEe2fd909RlIZZw", 
    							List.of(new CharacteristicValueData("ServerLocation", "nonEU"), new CharacteristicValueData("ServerLocation", "EU")),
    							Map.of(
    								"userdata", List.of(new CharacteristicValueData("DataSensitivity", "Personal"))
    							))
    					)),
    			Arguments.of(dataStoreAnalysis, dataStoreConstraint,
    					List.of(
    						new ConstraintData("_elixoHQdEe2W39w_cTGxjg", 
    							List.of(new CharacteristicValueData("AssignedRole", "User")), 
    							Map.of(
    								"RETURN", List.of(new CharacteristicValueData("GrantedRole", "Admin")),
    								"ccd", List.of(new CharacteristicValueData("GrantedRole", "Admin"))
    							))
    					)),
    			/**
    			 * Constraint violation found: CallingUserActionSequenceElement / returning (EntryLevelSystemCall1, _nOhAgILtEe2YyoqaKVkqog))
	Node characteristics: AssignedRole.User
	Data flow Variables:  RETURN [AssignedRole.Admin], data [AssignedRole.Admin]
    			 */
    			Arguments.of(returnAnalysis, returnConstraint,
    					List.of(
    						new ConstraintData("_nOhAgILtEe2YyoqaKVkqog",
    							List.of(new CharacteristicValueData("AssignedRole", "User")),
    							Map.of(
    								"RETURN", List.of(new CharacteristicValueData("AssignedRole", "Admin")),
    								"data", List.of(new CharacteristicValueData("AssignedRole", "Admin"))
    							))
    					))
 
    	);
    }
    
    /**
     * Tests, whether the analysis correctly identifies violations for the example models
     * <p>
     * Fails if the analysis does not propagate the correct characteristics for each ActionSequence
     */

    @ParameterizedTest
    @MethodSource("provideTestConstraintResults")
    @DisplayName("Test results of the analysis with a constraint")
    public void testContraintResults(StandalonePCMDataFlowConfidentialtyAnalysis analysis, Predicate<AbstractActionSequenceElement<?>> constraint, List<ConstraintData> constraintData) {
    	analysis.setLoggerLevel(Level.TRACE);
    	List<ActionSequence> actionSequences = analysis.findAllSequences();
    	List<ActionSequence> evaluatedSequences = analysis.evaluateDataFlows(actionSequences);
    	
    	List<AbstractActionSequenceElement<?>> results = evaluatedSequences.stream()
    			.map(it -> analysis.queryDataFlow(it, constraint))
    			.flatMap(it -> it.stream())
    			.collect(Collectors.toList());
    	
    	assertEquals(constraintData.size(), results.size(), "Incorrect amount of violations found");
    	
    	for(ConstraintData constraintNodeData : constraintData) {
    		var violatingNode = results.stream()
    				.filter(it -> constraintNodeData.matches(it))
    				.findFirst();
    		
    		if (violatingNode.isEmpty()) {
    			fail("Could not find node for expected constraint violation");
    		}
    		
    		List<CharacteristicValue> nodeCharacteristics = violatingNode.get().getAllNodeCharacteristics();
    		List<DataFlowVariable> dataFlowVariables = violatingNode.get().getAllDataFlowVariables();
    		
    		assertEquals(constraintNodeData.nodeCharacteristicsAmount(), nodeCharacteristics.size());
    		assertEquals(constraintNodeData.dataFlowVariablesAmount(), dataFlowVariables.size());
    		
    		for(CharacteristicValue characteristicValue : nodeCharacteristics) {
    			assertTrue(constraintNodeData.hasNodeCharacteristic(characteristicValue));
    		}
    		
    		for(DataFlowVariable dataFlowVariable : dataFlowVariables) {
    			assertTrue(constraintNodeData.hasDataFlowVariable(dataFlowVariable));
    		}
    	}
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


