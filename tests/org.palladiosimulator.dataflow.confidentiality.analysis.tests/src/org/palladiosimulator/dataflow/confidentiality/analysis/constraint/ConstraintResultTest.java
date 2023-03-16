package org.palladiosimulator.dataflow.confidentiality.analysis.constraint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.dataflow.confidentiality.analysis.StandalonePCMDataFlowConfidentialtyAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.constraint.data.ConstraintData;
import org.palladiosimulator.dataflow.confidentiality.analysis.constraint.data.ConstraintViolations;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.ActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;

public class ConstraintResultTest extends ConstraintTest {
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
    
    /**
     * Indicates whether an element in an action sequence violates the constraint of the
     * datastore test model
     * 
     * @param node
     *            Element of the action sequence
     * @return Returns true, if the constraint is violated. Otherwise, the method returns false.
     */
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
    
    /**
     * Indicates whether an element in an action sequence violates the constraint of the
     * return test model
     * 
     * @param node
     *            Element of the action sequence
     * @return Returns true, if the constraint is violated. Otherwise, the method returns false.
     */
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
    
    /**
     * Tests, whether the analysis correctly identifies violations for the example models
     * <p>
     * Fails if the analysis does not propagate the correct characteristics for each ActionSequence
     */
    @Test
    public void travelPlannerTestConstraintResults() {
    	travelPlannerAnalysis.setLoggerLevel(Level.TRACE);
    	List<ActionSequence> actionSequences = travelPlannerAnalysis.findAllSequences();
    	List<ActionSequence> evaluatedSequences = travelPlannerAnalysis.evaluateDataFlows(actionSequences);
    	Predicate<AbstractActionSequenceElement<?>> constraint = node -> travelPlannerCondition(node);
    	List<ConstraintData> constraintData = ConstraintViolations.travelPlannerViolations;
    	
    	List<AbstractActionSequenceElement<?>> results = evaluatedSequences.stream()
    			.map(it -> travelPlannerAnalysis.queryDataFlow(it, constraint))
    			.flatMap(it -> it.stream())
    			.collect(Collectors.toList());
    	
    	assertEquals(constraintData.size(), results.size(), "Incorrect count of violations found");
    	
    	for(ConstraintData constraintNodeData : constraintData) {
    		var violatingNode = results.stream()
    				.filter(it -> constraintNodeData.matches(it))
    				.findFirst();
    		
    		if (violatingNode.isEmpty()) {
    			fail("Could not find node for expected constraint violation");
    		}
    		
    		List<CharacteristicValue> nodeCharacteristics = violatingNode.get().getAllNodeCharacteristics();
    		List<DataFlowVariable> dataFlowVariables = violatingNode.get().getAllDataFlowVariables();
    		
    		assertEquals(constraintNodeData.nodeCharacteristicsCount(), nodeCharacteristics.size());
    		assertEquals(constraintNodeData.dataFlowVariablesCount(), dataFlowVariables.size());
    		
    		for(CharacteristicValue characteristicValue : nodeCharacteristics) {
    			assertTrue(constraintNodeData.hasNodeCharacteristic(characteristicValue));
    		}
    		
    		for(DataFlowVariable dataFlowVariable : dataFlowVariables) {
    			assertTrue(constraintNodeData.hasDataFlowVariable(dataFlowVariable));
    		}
    	}
    }
    
