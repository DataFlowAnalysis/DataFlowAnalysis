package org.dataflowanalysis.analysis.tests.unit.pcm;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Stream;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.pcm.core.PCMVertexCharacteristicsCalculator;
import org.dataflowanalysis.analysis.tests.unit.mock.DummyResourceProvider;
import org.dataflowanalysis.examplemodels.results.ExpectedCharacteristic;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.seff.SeffFactory;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;

public class PCMVertexCharacteristicsCalculatorTest {
    private static DummyResourceProvider dummyResourceProvider;
    private static UsageScenario usageScenario;
    private static UsageScenario otherUsageScenario;
    private static AssemblyContext assemblyContext;
    private static AssemblyContext otherAssemblyContext;
    private static AssemblyContext resourceAssemblyContext;

    @BeforeAll
    public static void setupDataDictionary() {
        dummyResourceProvider = new DummyResourceProvider();
        usageScenario = dummyResourceProvider.getUsageScenario("UsageScenario");
        otherUsageScenario = dummyResourceProvider.getUsageScenario("OtherUsageScenario");
        ResourceContainer resourceContainer = dummyResourceProvider.getResourceContainer("ResourceContainer");
        ResourceContainer otherResourceContainer = dummyResourceProvider.getResourceContainer("AnotherResourceContainer");
        assemblyContext = dummyResourceProvider.addAssemblyContext("AssemblyContext", resourceContainer);
        otherAssemblyContext = dummyResourceProvider.addAssemblyContext("OtherAssemblyContext", otherResourceContainer);
        resourceAssemblyContext = dummyResourceProvider.addAssemblyContext("ResourceAssemblyContext", resourceContainer);

        dummyResourceProvider.addUsageAssignment(usageScenario, "A", "B");
        dummyResourceProvider.addAssemblyAssignment(assemblyContext, "A", "C");
        dummyResourceProvider.addResourceAssignment(resourceContainer, "A", "D");
    }

    private static Stream<Arguments> getValidCharacterizationsResult() {
        return Stream.of(Arguments.of(getAbstractUserAction(usageScenario), List.of(), List.of(ExpectedCharacteristic.of("A", "B"))),
                Arguments.of(getAbstractUserAction(otherUsageScenario), List.of(), List.of()),
                Arguments.of(getUserAction(), List.of(assemblyContext),
                        List.of(ExpectedCharacteristic.of("A", "C"), ExpectedCharacteristic.of("A", "D"))),
                Arguments.of(getUserAction(), List.of(otherAssemblyContext, assemblyContext),
                        List.of(ExpectedCharacteristic.of("A", "C"), ExpectedCharacteristic.of("A", "D"))),
                Arguments.of(getUserAction(), List.of(otherAssemblyContext), List.of()),
                Arguments.of(getUserAction(), List.of(resourceAssemblyContext), List.of(ExpectedCharacteristic.of("A", "D"))));
    }

    @Test
    public void shouldAllowValidAssignments() {
        PCMVertexCharacteristicsCalculator calculator = new PCMVertexCharacteristicsCalculator(dummyResourceProvider);
        assertDoesNotThrow(calculator::checkAssignments);
    }

    @ParameterizedTest
    @MethodSource("getValidCharacterizationsResult")
    public void shouldCalculateCorrectCharacteristics(Entity node, List<AssemblyContext> givenContext,
            List<ExpectedCharacteristic> expectedCharacteristics) {
        Deque<AssemblyContext> context = new ArrayDeque<>();
        givenContext.forEach(context::push);

        PCMVertexCharacteristicsCalculator calculator = new PCMVertexCharacteristicsCalculator(dummyResourceProvider);
        List<CharacteristicValue> vertexCharacteristics = calculator.getVertexCharacteristics(node, context);

        assertEquals(expectedCharacteristics.size(), vertexCharacteristics.size());
        for (ExpectedCharacteristic expectedCharacteristic : expectedCharacteristics) {
            assertTrue(vertexCharacteristics.stream()
                    .filter(it -> it.getTypeName()
                            .equals(expectedCharacteristic.characteristicType()))
                    .anyMatch(it -> it.getValueName()
                            .equals(expectedCharacteristic.characteristicLiteral())));
        }
    }

    private static Entity getAbstractUserAction(UsageScenario usageScenario) {
        ScenarioBehaviour scenario = UsagemodelFactory.eINSTANCE.createScenarioBehaviour();
        scenario.setUsageScenario_SenarioBehaviour(usageScenario);
        AbstractUserAction action = UsagemodelFactory.eINSTANCE.createEntryLevelSystemCall();
        action.setScenarioBehaviour_AbstractUserAction(scenario);
        return action;
    }

    private static Entity getUserAction() {
        return SeffFactory.eINSTANCE.createSetVariableAction();
    }
}
