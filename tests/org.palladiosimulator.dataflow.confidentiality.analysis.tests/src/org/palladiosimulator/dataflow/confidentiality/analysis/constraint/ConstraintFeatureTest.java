package org.palladiosimulator.dataflow.confidentiality.analysis.constraint;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.dataflow.confidentiality.analysis.DataFlowConfidentialityAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.ListAppender;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.PCMActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.seff.DatabaseActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.user.UserActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.ActionSequence;

public class ConstraintFeatureTest extends ConstraintTest {
	private final Logger logger = Logger.getLogger(ConstraintFeatureTest.class);
	
	@Test
    @DisplayName("Test whether cycles in datastores are detected")
    public void testCycleDataStores() {
    	var usageModelPath = Paths.get("models", "CycleDatastoreTest", "default.usagemodel");
    	var allocationPath = Paths.get("models", "CycleDatastoreTest", "default.allocation");
    	DataFlowConfidentialityAnalysis analysis = super.initializeAnalysis(usageModelPath, allocationPath);
    	
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
    	DataFlowConfidentialityAnalysis analysis = super.initializeAnalysis(usageModelPath, allocationPath);
    	
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
    	DataFlowConfidentialityAnalysis analysis = super.initializeAnalysis(usageModelPath, allocationPath);
    	
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
    	DataFlowConfidentialityAnalysis analysis = super.initializeAnalysis(usageModelPath, allocationPath);
    	
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
}
