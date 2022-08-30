package org.palladiosimulator.dataflow.confidentiality.analysis;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.TEST_MODEL_PROJECT_NAME;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertSequenceElement;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertSequenceElements;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.CallingSEFFActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.CallingUserActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.SEFFActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.testmodels.Activator;

public class DataFlowConfidentialityAnalysisTest {

    @Test
    public void testStandaloneAnalysis() {
        final var usageModelPath = Paths.get("models", "BranchingOnlineShop", "default.usagemodel").toString();
        final var allocationPath = Paths.get("models", "BranchingOnlineShop", "default.allocation").toString();

        final DataFlowConfidentialityAnalysis analysis = new StandalonePCMDataFlowConfidentialtyAnalysis(
                TEST_MODEL_PROJECT_NAME, Activator.class, usageModelPath, allocationPath);

        analysis.initalizeAnalysis();
        var allSequences = analysis.findAllSequences();
        allSequences.stream()
            .map(it -> it.toString())
            .forEach(System.out::println);

        assertFalse(allSequences.isEmpty());
    }

    @Test
    public void testPCMActionSequenceFinder() {
        final var usageModelPath = Paths.get("models", "BranchingOnlineShop", "default.usagemodel").toString();
        final var allocationPath = Paths.get("models", "BranchingOnlineShop", "default.allocation").toString();

        final DataFlowConfidentialityAnalysis analysis = new StandalonePCMDataFlowConfidentialtyAnalysis(
                TEST_MODEL_PROJECT_NAME, Activator.class, usageModelPath, allocationPath);

        analysis.initalizeAnalysis();
        var allSequences = analysis.findAllSequences();

        assertTrue(allSequences.size() == 2);

        assertSequenceElements(allSequences.get(0), CallingUserActionSequenceElement.class,
                CallingSEFFActionSequenceElement.class, SEFFActionSequenceElement.class,
                CallingSEFFActionSequenceElement.class, SEFFActionSequenceElement.class,
                CallingUserActionSequenceElement.class, CallingUserActionSequenceElement.class,
                CallingSEFFActionSequenceElement.class, CallingSEFFActionSequenceElement.class,
                CallingUserActionSequenceElement.class);

        assertSequenceElement(allSequences.get(1), 8, SEFFActionSequenceElement.class);
    }

}
