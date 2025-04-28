package org.dataflowanalysis.analysis.tests.unit.pcm;

import de.uka.ipd.sdq.stoex.AbstractNamedReference;
import de.uka.ipd.sdq.stoex.StoexFactory;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.pcm.core.PCMCharacteristicValue;
import org.dataflowanalysis.analysis.pcm.core.PCMDataCharacteristicsCalculator;
import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;
import org.dataflowanalysis.analysis.tests.unit.mock.CharacteristicsFactory;
import org.dataflowanalysis.analysis.tests.unit.mock.DummyCharacteristicValue;
import org.dataflowanalysis.analysis.tests.unit.mock.DummyResourceProvider;
import org.dataflowanalysis.examplemodels.results.ExpectedCharacteristic;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.CharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.DataDictionaryCharacterizedFactory;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.And;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.ExpressionsFactory;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.Term;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityFactory;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.dataflowanalysis.pcm.extension.model.confidentiality.expression.ExpressionFactory;
import org.dataflowanalysis.pcm.extension.model.confidentiality.expression.LhsEnumCharacteristicReference;
import org.dataflowanalysis.pcm.extension.model.confidentiality.expression.NamedEnumCharacteristicReference;
import org.dataflowanalysis.pcm.extension.model.confidentiality.expression.VariableCharacterizationLhs;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.parameter.ParameterFactory;
import org.palladiosimulator.pcm.parameter.VariableUsage;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PCMDataCharacteristicsCalculatorTest {
    private static final String STAR = null;
    private static DummyResourceProvider dummyResourceProvider;

    @BeforeAll
    public static void setupDataDictionary() {
        dummyResourceProvider = new DummyResourceProvider();
        dummyResourceProvider.addType("A", List.of("B", "C"));
        dummyResourceProvider.addType("otherA", List.of("B", "C"));
        dummyResourceProvider.addType("NodeType", List.of("NodeValue"));
    }

    private static Stream<Arguments> getValidCharacterizations() {
        return Stream.of(
                // ccd.A.B := TRUE
                Arguments.of(getCharacterization("ccd", "A", "B", getTrueRhs())),
                // ccd.A.* := true
                Arguments.of(getCharacterization("ccd", "A", STAR, getTrueRhs())),
                // ccd.*.* := true
                Arguments.of(getCharacterization("ccd", STAR, STAR, getTrueRhs())),
                // ccd.*.* := RETURN.*.*
                Arguments.of(getCharacterization("ccd", STAR, STAR, getReference("RETURN", STAR, STAR))),
                // ccd.A.B := false && true
                Arguments.of(getCharacterization("ccd", "A", "B", getAndRhs(getFalseRhs(), getTrueRhs()))),
                // ccd.A.B := container.NodeType.NodeValue
                Arguments.of(getCharacterization("ccd", "A", "B", getReference("container", "NodeType", "NodeValue")))
        );
    }

    private static Stream<Arguments> getInvalidCharacterizations() {
        return Stream.of(
                Arguments.of(getCharacterization("", "A", "B", getTrueRhs())),
                Arguments.of(getCharacterization("ccd", STAR, "A", getTrueRhs())),
                Arguments.of(getCharacterization("ccd", STAR, STAR, getReference("", STAR, STAR)))
        );
    }

    private static Stream<Arguments> getValidCharacterizationsResult() {
        return Stream.of(
                // ccd.A.B := TRUE
                Arguments.of(getCharacterization("ccd", "A", "B", getTrueRhs()),
                        Map.of("ccd", List.of(ExpectedCharacteristic.of("A", "B")),
                                "RETURN", List.of(ExpectedCharacteristic.of("otherA", "B")))),
                // ccd.A.* := true
                Arguments.of(getCharacterization("ccd", "A", STAR, getTrueRhs()),
                        Map.of("ccd", List.of(ExpectedCharacteristic.of("A", "B"),  ExpectedCharacteristic.of("A", "C")),
                                "RETURN", List.of(ExpectedCharacteristic.of("otherA", "B")))),
                // ccd.*.* := true
                Arguments.of(getCharacterization("ccd", STAR, STAR, getTrueRhs()),
                        Map.of("ccd", List.of(ExpectedCharacteristic.of("A", "B"),  ExpectedCharacteristic.of("A", "C"),
                                ExpectedCharacteristic.of("otherA", "B"),  ExpectedCharacteristic.of("otherA", "C")),
                                "RETURN", List.of(ExpectedCharacteristic.of("otherA", "B")))),
                // ccd.*.* := RETURN.*.*
                Arguments.of(getCharacterization("ccd", STAR, STAR, getReference("RETURN", STAR, STAR)),
                        Map.of("ccd", List.of(ExpectedCharacteristic.of("otherA", "B")),
                                "RETURN", List.of(ExpectedCharacteristic.of("otherA", "B")))),
                // ccd.A.B := false && true
                Arguments.of(getCharacterization("ccd", "A", "B", getAndRhs(getFalseRhs(), getTrueRhs())),
                        Map.of("ccd", List.of(),
                                "RETURN", List.of(ExpectedCharacteristic.of("otherA", "B")))),
                // ccd.A.B := container.NodeType.NodeValue
                Arguments.of(getCharacterization("ccd", "A", "B", getReference("container", "NodeType", "NodeValue")),
                        Map.of("ccd", List.of(ExpectedCharacteristic.of("A", "B")),
                                "RETURN", List.of(ExpectedCharacteristic.of("otherA", "B"))))
        );
    }

    @ParameterizedTest
    @MethodSource("getValidCharacterizations")
    public void shouldAcceptValidCharacterizations(ConfidentialityVariableCharacterisation characterisation) {
        PCMDataCharacteristicsCalculator calculator = new PCMDataCharacteristicsCalculator(List.of(), List.of(), dummyResourceProvider);
        assertDoesNotThrow(() -> calculator.evaluate(characterisation));
    }

    @ParameterizedTest
    @MethodSource("getInvalidCharacterizations")
    public void shouldRejectInvalidCharacterizations(ConfidentialityVariableCharacterisation characterisation) {
        PCMDataCharacteristicsCalculator calculator = new PCMDataCharacteristicsCalculator(List.of(), List.of(), dummyResourceProvider);
        assertThrows(IllegalArgumentException.class, () -> calculator.evaluate(characterisation));
    }

    @ParameterizedTest
    @MethodSource("getValidCharacterizationsResult")
    public void shouldCalculateCorrectCharacteristics(ConfidentialityVariableCharacterisation characterisation, Map<String, List<ExpectedCharacteristic>> expectedResult) {
        PCMDataCharacteristicsCalculator calculator = new PCMDataCharacteristicsCalculator(
                List.of(new CharacteristicsFactory("RETURN").with("otherA.B")),
                List.of(DummyCharacteristicValue.fromString("NodeType.NodeValue")),
                dummyResourceProvider);
        calculator.evaluate(characterisation);
        var result = calculator.getCalculatedCharacteristics();
        assertEquals(expectedResult.size(), result.size());
        for (var expectedDataCharacteristics : expectedResult.entrySet()) {
            Optional<DataCharacteristic> dataCharacteristic = result.stream()
                    .filter(it -> it.getVariableName().equals(expectedDataCharacteristics.getKey()))
                    .findAny();
            assertTrue(dataCharacteristic.isPresent());
            for(ExpectedCharacteristic expectedCharacteristic : expectedDataCharacteristics.getValue()) {
                assertTrue(dataCharacteristic.get().getAllCharacteristics().stream()
                        .filter(it -> it.getTypeName().equals(expectedCharacteristic.characteristicType()))
                        .anyMatch(it -> it.getValueName().equals(expectedCharacteristic.characteristicLiteral())));
            }
        }
    }

    private static ConfidentialityVariableCharacterisation getCharacterization(String variableName, String characteristicType, String characteristicValue, Term term) {
        ConfidentialityVariableCharacterisation characterisation = ConfidentialityFactory.eINSTANCE.createConfidentialityVariableCharacterisation();
        characterisation.setVariableUsage_VariableCharacterisation(getVariableUsage(variableName));
        characterisation.setLhs(getLhs(characteristicType, characteristicValue));
        characterisation.setRhs(term);
        return characterisation;
    }

    private static VariableUsage getVariableUsage(String name) {
        AbstractNamedReference namedReference = StoexFactory.eINSTANCE.createVariableReference();
        namedReference.setReferenceName(name);

        VariableUsage variableUsage = ParameterFactory.eINSTANCE.createVariableUsage();
        variableUsage.setNamedReference__VariableUsage(namedReference);
        return variableUsage;
    }

    private static VariableCharacterizationLhs getLhs(String characteristicTypeName, String characteristicValueName) {
        LhsEnumCharacteristicReference lhs = ExpressionFactory.eINSTANCE.createLhsEnumCharacteristicReference();

        if (Objects.nonNull(characteristicTypeName)) {
            CharacteristicType characteristicType = DataDictionaryCharacterizedFactory.eINSTANCE.createEnumCharacteristicType();
            characteristicType.setName(characteristicTypeName);
            lhs.setCharacteristicType(characteristicType);
        }

        if (Objects.nonNull(characteristicValueName)) {
            Literal characteristicValue = DataDictionaryCharacterizedFactory.eINSTANCE.createLiteral();
            characteristicValue.setName(characteristicValueName);
            lhs.setLiteral(characteristicValue);
        }
        return lhs;
    }

    private static Term getTrueRhs() {
        return ExpressionsFactory.eINSTANCE.createTrue();
    }

    private static Term getFalseRhs() {
        return ExpressionsFactory.eINSTANCE.createFalse();
    }

    private static Term getAndRhs(Term left, Term right) {
        And term = ExpressionsFactory.eINSTANCE.createAnd();
        term.setLeft(left);
        term.setRight(right);
        return term;
    }

    private static Term getReference(String variableName, String characteristicTypeName, String characteristicValueName) {
        NamedEnumCharacteristicReference term = ExpressionFactory.eINSTANCE.createNamedEnumCharacteristicReference();

        AbstractNamedReference namedReference = StoexFactory.eINSTANCE.createVariableReference();
        namedReference.setReferenceName(variableName);
        term.setNamedReference(namedReference);

        if (Objects.nonNull(characteristicTypeName)) {
            CharacteristicType characteristicType = DataDictionaryCharacterizedFactory.eINSTANCE.createEnumCharacteristicType();
            characteristicType.setName(characteristicTypeName);
            term.setCharacteristicType(characteristicType);
        }

        if (Objects.nonNull(characteristicValueName)) {
            Literal characteristicValue = DataDictionaryCharacterizedFactory.eINSTANCE.createLiteral();
            characteristicValue.setName(characteristicValueName);
            term.setLiteral(characteristicValue);
        }
        return term;
    }
}