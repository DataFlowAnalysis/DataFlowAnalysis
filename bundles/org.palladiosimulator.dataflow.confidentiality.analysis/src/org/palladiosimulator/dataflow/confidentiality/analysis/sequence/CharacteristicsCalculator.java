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

    /**
     * Initialize Characteristic Calculator with initial variables.
     * In addition the read-only container for node characteristics is created. See {@link createNodeCharacteristicsContainer}
     * 
     * @param initialVariables
     *            DataFlowVariables of the previous ActionSequence Element
     */
    public CharacteristicsCalculator(List<DataFlowVariable> initialVariables, List<CharacteristicValue> nodeCharacteristics) {
        this.currentVariables = new ArrayList<>(initialVariables);
        createNodeCharacteristicsContainer(nodeCharacteristics);
    }
    
    /**
     * Create the container for node characteristics. 
     * Each node characteristic is saved within the container DataFlowVariable with it's characteristic type and value.
     * <p>
     * Furthermore, node characteristics cannot be modified by variable characterisations, so this variable is read-only.
     * @param nodeCharacteristics
     */
    private void createNodeCharacteristicsContainer(List<CharacteristicValue> nodeCharacteristics) {
    	DataFlowVariable nodeCharacteristicContainer = new DataFlowVariable("container");
        nodeCharacteristics.forEach(it -> nodeCharacteristicContainer.addCharacteristic(it));
        this.currentVariables.add(nodeCharacteristicContainer);
    }

    /**
     * Evaluate a Variable Characterization with the current Variables and update the internal state of the characteristics calculator.
     * This method should be called for each Variable Characterization (e.g. Stoex)
     * <p>
     * For easier use, the state of characteristics at a given sequence element, is managed and updated by calling this method.
     * The final DataflowVariables for an element are accessed with {@link getCalculatedVariables}.
     * 
     * @param variableCharacterisation
     *            Variable Characterization at the Sequence Element
     */
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

    /**
     * Calculates the list of modified characteristics with the given characteristic types and
     * values
     * 
     * @param existingVariable
     *            DataFlowVariable which should be modified
     * @param characteristicType
     *            Bound for the characteristic type. May be null to allow a wildcard
     * @param characteristicValue
     *            Bound for the characteristic value. May be null to allow a wildcard
     * @return Returns the list of all characteristics that are modified with the given bounds
     */
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

    /**
     * Evaluates the term (e.g. Right Hand Side) of a given Variable Characterization
     * 
     * @param term
     *            Right Hand Side of the expression
     * @param characteristicValue
     *            Characteristic value that is modified
     * @return Returns, whether the characteristic value should be set
     */
    private boolean evaluateTerm(Term term, CharacteristicValue characteristicValue) {
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

    /**
     * Evaluates a named reference with a given characteristic value
     * 
     * @param characteristicReference
     *            Right hand side with a reference to a characteristic
     * @param characteristicValue
     *            Characteristic value that is modified
     * @return Returns, whether the characteristic reference evaluates to true or false (or is
     *         undefined)
     */
    private boolean evaluateNamedReference(NamedEnumCharacteristicReference characteristicReference,
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
        var characteristicReferenceType = characteristicReference.getCharacteristicType() != null
                ? characteristicReference.getCharacteristicType()
                : characteristicValue.characteristicType();
        var characteristicReferenceValue = characteristicReference.getLiteral() != null
                ? characteristicReference.getLiteral()
                : characteristicValue.characteristicLiteral();

        var characteristic = dataflowVariable.getAllCharacteristics()
            .stream()
            .filter(it -> it.characteristicType()
                .getName()
                .equals(characteristicReferenceType.getName()))
            .filter(it -> it.characteristicLiteral()
                .getName()
                .equals(characteristicReferenceValue.getName()))
            .findAny();
        return !characteristic.isEmpty() && dataflowVariable.hasCharacteristic(characteristic.get());
    }

    /**
     * Discovers all possible characteristics of a variable with an optional bound for the
     * characteristic type
     * 
     * @param variable
     *            DataFlowVariable of which the characteristics should be discovered
     * @param characteristicType
     *            Optional bound for the discovered characteristics
     * @return List of characteristics available for the given variable and satisfying the possible
     *         bound
     */
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

    /**
     * Returns the list of DataFlowVariables that were calculated according to the
     * VariableCharacterizations provided
     * 
     * @return List of DataFlowVariables after evaluating
     */
    public List<DataFlowVariable> getCalculatedCharacteristics() {
        return this.currentVariables.stream()
        		.filter(df -> !df.variableName().equals("container"))
        		.collect(Collectors.toList());
    }
}
