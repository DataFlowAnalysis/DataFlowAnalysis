package org.dataflowanalysis.analysis.tests.dsl;

import org.dataflowanalysis.analysis.core.FlowGraph;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.ConstraintDSL;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.DataCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.selectors.NodeCharacteristicsSelector;
import org.dataflowanalysis.analysis.pcm.core.PCMFlowGraph;

import java.util.List;

public class DSLDemonstration {
    public void testDSL() {
        var constraint = new ConstraintDSL()
                .ofData()
                .withLabel("Sensitivity", "Personal")
                .withoutLabel("Encryption", "Encrypted")
                .neverFlows()
                .toNode()
                .withLabel("Location", "nonEU")
                .create();
    }

    public void testDataObjects() {
        AnalysisConstraint constraint = new AnalysisConstraint();
        constraint.addFlowSource(new DataCharacteristicsSelector(List.of(new CharacteristicsSelectorData("Sensitivity", "Personal")), false));
        constraint.addFlowSource(new DataCharacteristicsSelector(List.of(new CharacteristicsSelectorData("Encryption", "Encrypted")), true));
        constraint.addFlowDestination(new NodeCharacteristicsSelector(List.of(new CharacteristicsSelectorData("Location", "nonEU"))));

        FlowGraph flowGraph = new PCMFlowGraph(null); // Placeholder for analysis flow graph

        var violations = flowGraph.getPartialFlowGraphs().stream()
                .flatMap(pfg -> constraint.matchPartialFlowGraph(pfg).stream())
                .toList();
    }
}