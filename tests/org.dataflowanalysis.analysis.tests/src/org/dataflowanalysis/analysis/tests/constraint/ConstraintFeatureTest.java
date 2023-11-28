package org.dataflowanalysis.analysis.tests.constraint;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.entity.pcm.PCMActionSequence;
import org.dataflowanalysis.analysis.entity.pcm.user.UserActionSequenceElement;
import org.dataflowanalysis.analysis.entity.sequence.ActionSequence;
import org.dataflowanalysis.analysis.tests.ListAppender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ConstraintFeatureTest extends ConstraintTest {
	private final Logger logger = Logger.getLogger(ConstraintFeatureTest.class);
	
	@Test
  @Disabled
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
    
    /**
     * Test determining whether node characteristics work correctly
     */
    @Test
    @Disabled
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
    @Disabled
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
