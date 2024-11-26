package org.dataflowanalysis.analysis.tests.integration.constraint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.dataflowanalysis.analysis.tests.integration.AnalysisUtils.TEST_MODEL_PROJECT_NAME;


import java.nio.file.Paths;
import java.util.List;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.core.PCMCharacteristicValue;
import org.dataflowanalysis.examplemodels.Activator;
import org.junit.jupiter.api.Test;

public class MaasTest {
    private DataFlowConfidentialityAnalysis analysis;

    @Test
    public void testRealisticConstraints() {
		 final var usageModelPath = Paths.get("casestudies", "pcm", "MaaS_Ticket_System_base", "MaaS.usagemodel")
	                .toString();
        final var allocationPath = Paths.get("casestudies", "pcm", "MaaS_Ticket_System_base", "MaaS.allocation")
                .toString();
        final var nodeCharPath = Paths.get("casestudies", "pcm", "MaaS_Ticket_System_base", "MaaS.nodecharacteristics")
	                .toString();
        
        analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useUsageModel(usageModelPath)
                .useAllocationModel(allocationPath)
                .useNodeCharacteristicsModel(nodeCharPath)
                .build();

        analysis.initializeAnalysis();
        var flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();

        // Constraint 1:Plain text and granular data associated with a customer c are only readable by c

        int violationsFound = 0;
        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            var violations = analysis.queryDataFlow(transposeFlowGraph, it -> {
                var nodeLabels = retrieveNodeLabels(it);
                var dataLabels = retrieveDataLabels(it);

                return (!dataLabels.contains("Encrypted") || dataLabels.contains("FineGranular")) && dataLabels.contains("Customer")
                        && (dataLabels.contains("STS") || dataLabels.contains("LTS") || dataLabels.contains("TripData"))
                        && !nodeLabels.contains("Customer");
            });

            violationsFound += violations.size();
        }
        assertEquals(0, violationsFound);

        // Constraint 2: Ticket inspectors must not be able to trace past trips of customers c
        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            var violations = analysis.queryDataFlow(transposeFlowGraph, it -> {
                var nodeLabels = retrieveNodeLabels(it);
                var dataLabels = retrieveDataLabels(it);

                return dataLabels.contains("Vehicle") && dataLabels.contains("VehicleData") && dataLabels.contains("Customer")
                        && dataLabels.contains("TripData") && nodeLabels.contains("Inspector");
            });

            assertEquals(0, violations.size());
        }

        // Constraint 3: No granular information about individual trips of customer must be visible to the company
        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            var violations = analysis.queryDataFlow(transposeFlowGraph, it -> {
                var nodeLabels = retrieveNodeLabels(it);
                var dataLabels = retrieveDataLabels(it);

                return dataLabels.contains("FineGranular") && dataLabels.contains("Customer")
                        && (dataLabels.contains("STS") || dataLabels.contains("LTS") || dataLabels.contains("TripData"))
                        && nodeLabels.contains("MobilityProvider");
            });

            assertEquals(0, violations.size());
        }

        // Constraint 4: Staff E must not be able to learn which trips customers C took
        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            var violations = analysis.queryDataFlow(transposeFlowGraph, it -> {
                var nodeLabels = retrieveNodeLabels(it);
                var dataLabels = retrieveDataLabels(it);

                return dataLabels.contains("FineGranular") && dataLabels.contains("Customer") && dataLabels.contains("TripData")
                        && (nodeLabels.contains("SupportStaff") || nodeLabels.contains("BillingStaff") || nodeLabels.contains("AnalysisStaff")
                                || nodeLabels.contains("Administrators"));
            });

            assertEquals(0, violations.size());
        }

        // Constraint 5: Data stored in the ticket system must not be mutable or removable except for valid actions.
        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            var violations = analysis.queryDataFlow(transposeFlowGraph, it -> {
                var nodeLabels = retrieveNodeLabels(it);
                var dataLabelTypes = retrieveDataLabelsTypes(it);

                return dataLabelTypes.contains("writeActions") && nodeLabels.contains("Write");
            });

            assertEquals(0, violations.size());
        }
    }

    private List<String> retrieveNodeLabels(AbstractVertex<?> vertex) {
        return vertex.getAllVertexCharacteristics()
                .stream()
                .map(PCMCharacteristicValue.class::cast)
                .map(PCMCharacteristicValue::getValueName)
                .toList();
    }

    private List<String> retrieveDataLabels(AbstractVertex<?> vertex) {
        return vertex.getAllDataCharacteristics()
                .stream()
                .map(DataCharacteristic::getAllCharacteristics)
                .flatMap(List::stream)
                .map(PCMCharacteristicValue.class::cast)
                .map(PCMCharacteristicValue::getValueName)
                .toList();
    }

    private List<String> retrieveDataLabelsTypes(AbstractVertex<?> vertex) {
        return vertex.getAllDataCharacteristics()
                .stream()
                .map(DataCharacteristic::getAllCharacteristics)
                .flatMap(List::stream)
                .map(PCMCharacteristicValue.class::cast)
                .map(PCMCharacteristicValue::getTypeName)
                .toList();
    }
}
