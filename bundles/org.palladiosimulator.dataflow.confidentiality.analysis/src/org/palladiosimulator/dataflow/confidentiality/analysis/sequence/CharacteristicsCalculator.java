package org.palladiosimulator.dataflow.confidentiality.analysis.sequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.palladiosimulator.dataflow.confidentiality.analysis.PCMAnalysisUtils;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.dictionary.DictionaryPackage;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.dictionary.PCMDataDictionary;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.expression.LhsEnumCharacteristicReference;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.expression.NamedEnumCharacteristicReference;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.expressions.And;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.expressions.False;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.expressions.Or;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.expressions.Term;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.expressions.True;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;

public class CharacteristicsCalculator {
    private List<DataFlowVariable> currentVariables;

    public CharacteristicsCalculator(List<DataFlowVariable> initialVariables) {
        this.currentVariables = initialVariables;
    }

    public void evaluate(VariableCharacterisation variableCharacterisation) {
        // 1. Find variable with given name
        var variableName = variableCharacterisation.getVariableUsage_VariableCharacterisation()
            .getNamedReference__VariableUsage()
            .getReferenceName();
        var confidentialityVariable = (ConfidentialityVariableCharacterisation) variableCharacterisation;
        var leftHandSide = (LhsEnumCharacteristicReference) confidentialityVariable.getLhs();
        var characteristicType = (EnumCharacteristicType) leftHandSide.getCharacteristicType();
        var characteristicValue = leftHandSide.getLiteral();

        var rightHandSide = confidentialityVariable.getRhs();

        var existingVariable = this.currentVariables.stream()
            .filter(it -> it.variableName()
                .equals(variableName))
            .findAny()
            .orElse(new DataFlowVariable(variableName));

        // 2. Process wildcards
        List<CharacteristicValue> modifiedCharacteristics = calculateModifiedCharacteristics(existingVariable,
                characteristicType, characteristicValue);

        // 3. Create new modified DataFlowVariable
        DataFlowVariable computedVariable = new DataFlowVariable(variableName);
        var unmodifiedCharacteristics = existingVariable.getAllCharacteristics()
            .stream()
            .filter(it -> !modifiedCharacteristics.contains(it))
            .collect(Collectors.toList());

        for (CharacteristicValue umodifedCharacteristic : unmodifiedCharacteristics) {
            computedVariable = computedVariable.addCharacteristic(umodifedCharacteristic);
        }

        for (CharacteristicValue modifedCharacteristic : modifiedCharacteristics) {
            if (evaluateTerm(rightHandSide, modifedCharacteristic)) {
                computedVariable = computedVariable.addCharacteristic(modifedCharacteristic);
            }
        }
        currentVariables.remove(existingVariable);
        currentVariables.add(computedVariable);
    }

    private List<CharacteristicValue> calculateModifiedCharacteristics(DataFlowVariable existingVariable,
            EnumCharacteristicType characteristicType, Literal characteristicValue) {
        if (characteristicValue == null && characteristicType != null) {
            return discoverNewVariables(existingVariable, Optional.of(characteristicType));
        } else if (characteristicValue == null && characteristicType == null) {
            return discoverNewVariables(existingVariable, Optional.empty());
        } else {
            return List.of(existingVariable.getAllCharacteristics()
                .stream()
                .filter(it -> it.characteristicLiteral()
                    .getName()
                    .equals(characteristicValue.getName()))
                .filter(it -> it.characteristicType()
                    .getName()
                    .equals(characteristicType.getName()))
                .findAny()
                .orElse(new CharacteristicValue(characteristicType, characteristicValue)));
        }
    }

    public boolean evaluateTerm(Term term, CharacteristicValue characteristicValue) {
        if (term instanceof True) {
            return true;
        } else if (term instanceof False) {
            return false;
        } else if (term instanceof NamedEnumCharacteristicReference) {
            return evaluateNamedReference((NamedEnumCharacteristicReference) term, characteristicValue);
        } else if (term instanceof And) {
            var andTerm = (And) term;
            return evaluateTerm(andTerm.getLeft(), characteristicValue)
                    && evaluateTerm(andTerm.getRight(), characteristicValue);
        } else if (term instanceof Or) {
            var andTerm = (And) term;
            return evaluateTerm(andTerm.getLeft(), characteristicValue)
                    || evaluateTerm(andTerm.getRight(), characteristicValue);
        } else {
            throw new IllegalArgumentException("Unknown type: " + term.getClass()
                .getName());
        }
    }

