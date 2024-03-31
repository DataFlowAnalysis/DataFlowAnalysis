package org.dataflowanalysis.analysis.tests.constraint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.pcm.core.PCMFlowGraphCollection;
import org.dataflowanalysis.analysis.pcm.core.user.CallingUserPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.UserPCMVertex;
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
        PCMDataFlowConfidentialityAnalysis analysis = super.initializeAnalysis(usageModelPath, allocationPath, nodeCharacteristicsPath);

        PCMFlowGraphCollection flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();

        logger.setLevel(Level.TRACE);
        var results = analysis.queryDataFlow(flowGraph.getPartialFlowGraphs().get(0), node -> {
            printNodeInformation(node);
            if (node instanceof UserPCMVertex<?>) {
                return node.getAllVertexCharacteristics().size() != 1;
            } else {
                return node.getAllVertexCharacteristics().size() != 2;
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
        PCMDataFlowConfidentialityAnalysis analysis = super.initializeAnalysis(usageModelPath, allocationPath, nodeCharacteristicsPath);

        PCMFlowGraphCollection flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();

        logger.setLevel(Level.TRACE);
        var results = analysis.queryDataFlow(flowGraph.getPartialFlowGraphs().get(0), node -> {
            printNodeInformation(node);
            if (node instanceof UserPCMVertex<?>) {
                return node.getAllVertexCharacteristics().size() != 1;
            } else {
                return node.getAllVertexCharacteristics().size() != 3;
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
    public void testUnknownSEFFActions() {
        var usageModelPath = Paths.get("models", "IgnoredNodeTest", "default.usagemodel");
        var allocationPath = Paths.get("models", "IgnoredNodeTest", "default.allocation");
        var nodeCharacteristicsPath = Paths.get("models", "IgnoredNodeTest", "default.nodecharacteristics");
        PCMDataFlowConfidentialityAnalysis analysis = super.initializeAnalysis(usageModelPath, allocationPath, nodeCharacteristicsPath);

        PCMFlowGraphCollection flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();

        logger.setLevel(Level.TRACE);
        var results = analysis.queryDataFlow(flowGraph.getPartialFlowGraphs().get(0), node -> {
            printNodeInformation(node);
            if (node instanceof CallingUserPCMVertex && ((CallingUserPCMVertex) node).isReturning()) {
                return !node.getAllDataCharacteristics().isEmpty();
            }
            return false;
        });
        printViolation(results);
        assertEquals(1, results.size(), "IgnoredNodeTest did not yield one violation");
    }
}