    /**
     * Tests, whether the analysis correctly identifies violations for the example models
     * <p>
     * Fails if the analysis does not propagate the correct characteristics for each ActionSequence
     */
    @Test
    public void internationalOnlineShopTestConstraintResults() {
    	internationalOnlineShopAnalysis.setLoggerLevel(Level.TRACE);
    	List<ActionSequence> actionSequences = internationalOnlineShopAnalysis.findAllSequences();
    	List<ActionSequence> evaluatedSequences = internationalOnlineShopAnalysis.evaluateDataFlows(actionSequences);
    	Predicate<AbstractActionSequenceElement<?>> constraint = node -> internationalOnlineShopCondition(node);
    	List<ConstraintData> constraintData = ConstraintViolations.internationalOnlineShopViolations;
    	
    	List<AbstractActionSequenceElement<?>> results = evaluatedSequences.stream()
    			.map(it -> internationalOnlineShopAnalysis.queryDataFlow(it, constraint))
    			.flatMap(it -> it.stream())
    			.collect(Collectors.toList());
    	
    	assertEquals(constraintData.size(), results.size(), "Incorrect count of violations found");
    	
    	for(ConstraintData constraintNodeData : constraintData) {
    		var violatingNode = results.stream()
    				.filter(it -> constraintNodeData.matches(it))
    				.findFirst();
    		
    		if (violatingNode.isEmpty()) {
    			fail("Could not find node for expected constraint violation");
    		}
    		
    		List<CharacteristicValue> nodeCharacteristics = violatingNode.get().getAllNodeCharacteristics();
    		List<DataFlowVariable> dataFlowVariables = violatingNode.get().getAllDataFlowVariables();
    		
    		assertEquals(constraintNodeData.nodeCharacteristicsCount(), nodeCharacteristics.size());
    		assertEquals(constraintNodeData.dataFlowVariablesCount(), dataFlowVariables.size());
    		
    		for(CharacteristicValue characteristicValue : nodeCharacteristics) {
    			assertTrue(constraintNodeData.hasNodeCharacteristic(characteristicValue));
    		}
    		
    		for(DataFlowVariable dataFlowVariable : dataFlowVariables) {
    			assertTrue(constraintNodeData.hasDataFlowVariable(dataFlowVariable));
    		}
    	}
    }
    
    /**
     * Tests, whether the analysis correctly identifies violations for the example models
     * <p>
     * Fails if the analysis does not propagate the correct characteristics for each ActionSequence
     */
    @Test
    public void oneAssemblyMultipleResourceTestConstraintResults() {
    	StandalonePCMDataFlowConfidentialtyAnalysis analysis = 
    			super.initializeAnalysis(Paths.get("models", "OneAssembyMultipleResourceContainerTest", "default.usagemodel"), Paths.get("models", "OneAssembyMultipleResourceContainerTest", "default.allocation"));
    	analysis.setLoggerLevel(Level.TRACE);
    	List<ActionSequence> actionSequences = analysis.findAllSequences();
    	List<ActionSequence> evaluatedSequences = analysis.evaluateDataFlows(actionSequences);
    	Predicate<AbstractActionSequenceElement<?>> constraint = node -> internationalOnlineShopCondition(node);
    	List<ConstraintData> constraintData = ConstraintViolations.multipleRessourcesViolations;
    	
    	List<AbstractActionSequenceElement<?>> results = evaluatedSequences.stream()
    			.map(it -> analysis.queryDataFlow(it, constraint))
    			.flatMap(it -> it.stream())
    			.collect(Collectors.toList());
    	
    	assertEquals(constraintData.size(), results.size(), "Incorrect count of violations found");
    	
    	for(ConstraintData constraintNodeData : constraintData) {
    		var violatingNode = results.stream()
    				.filter(it -> constraintNodeData.matches(it))
    				.findFirst();
    		
    		if (violatingNode.isEmpty()) {
    			fail("Could not find node for expected constraint violation");
    		}
    		
    		List<CharacteristicValue> nodeCharacteristics = violatingNode.get().getAllNodeCharacteristics();
    		List<DataFlowVariable> dataFlowVariables = violatingNode.get().getAllDataFlowVariables();
    		
    		assertEquals(constraintNodeData.nodeCharacteristicsCount(), nodeCharacteristics.size());
    		assertEquals(constraintNodeData.dataFlowVariablesCount(), dataFlowVariables.size());
    		
    		for(CharacteristicValue characteristicValue : nodeCharacteristics) {
    			assertTrue(constraintNodeData.hasNodeCharacteristic(characteristicValue));
    		}
    		
    		for(DataFlowVariable dataFlowVariable : dataFlowVariables) {
    			assertTrue(constraintNodeData.hasDataFlowVariable(dataFlowVariable));
    		}
    	}
    }
    