    public boolean evaluateNamedReference(NamedEnumCharacteristicReference characteristicReference,
            CharacteristicValue characteristicValue) {
        var optionalDataflowVariable = this.currentVariables.stream()
            .filter(it -> it.variableName()
                .equals(characteristicReference.getNamedReference()
                    .getReferenceName()))
            .findAny();
        if (optionalDataflowVariable.isEmpty()) {
            return false;
        }
        var dataflowVariable = optionalDataflowVariable.get();
        if (characteristicReference.getLiteral() == null && characteristicReference.getCharacteristicType() != null) {
            var characteristicSearchValue = dataflowVariable.getAllCharacteristics()
                .stream()
                .filter(it -> it.characteristicType()
                    .getName()
                    .equals(characteristicReference.getCharacteristicType()
                        .getName()))
                .filter(it -> it.characteristicLiteral()
                    .getName()
                    .equals(characteristicValue.characteristicLiteral()
                        .getName()))
                .findAny();
            return !characteristicSearchValue.isEmpty()
                    && dataflowVariable.hasCharacteristic(characteristicSearchValue.get());
        } else if (characteristicReference.getLiteral() == null
                && characteristicReference.getCharacteristicType() == null) {
            var characteristicSearchValue = dataflowVariable.getAllCharacteristics()
                .stream()
                .filter(it -> it.characteristicType()
                    .getName()
                    .equals(characteristicValue.characteristicType()
                        .getName()))
                .filter(it -> it.characteristicLiteral()
                    .getName()
                    .equals(characteristicValue.characteristicLiteral()
                        .getName()))
                .findAny();
            return !characteristicSearchValue.isEmpty()
                    && dataflowVariable.hasCharacteristic(characteristicSearchValue.get());
        } else {
            var characteristicSearchValue = dataflowVariable.getAllCharacteristics()
                .stream()
                .filter(it -> it.characteristicType()
                    .getName()
                    .equals(characteristicReference.getCharacteristicType()
                        .getName()))
                .filter(it -> it.characteristicLiteral()
                    .getName()
                    .equals(characteristicReference.getLiteral()
                        .getName()))
                .findAny();
            return !characteristicSearchValue.isEmpty()
                    && dataflowVariable.hasCharacteristic(characteristicSearchValue.get());
        }
    }

    private static List<CharacteristicValue> discoverNewVariables(DataFlowVariable variable,
            Optional<EnumCharacteristicType> characteristicType) {
        List<CharacteristicValue> updatedCharacteristicValues = new ArrayList<>();
        var dataDictonaries = PCMAnalysisUtils.lookupElementOfType(DictionaryPackage.eINSTANCE.getPCMDataDictionary())
            .stream()
            .filter(PCMDataDictionary.class::isInstance)
            .map(PCMDataDictionary.class::cast)
            .collect(Collectors.toList());

        List<EnumCharacteristicType> characteristicTypes = dataDictonaries.stream()
            .flatMap(it -> it.getCharacteristicTypes()
                .stream())
            .filter(it -> characteristicType.isEmpty() || it.getName()
                .equals(characteristicType.get()
                    .getName()))
            .filter(EnumCharacteristicType.class::isInstance)
            .map(EnumCharacteristicType.class::cast)
            .collect(Collectors.toList());

        characteristicTypes.stream()
            .forEach(enumCharacteristicType -> {
                enumCharacteristicType.getType()
                    .getLiterals()
                    .stream()
                    .forEach(characteristicValue -> {
                        updatedCharacteristicValues
                            .add(new CharacteristicValue(enumCharacteristicType, characteristicValue));
                    });
            });
        return updatedCharacteristicValues;
    }

    public List<DataFlowVariable> getCalculatedCharacteristics() {
        return this.currentVariables;
    }
}
