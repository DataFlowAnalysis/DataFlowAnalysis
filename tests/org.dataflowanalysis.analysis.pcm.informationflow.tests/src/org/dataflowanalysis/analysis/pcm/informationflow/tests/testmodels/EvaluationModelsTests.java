package org.dataflowanalysis.analysis.pcm.informationflow.tests.testmodels;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.pcm.informationflow.IFPCMDataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.pcm.informationflow.IFPCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.informationflow.core.utils.IFLatticeUtils;
import org.dataflowanalysis.analysis.pcm.informationflow.core.utils.IFPCMDataDictionaryUtils;
import org.dataflowanalysis.analysis.pcm.informationflow.tests.IFTestsActivator;
import org.dataflowanalysis.analysis.pcm.informationflow.tests.ModelCreationTestUtils;
import org.dataflowanalysis.analysis.pcm.informationflow.tests.evaluation.EvaluationModelConditionUtils;
import org.dataflowanalysis.analysis.pcm.informationflow.tests.evaluation.EvaluationModelData;
import org.dataflowanalysis.analysis.pcm.informationflow.tests.evaluation.EvaluationModelInstanceData;
import org.dataflowanalysis.analysis.pcm.informationflow.tests.evaluation.EvaluationModels;
import org.dataflowanalysis.analysis.pcm.informationflow.tests.evaluation.EvaluationSpecificationType;
import org.dataflowanalysis.analysis.pcm.informationflow.tests.evaluation.ModelExecutionUtils;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class EvaluationModelsTests {

    private final static Logger logger = Logger.getLogger(EvaluationModelsTests.class);

    private final static String FriendMap = "FriendMap_CallReturn_2L";
    private final static String Hospital = "Hospital_CallReturn_2L";
    private final static String TravelPlanner = "TravelPlanner";

    private IFPCMDataFlowConfidentialityAnalysis friendMapAnalysis;
    private IFPCMDataFlowConfidentialityAnalysis hospitalAnalysis;
    private IFPCMDataFlowConfidentialityAnalysis travelPlannerAnalysis;

    // @BeforeAll
    // void initializeFriendMap() {
    // final var usageModelPath = Paths.get("models", FriendMap, "newUsageModel.usagemodel");
    // final var allocationPath = Paths.get("models", FriendMap, "newAllocation.allocation");
    // final var nodeCharacteristicsPath = Paths.get("models", FriendMap, "default.nodecharacteristics");
    // friendMapAnalysis = initializeAnalysis(usageModelPath, allocationPath, nodeCharacteristicsPath);
    // }

    // @BeforeAll
    // void initializeHospital() {
    // final var usageModelPath = Paths.get("models", Hospital, "newUsageModel.usagemodel");
    // final var allocationPath = Paths.get("models", Hospital, "newAllocation.allocation");
    // final var nodeCharacteristicsPath = Paths.get("models", Hospital, "default.nodecharacteristics");
    // hospitalAnalysis = initializeAnalysis(usageModelPath, allocationPath, nodeCharacteristicsPath);
    // }

    // @BeforeAll
    // void initializeTravelPlanner() {
    // final var usageModelPath = Paths.get("models", TravelPlanner, "travelPlanner.usagemodel");
    // final var allocationPath = Paths.get("models", TravelPlanner, "travelPlanner.allocation");
    // final var nodeCharacteristicsPath = Paths.get("models", TravelPlanner, "travelPlanner.nodecharacteristics");
    // travelPlannerAnalysis = initializeAnalysis(usageModelPath, allocationPath, nodeCharacteristicsPath);
    // }

    // @Test
    // void testFriendMap() {
    // var analysis = friendMapAnalysis;
    //
    // // TODO consider encryption! Does it work?
    // // TODO Encryption with or without Encrypted Label? (Without easier)
    // List<AbstractVertex<?>> violations = findViolationsIncomingHigherThanNodeCharacteristic(analysis);
    //
    // assertTrue(violations.isEmpty(), "No violations should be found.");
    // }

    // @Test
    // void testHospital() {
    // var analysis = hospitalAnalysis;
    //
    // List<AbstractVertex<?>> violations = findViolationsIncomingHigherThanNodeCharacteristic(analysis);
    //
    // // TODO consider encryption! Does it work?
    // // TODO Encryption with or without Encrypted Label? (Without easier)
    // logger.info("Violations: ");
    // for (var vertex : violations) {
    // logger.info(vertex);
    // logger.info(vertex.getAllIncomingDataFlowVariables());
    // logger.info(vertex.createPrintableCharacteristicsList(vertex.getAllNodeCharacteristics()));
    // }
    //
    // assertTrue(violations.isEmpty(), "No violations should be found.");
    // }

    // @Test
    // void testTravelPlanner() {
    // var analysis = travelPlannerAnalysis;
    //
    // List<AbstractVertex<?>> violations = findViolationsIncomingHigherThanNodeCharacteristic(analysis);
    //
    // logger.info("Violations:");
    // for (var vertex : violations) {
    // logger.info(vertex.createPrintableNodeInformation());
    // logger.info(vertex.createPrintableCharacteristicsList(vertex.getAllNodeCharacteristics()));
    // }
    //
    // assertTrue(!violations.isEmpty(), "Declassification is missing!");
    // }

    // TODO Following copied from ModelCreateionTestUtils

    @Disabled
    @Test
    void testImplicitSurrounded() {
        EvaluationModelData modelData = EvaluationModels.implicitSurroundedModel;
        for (var modelInstance : modelData.modelInstances()) {
            ModelExecutionUtils.executeInformationflowAnalysis(modelInstance, modelData.violationCondition());
        }
    }

    @Disabled
    @Test
    void testExplicitProcessed() {
        EvaluationModelData modelData = EvaluationModels.explicitProcessedModelManuallySpecified;
        for (var modelInstance : modelData.modelInstances()) {
            ModelExecutionUtils.executeNormalAnalysis(modelInstance, modelData.violationCondition());
        }
    }

    @Disabled
    @Test
    void testContactSMSNewAnnotation() {
        EvaluationModelInstanceData modelInstanceNoViolation = new EvaluationModelInstanceData(
                Path.of("models", "evaluation-case-studies", "ContactSMS_CallReturn_HL", "noViolation-cvcs", "newUsageModel.usagemodel"),
                Path.of("models", "evaluation-case-studies", "ContactSMS_CallReturn_HL", "noViolation-cvcs", "newAllocation.allocation"),
                Path.of("models", "evaluation-case-studies", "ContactSMS_CallReturn_HL", "noViolation-cvcs", "default.nodecharacteristics"),
                EvaluationSpecificationType.AllConfidentialityCharacterisations, false);
        EvaluationModelInstanceData modelInstanceViolation = new EvaluationModelInstanceData(
                Path.of("models", "evaluation-case-studies", "ContactSMS_CallReturn_HL", "violation-cvcs", "newUsageModel_withIssue.usagemodel"),
                Path.of("models", "evaluation-case-studies", "ContactSMS_CallReturn_HL", "violation-cvcs", "newAllocation_withIssue.allocation"),
                Path.of("models", "evaluation-case-studies", "ContactSMS_CallReturn_HL", "violation-cvcs", "default.nodecharacteristics"),
                EvaluationSpecificationType.AllConfidentialityCharacterisations, true);

        boolean caseNoViolationFound = ModelExecutionUtils.executeNormalAnalysis(modelInstanceNoViolation,
                (vertex) -> EvaluationModelConditionUtils.flowsTo(vertex, "User", "UserAndReceiver"));
        boolean caseViolationFound = ModelExecutionUtils.executeNormalAnalysis(modelInstanceViolation,
                (vertex) -> EvaluationModelConditionUtils.flowsTo(vertex, "User", "UserAndReceiver"));
        assertFalse(caseNoViolationFound);
        assertTrue(caseViolationFound);
    }

    @Disabled
    @Test
    void testDistanceTrackerNewAnnotation() {
        EvaluationModelInstanceData modelInstanceNoViolation = new EvaluationModelInstanceData(
                Path.of("models", "evaluation-case-studies", "DistanceTracker_CallReturn_HL", "noViolation-cvcs", "newUsageModel.usagemodel"),
                Path.of("models", "evaluation-case-studies", "DistanceTracker_CallReturn_HL", "noViolation-cvcs", "newAllocation.allocation"),
                Path.of("models", "evaluation-case-studies", "DistanceTracker_CallReturn_HL", "noViolation-cvcs", "default.nodecharacteristics"),
                EvaluationSpecificationType.AllConfidentialityCharacterisations, false);
        EvaluationModelInstanceData modelInstanceViolation = new EvaluationModelInstanceData(
                Path.of("models", "evaluation-case-studies", "DistanceTracker_CallReturn_HL", "violation-cvcs", "newUsageModel_withIssue.usagemodel"),
                Path.of("models", "evaluation-case-studies", "DistanceTracker_CallReturn_HL", "violation-cvcs", "newAllocation_withIssue.allocation"),
                Path.of("models", "evaluation-case-studies", "DistanceTracker_CallReturn_HL", "violation-cvcs", "default.nodecharacteristics"),
                EvaluationSpecificationType.AllConfidentialityCharacterisations, true);

        boolean caseNoViolationFound = ModelExecutionUtils.executeNormalAnalysis(modelInstanceNoViolation,
                (vertex) -> EvaluationModelConditionUtils.flowsTo(vertex, "UserAndDistanceTracker", "OnlyDistance"));
        boolean caseViolationFound = ModelExecutionUtils.executeNormalAnalysis(modelInstanceViolation,
                (vertex) -> EvaluationModelConditionUtils.flowsTo(vertex, "UserAndDistanceTracker", "OnlyDistance"));
        assertFalse(caseNoViolationFound);
        assertTrue(caseViolationFound);
    }

    @Disabled
    @Test
    void testFriendMapNewAnnotation() {
        EvaluationModelInstanceData modelInstanceNoViolation = new EvaluationModelInstanceData(
                Path.of("models", "evaluation-case-studies", "FriendMap_CallReturn_HL", "noViolation", "newUsageModel.usagemodel"),
                Path.of("models", "evaluation-case-studies", "FriendMap_CallReturn_HL", "noViolation", "newAllocation.allocation"),
                Path.of("models", "evaluation-case-studies", "FriendMap_CallReturn_HL", "noViolation", "default.nodecharacteristics"),
                EvaluationSpecificationType.NeededConfidentialityCharacterisations, false);
        EvaluationModelInstanceData modelInstanceViolation = new EvaluationModelInstanceData(
                Path.of("models", "evaluation-case-studies", "FriendMap_CallReturn_HL", "violation", "newUsageModel_withIssue.usagemodel"),
                Path.of("models", "evaluation-case-studies", "FriendMap_CallReturn_HL", "violation", "newAllocation_withIssue.allocation"),
                Path.of("models", "evaluation-case-studies", "FriendMap_CallReturn_HL", "violation", "default.nodecharacteristics"),
                EvaluationSpecificationType.NeededConfidentialityCharacterisations, true);

        boolean caseNoViolationFound = ModelExecutionUtils.executeAnalysis(modelInstanceNoViolation,
                (vertex) -> EvaluationModelConditionUtils.flowsTo(vertex, "Lattice", "High", "NodeClearance", "Attack"));
        boolean caseViolationFound = ModelExecutionUtils.executeAnalysis(modelInstanceViolation,
                (vertex) -> EvaluationModelConditionUtils.flowsTo(vertex, "Lattice", "High", "NodeClearance", "Attack"));
        assertFalse(caseNoViolationFound);
        assertTrue(caseViolationFound);
    }

    @Disabled
    @Test
    void testHospitalNewAnnotation() {
        EvaluationModelInstanceData modelInstanceNoViolation = new EvaluationModelInstanceData(
                Path.of("models", "evaluation-case-studies", "Hospital_CallReturn_HL", "noViolation", "newUsageModel.usagemodel"),
                Path.of("models", "evaluation-case-studies", "Hospital_CallReturn_HL", "noViolation", "newAllocation.allocation"),
                Path.of("models", "evaluation-case-studies", "Hospital_CallReturn_HL", "noViolation", "default.nodecharacteristics"),
                EvaluationSpecificationType.NeededConfidentialityCharacterisations, false);
        EvaluationModelInstanceData modelInstanceViolation = new EvaluationModelInstanceData(
                Path.of("models", "evaluation-case-studies", "Hospital_CallReturn_HL", "violation", "newUsageModel_withIssue.usagemodel"),
                Path.of("models", "evaluation-case-studies", "Hospital_CallReturn_HL", "violation", "newAllocation_withIssue.allocation"),
                Path.of("models", "evaluation-case-studies", "Hospital_CallReturn_HL", "violation", "default.nodecharacteristics"),
                EvaluationSpecificationType.NeededConfidentialityCharacterisations, true);

        boolean caseNoViolationFound = ModelExecutionUtils.executeAnalysis(modelInstanceNoViolation,
                (vertex) -> EvaluationModelConditionUtils.flowsTo(vertex, "Lattice", "High", "NodeClearance", "Attack"));
        boolean caseViolationFound = ModelExecutionUtils.executeAnalysis(modelInstanceViolation,
                (vertex) -> EvaluationModelConditionUtils.flowsTo(vertex, "Lattice", "High", "NodeClearance", "Attack"));
        assertFalse(caseNoViolationFound);
        assertTrue(caseViolationFound);
    }

    // ----JPMail

    @Disabled
    @Test
    void testTravelPlannerNewAnnotation() {
        EvaluationModelInstanceData modelInstanceNoViolation = new EvaluationModelInstanceData(
                Path.of("models", "evaluation-case-studies", "TravelPlanner_CallReturn_HL", "noViolation-cvcs", "newUsageModel.usagemodel"),
                Path.of("models", "evaluation-case-studies", "TravelPlanner_CallReturn_HL", "noViolation-cvcs", "newAllocation.allocation"),
                Path.of("models", "evaluation-case-studies", "TravelPlanner_CallReturn_HL", "noViolation-cvcs", "default.nodecharacteristics"),
                EvaluationSpecificationType.AllConfidentialityCharacterisations, false);
        EvaluationModelInstanceData modelInstanceViolation = new EvaluationModelInstanceData(
                Path.of("models", "evaluation-case-studies", "TravelPlanner_CallReturn_HL", "violation-cvcs", "newUsageModel_withIssue.usagemodel"),
                Path.of("models", "evaluation-case-studies", "TravelPlanner_CallReturn_HL", "violation-cvcs", "newAllocation.allocation"),
                Path.of("models", "evaluation-case-studies", "TravelPlanner_CallReturn_HL", "violation-cvcs", "default.nodecharacteristics"),
                EvaluationSpecificationType.AllConfidentialityCharacterisations, true);

        boolean caseNoViolationFound = ModelExecutionUtils.executeNormalAnalysis(modelInstanceNoViolation,
                (vertex) -> EvaluationModelConditionUtils.flowsToLower(vertex, "UserAndAirlineAndTravelAgency", "UserAndAirline", "User"));
        boolean caseViolationFound = ModelExecutionUtils.executeNormalAnalysis(modelInstanceViolation,
                (vertex) -> EvaluationModelConditionUtils.flowsToLower(vertex, "UserAndAirlineAndTravelAgency", "UserAndAirline", "User"));
        assertFalse(caseNoViolationFound);
        assertTrue(caseViolationFound);
    }

    // -- WebRTC

    public static final String TEST_MODEL_PROJECT_NAME = "org.dataflowanalysis.analysis.pcm.informationflow.tests";

    public static IFPCMDataFlowConfidentialityAnalysis createAnalysisFromModelName(String modelName) {
        final var usageModelPath = Paths.get("models", modelName, "newUsageModel.usagemodel");
        final var allocationPath = Paths.get("models", modelName, "newAllocation.allocation");
        final var nodeCharacteristicsPath = Paths.get("models", modelName, "default.nodecharacteristics");
        return initializeAnalysis(usageModelPath, allocationPath, nodeCharacteristicsPath);
    }

    private static IFPCMDataFlowConfidentialityAnalysis initializeAnalysis(Path usagePath, Path allocationPath, Path nodePath) {
        return initializeAnalysis(TEST_MODEL_PROJECT_NAME, usagePath, allocationPath, nodePath);
    }

    private static IFPCMDataFlowConfidentialityAnalysis initializeAnalysis(String testModelProjectName, Path usagePath, Path allocationPath,
            Path nodePath) {
        IFPCMDataFlowConfidentialityAnalysis analysis = new IFPCMDataFlowConfidentialityAnalysisBuilder().standalone()
                .modelProjectName(testModelProjectName)
                .usePluginActivator(IFTestsActivator.class)
                .useUsageModel(usagePath.toString())
                .useAllocationModel(allocationPath.toString())
                .useNodeCharacteristicsModel(nodePath.toString())
                .build();
        analysis.initializeAnalysis();
        analysis.setLoggerLevel(Level.ALL);
        return analysis;
    }

    // TODO following copied from TestmodelsTest

    private List<AbstractVertex<?>> findViolationsIncomingHigherThanNodeCharacteristic(IFPCMDataFlowConfidentialityAnalysis analysis) {
        var flowGraph = analysis.findFlowGraph();

        var propagatedFlowGraph = analysis.evaluateFlowGraph(flowGraph);

        for (var partialFlowGraph : propagatedFlowGraph.getPartialFlowGraphs()) {
            logger.info(partialFlowGraph);

            for (var vertex : partialFlowGraph.getVertices()) {
                logger.info(vertex.createPrintableNodeInformation());
                logger.info(vertex.createPrintableCharacteristicsList(vertex.getAllNodeCharacteristics()));
            }

        }

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