    /**
     * Tests, whether the analysis correctly identifies violations for the example models
     * <p>
     * Fails if the analysis does not propagate the correct characteristics for each ActionSequence
     */
    @Test
    public void dataStoreTestConstraintResults() {
    	StandalonePCMDataFlowConfidentialtyAnalysis dataStoreAnalysis = 
    			super.initializeAnalysis(Paths.get("models", "DatastoreTest", "default.usagemodel"), Paths.get("models", "DatastoreTest", "default.allocation"));
    	Predicate<AbstractActionSequenceElement<?>> constraint = node -> dataStoreCondition(node);
    	dataStoreAnalysis.setLoggerLevel(Level.TRACE);
    	List<ActionSequence> actionSequences = dataStoreAnalysis.findAllSequences();
    	List<ActionSequence> evaluatedSequences = dataStoreAnalysis.evaluateDataFlows(actionSequences);
    	List<ConstraintData> constraintData = ConstraintViolations.dataStoreViolations;
    	
    	List<AbstractActionSequenceElement<?>> results = evaluatedSequences.stream()
    			.map(it -> dataStoreAnalysis.queryDataFlow(it, constraint))
    			.flatMap(it -> it.stream())
    			.collect(Collectors.toList());
    	
    	assertEquals(constraintData.size(), results.size(), "Incorrect count of violations found");
    	
    	for(ConstraintData constraintNodeData : constraintData) {
    		var violatingNode = results.stream()
    				.filter(it -> constraintNodeData.matches(it))
    				.findFirst();
    		
    		if (violatingNode.isEmpty()) {
    			fail("Could not find node for expected constraint violation");
    		}
    		
    		List<CharacteristicValue> nodeCharacteristics = violatingNode.get().getAllNodeCharacteristics();
    		List<DataFlowVariable> dataFlowVariables = violatingNode.get().getAllDataFlowVariables();
    		
    		assertEquals(constraintNodeData.nodeCharacteristicsCount(), nodeCharacteristics.size());
    		assertEquals(constraintNodeData.dataFlowVariablesCount(), dataFlowVariables.size());
    		
    		for(CharacteristicValue characteristicValue : nodeCharacteristics) {
    			assertTrue(constraintNodeData.hasNodeCharacteristic(characteristicValue));
    		}
    		
    		for(DataFlowVariable dataFlowVariable : dataFlowVariables) {
    			assertTrue(constraintNodeData.hasDataFlowVariable(dataFlowVariable));
    		}
    	}
    }
    
    /**
     * Tests, whether the analysis correctly identifies violations for the example models
     * <p>
     * Fails if the analysis does not propagate the correct characteristics for each ActionSequence
     */
    @Test
    public void returnTestConstraintResults() {
    	StandalonePCMDataFlowConfidentialtyAnalysis returnAnalysis = 
    			super.initializeAnalysis(Paths.get("models", "ReturnTestModel", "default.usagemodel"), Paths.get("models", "ReturnTestModel", "default.allocation"));
    	Predicate<AbstractActionSequenceElement<?>> constraint = node -> returnCondition(node);
    	returnAnalysis.setLoggerLevel(Level.TRACE);
    	List<ActionSequence> actionSequences = returnAnalysis.findAllSequences();
    	List<ActionSequence> evaluatedSequences = returnAnalysis.evaluateDataFlows(actionSequences);
    	List<ConstraintData> constraintData = ConstraintViolations.returnViolations;
    	
    	List<AbstractActionSequenceElement<?>> results = evaluatedSequences.stream()
    			.map(it -> returnAnalysis.queryDataFlow(it, constraint))
    			.flatMap(it -> it.stream())
    			.collect(Collectors.toList());
    	
    	assertEquals(constraintData.size(), results.size(), "Incorrect count of violations found");
    	
    	for(ConstraintData constraintNodeData : constraintData) {
    		var violatingNode = results.stream()
    				.filter(it -> constraintNodeData.matches(it))
    				.findFirst();
    		
    		if (violatingNode.isEmpty()) {
    			fail("Could not find node for expected constraint violation");
    		}
    		
    		List<CharacteristicValue> nodeCharacteristics = violatingNode.get().getAllNodeCharacteristics();
    		List<DataFlowVariable> dataFlowVariables = violatingNode.get().getAllDataFlowVariables();
    		
    		assertEquals(constraintNodeData.nodeCharacteristicsCount(), nodeCharacteristics.size());
    		assertEquals(constraintNodeData.dataFlowVariablesCount(), dataFlowVariables.size());
    		
    		for(CharacteristicValue characteristicValue : nodeCharacteristics) {
    			assertTrue(constraintNodeData.hasNodeCharacteristic(characteristicValue));
    		}
    		
    		for(DataFlowVariable dataFlowVariable : dataFlowVariables) {
    			assertTrue(constraintNodeData.hasDataFlowVariable(dataFlowVariable));
    		}
    	}
    }
}