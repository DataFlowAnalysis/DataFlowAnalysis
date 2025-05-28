package org.dataflowanalysis.examplemodels.results;

import java.util.*;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;

public class ExpectedViolation {
    private final int flowGraphIndex;
    private final Identifier identifier;
    private final List<ExpectedCharacteristic> vertexCharacteristics;
    private final Map<String, List<ExpectedCharacteristic>> dataCharacteristics;

    public ExpectedViolation(int flowGraphIndex, Identifier identifier, List<ExpectedCharacteristic> vertexCharacteristics,
            Map<String, List<ExpectedCharacteristic>> dataCharacteristics) {
        this.flowGraphIndex = flowGraphIndex;
        this.identifier = identifier;
        this.vertexCharacteristics = vertexCharacteristics;
        this.dataCharacteristics = dataCharacteristics;
    }

    public boolean references(AbstractVertex<?> element, int flowGraphIndex) {
        return this.identifier.matches(element) && this.flowGraphIndex == flowGraphIndex;
    }

    public List<ExpectedCharacteristic> hasNodeCharacteristic(List<CharacteristicValue> actualCharacteristics) {
        return this.vertexCharacteristics.stream()
                .filter(it -> !this.hasCharacteristicValue(it, actualCharacteristics))
                .toList();
    }

    public List<CharacteristicValue> hasIncorrectNodeCharacteristics(List<CharacteristicValue> actualCharacteristics) {
        return actualCharacteristics.stream()
                .filter(it -> !this.hasExpectedCharacteristic(this.vertexCharacteristics, it))
                .toList();
    }

    public Map<String, List<ExpectedCharacteristic>> hasDataCharacteristics(List<DataCharacteristic> actualDataCharacteristics) {
        Map<String, List<ExpectedCharacteristic>> missingDataCharacteristics = new HashMap<>();
        for (var expectedDataCharacteristic : dataCharacteristics.entrySet()) {
            Optional<DataCharacteristic> actualDataCharacteristic = actualDataCharacteristics.stream()
                    .filter(it -> it.getVariableName()
                            .equals(expectedDataCharacteristic.getKey()))
                    .findAny();
            if (actualDataCharacteristic.isEmpty()) {
                missingDataCharacteristics.put(expectedDataCharacteristic.getKey(), expectedDataCharacteristic.getValue());
                continue;
            }
            List<ExpectedCharacteristic> expectedCharacteristics = expectedDataCharacteristic.getValue();
            List<ExpectedCharacteristic> missingCharacteristics = new ArrayList<>();
            for (var expectedCharacteristic : expectedCharacteristics) {
                if (!this.hasCharacteristicValue(expectedCharacteristic, actualDataCharacteristic.get()
                        .getAllCharacteristics())) {
                    missingCharacteristics.add(expectedCharacteristic);
                }
            }
            if (!missingCharacteristics.isEmpty()) {
                missingDataCharacteristics.put(expectedDataCharacteristic.getKey(), missingCharacteristics);
            }
        }
        return missingDataCharacteristics;
    }

    public Map<String, List<CharacteristicValue>> hasMissingDataCharacteristics(List<DataCharacteristic> actualDataCharacteristics) {
        Map<String, List<CharacteristicValue>> missingDataCharacteristics = new HashMap<>();
        for (var actualDataCharacteristic : actualDataCharacteristics) {
            Optional<List<ExpectedCharacteristic>> expectedCharacteristics = this.dataCharacteristics.entrySet()
                    .stream()
                    .filter(it -> it.getKey()
                            .equals(actualDataCharacteristic.getVariableName()))
                    .map(Map.Entry::getValue)
                    .findAny();
            if (expectedCharacteristics.isEmpty()) {
                missingDataCharacteristics.put(actualDataCharacteristic.getVariableName(), actualDataCharacteristic.getAllCharacteristics());
                continue;
            }
            List<CharacteristicValue> incorrectCharacteristics = new ArrayList<>();
            for (var actualCharacteristic : actualDataCharacteristic.getAllCharacteristics()) {
                if (!this.hasExpectedCharacteristic(expectedCharacteristics.get(), actualCharacteristic)) {
                    incorrectCharacteristics.add(actualCharacteristic);
                }
            }
            if (!incorrectCharacteristics.isEmpty()) {
                missingDataCharacteristics.put(actualDataCharacteristic.getVariableName(), incorrectCharacteristics);
            }
        }
        return missingDataCharacteristics;
    }

    private boolean hasCharacteristicValue(ExpectedCharacteristic expectedCharacteristic, List<CharacteristicValue> actualCharacteristics) {
        return actualCharacteristics.stream()
                .filter(it -> expectedCharacteristic.characteristicType()
                        .equals(it.getTypeName()))
                .anyMatch(it -> expectedCharacteristic.characteristicLiteral()
                        .equals(it.getValueName()));
    }

    private boolean hasExpectedCharacteristic(List<ExpectedCharacteristic> expectedCharacteristics, CharacteristicValue actualCharacteristic) {
        return expectedCharacteristics.stream()
                .filter(it -> it.characteristicType()
                        .equals(actualCharacteristic.getTypeName()))
                .anyMatch(it -> it.characteristicLiteral()
                        .equals(actualCharacteristic.getValueName()));
    }

    public Identifier getIdentifier() {
        return identifier;
    }
}
