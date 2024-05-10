package org.dataflowanalysis.analysis.pcm.informationflow.tests.testmodels;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.core.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.IFPCMDataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.pcm.informationflow.core.utils.IFLatticeUtils;
import org.dataflowanalysis.analysis.pcm.informationflow.core.utils.IFPCMDataDictionaryUtils;
import org.dataflowanalysis.analysis.pcm.informationflow.tests.ModelCreationTestUtils;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class TestmodelsTest {

    private final static String SWAPPED_CALLS_WRONG_CALLEE = "SwappedCallsWrongCallee";

    @Test
    void testSwappedCalls() {
        var analysis = ModelCreationTestUtils.createSwappedCallsAnalysis();

        List<AbstractVertex<?>> violations = findViolationsIncomingHigherThanNodeCharacteristic(analysis);

        assertTrue(violations.isEmpty(), "No violations should be found.");
    }

    @Test
    void testSwappedCallsWrongCallee() {
        var analysis = ModelCreationTestUtils.createAnalysisFromModelName(SWAPPED_CALLS_WRONG_CALLEE);

        List<AbstractVertex<?>> violations = findViolationsIncomingHigherThanNodeCharacteristic(analysis);

        assertTrue(!violations.isEmpty(), "Violations should be found. A non-confidential method is called with confidential information.");
    }

    @Disabled
    @Test
    void testBranchingCalls() {
        // TODO
        fail();
    }

    @Disabled
    @Test
    void testBranchingCallsWithImplicitFlowViolation() {
        // TODO
        fail();
    }

    @Disabled
    @Test
    void testBranchingCallsWithImplicitFlowViolationInLastBranch() {
        // TODO
        fail();
    }

    @Disabled
    @Test
    void testBranchingCallsWithImpliitFlowViolationInNestledBranch() {
        // TODO
        fail();
    }

    private List<AbstractVertex<?>> findViolationsIncomingHigherThanNodeCharacteristic(IFPCMDataFlowConfidentialityAnalysis analysis) {
        var flowGraph = analysis.findFlowGraph();
        var propagatedFlowGraph = analysis.evaluateFlowGraph(flowGraph);

        List<AbstractVertex<?>> violations = new ArrayList<>();

        for (AbstractPartialFlowGraph partialFlowGraph : propagatedFlowGraph.getPartialFlowGraphs()) {
            List<? extends AbstractVertex<?>> partialFlowGraphViolations = analysis.queryDataFlow(partialFlowGraph,
                    vertex -> vertexViolatesIncomingHigherThanNode(vertex, analysis.getResourceProvider()));
            violations.addAll(partialFlowGraphViolations);
        }
        return violations;
    }

    private boolean vertexViolatesIncomingHigherThanNode(AbstractVertex<?> vertex, ResourceProvider resourceProvider) {
        List<String> latticeLiteralIds = IFPCMDataDictionaryUtils.getLatticeEnumeration(resourceProvider, "Lattice")
                .getLiterals()
                .stream()
                .map(literal -> literal.getId())
                .toList();
        EnumCharacteristicType lattice = IFPCMDataDictionaryUtils.getLatticeCharacteristicType(resourceProvider, "Lattice");

        List<String> vertexIncomingLatticeCharacteristicNames = vertex.getAllIncomingDataFlowVariables()
                .stream()
                .flatMap(incomingVariable -> incomingVariable.getAllCharacteristics()
                        .stream())
                .filter(incomingCharacteristic -> latticeLiteralIds.contains(incomingCharacteristic.getValueId()))
                .map(incomingCharacteristic -> incomingCharacteristic.getValueName())
                .toList();
        List<String> vertexLatticeCharacteristicNames = vertex.getAllNodeCharacteristics()
                .stream()
                .filter(characteristic -> latticeLiteralIds.contains(characteristic.getValueId()))
                .map(characteristic -> characteristic.getValueName())
                .toList();

        return violationIncomingHigherNode(vertexIncomingLatticeCharacteristicNames, vertexLatticeCharacteristicNames, lattice);
    }

    private boolean violationIncomingHigherNode(List<String> incomingLatticeCharacteristicNames, List<String> nodeLatticeCharacteristicNames,
            EnumCharacteristicType lattice) {

        Map<String, Literal> nameToLiteral = new HashMap<>();
        lattice.getType()
                .getLiterals()
                .stream()
                .forEach(literal -> nameToLiteral.put(literal.getName(), literal));

        List<Literal> incomingLatticeLiterals = mapLiteralNameToLiteral(incomingLatticeCharacteristicNames, nameToLiteral);
        List<Literal> nodeLatticeLiterals = mapLiteralNameToLiteral(nodeLatticeCharacteristicNames, nameToLiteral);
        Literal highestNodeLatticeLiteral = getHighestLiteral(nodeLatticeLiterals, lattice);
        if (highestNodeLatticeLiteral == null) {
            return false;
        }

        for (Literal incomingLiteral : incomingLatticeLiterals) {
            if (IFLatticeUtils.isHigherLevel(incomingLiteral, highestNodeLatticeLiteral)) {
                return true;
            }
        }
        return false;
    }

    private List<Literal> mapLiteralNameToLiteral(List<String> names, Map<String, Literal> nameToLiteralMapping) {
        return names.stream()
                .map(name -> nameToLiteralMapping.get(name))
                .toList();
    }

    private Literal getHighestLiteral(List<Literal> literals, EnumCharacteristicType lattice) {
        Literal highest = literals.size() > 0 ? literals.get(0) : null;
        for (Literal literal : literals) {
            if (IFLatticeUtils.isHigherLevel(literal, highest)) {
                highest = literal;
            }
        }
        return highest;
    }

}
