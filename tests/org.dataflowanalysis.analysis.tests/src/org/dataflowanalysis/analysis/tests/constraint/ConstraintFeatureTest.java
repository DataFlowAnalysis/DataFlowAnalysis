package org.dataflowanalysis.analysis.tests.constraint;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.core.ActionSequence;
import org.dataflowanalysis.analysis.pcm.core.user.CallingUserActionSequenceElement;
import org.dataflowanalysis.analysis.pcm.core.user.UserActionSequenceElement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ConstraintFeatureTest extends ConstraintTest {
	private final Logger logger = Logger.getLogger(ConstraintFeatureTest.class);
    
    /**
     * Test determining whether node characteristics work correctly
     */
    @Test
    @DisplayName("Test whether node characteristics works correctly")
    public void testNodeCharacteristics() {
    	var usageModelPath = Paths.get("models", "NodeCharacteristicsTest", "default.usagemodel");
    	var allocationPath = Paths.get("models", "NodeCharacteristicsTest", "default.allocation");
    	var nodeCharacteristicsPath = Paths.get("models", "NodeCharacteristicsTest", "default.nodecharacteristics");
    	DataFlowConfidentialityAnalysis analysis = super.initializeAnalysis(usageModelPath, allocationPath, nodeCharacteristicsPath);
    	
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
    	var nodeCharacteristicsPath = Paths.get("models", "CompositeCharacteristicsTest", "default.nodecharacteristics");
    	DataFlowConfidentialityAnalysis analysis = super.initializeAnalysis(usageModelPath, allocationPath, nodeCharacteristicsPath);
    	
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
    
    /**
     * Test determining whether node characteristics work correctly
     */
    @Test
    @DisplayName("Test whether unknown actions will not cause incorrect results")
    public void testUnkownSEFFActions() {
    	var usageModelPath = Paths.get("models", "IgnoredNodeTest", "default.usagemodel");
    	var allocationPath = Paths.get("models", "IgnoredNodeTest", "default.allocation");
    	var nodeCharacteristicsPath = Paths.get("models", "IgnoredNodeTest", "default.nodecharacteristics");
    	DataFlowConfidentialityAnalysis analysis = super.initializeAnalysis(usageModelPath, allocationPath, nodeCharacteristicsPath);
    	
    	List<ActionSequence> sequences = analysis.findAllSequences();
    	List<ActionSequence> propagatedSequences = analysis.evaluateDataFlows(sequences);
    	
    	logger.setLevel(Level.TRACE);
    	var results = analysis.queryDataFlow(propagatedSequences.get(0), node -> {
    		printNodeInformation(node);
    		if (node instanceof CallingUserActionSequenceElement && ((CallingUserActionSequenceElement) node).isReturning()) {
    			return node.getAllDataFlowVariables().size() != 0;
    		}
    		return false;
        });
        printViolation(results);
        assertTrue(results.size() == 1);
    }
}
