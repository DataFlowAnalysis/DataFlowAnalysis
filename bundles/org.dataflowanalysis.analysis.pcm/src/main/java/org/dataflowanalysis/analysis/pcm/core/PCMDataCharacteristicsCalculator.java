package org.dataflowanalysis.analysis.pcm.core;

import de.uka.ipd.sdq.stoex.AbstractNamedReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.And;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.False;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.Not;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.Or;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.Term;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.True;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.dataflowanalysis.pcm.extension.model.confidentiality.dictionary.DictionaryPackage;
import org.dataflowanalysis.pcm.extension.model.confidentiality.dictionary.PCMDataDictionary;
import org.dataflowanalysis.pcm.extension.model.confidentiality.expression.LhsEnumCharacteristicReference;
import org.dataflowanalysis.pcm.extension.model.confidentiality.expression.NamedEnumCharacteristicReference;

public class PCMDataCharacteristicsCalculator {
    private final List<DataCharacteristic> currentVariables;
    private final ResourceProvider resourceLoader;

    /**
     * Initialize Data characteristics Calculator with initial characteristics. In addition, the read-only container for
     * node characteristics is created. See {@link PCMDataCharacteristicsCalculator#createNodeCharacteristicsContainer}
     * @param initialCharacteristics Data characteristics of the previous vertex
     * @param nodeCharacteristics Vertex characteristics that might be referenced in the calculator
     * @param resourceProvider Resource provider to resolve unknown characteristics in the dictionary
     */
    public PCMDataCharacteristicsCalculator(List<DataCharacteristic> initialCharacteristics, List<CharacteristicValue> nodeCharacteristics,
            ResourceProvider resourceProvider) {
        this.currentVariables = new ArrayList<>(initialCharacteristics);
        this.resourceLoader = resourceProvider;
        createNodeCharacteristicsContainer(nodeCharacteristics);
    }

    /**
     * Create the container for vertex characteristics. Each node characteristic is saved within the container data
     * characteristics with its characteristic type and value.
     * <p>
     * Furthermore, vertex characteristics cannot be modified by variable characterisations, so this variable is read-only.
     * @param vertexCharacteristics Given list of vertex characteristics present at the current node
     */
    private void createNodeCharacteristicsContainer(List<CharacteristicValue> vertexCharacteristics) {
        DataCharacteristic vertexCharacteristicsContainer = new DataCharacteristic("container");
        for (CharacteristicValue vertexCharacteristic : vertexCharacteristics) {
            vertexCharacteristicsContainer = vertexCharacteristicsContainer.addCharacteristic(vertexCharacteristic);
        }
        this.currentVariables.add(vertexCharacteristicsContainer);
    }

    /**
     * Evaluate a Variable Characterization with the current data characteristics and update the internal state of the
     * calculator. This method should be called for each Variable Characterization (e.g. Sto-ex)
     * <p>
     * For easier use, the state of characteristics at a given sequence element, is managed and updated by calling this
     * method. The final data characteristics for an element are accessed with
     * {@link PCMDataCharacteristicsCalculator#getCalculatedCharacteristics()}.
     * @param variableCharacterisation Variable Characterization at the vertex
     */
    public void evaluate(ConfidentialityVariableCharacterisation variableCharacterisation) {
        var leftHandSide = (LhsEnumCharacteristicReference) variableCharacterisation.getLhs();

        EnumCharacteristicType characteristicType = (EnumCharacteristicType) leftHandSide.getCharacteristicType();
        Literal characteristicValue = leftHandSide.getLiteral();
        Term rightHandSide = variableCharacterisation.getRhs();

        AbstractNamedReference reference = variableCharacterisation.getVariableUsage_VariableCharacterisation()
                .getNamedReference__VariableUsage();
        if (reference.getReferenceName()
                .isBlank()) {
            throw new IllegalArgumentException("Variable Name may not be null!");
        }
        DataCharacteristic existingCharacteristic = this.getDataCharacteristicByReference(reference)
                .orElse(new DataCharacteristic(reference.getReferenceName()));

        List<CharacteristicValue> modifiedCharacteristics = calculateModifiedCharacteristics(existingCharacteristic, characteristicType,
                characteristicValue);

        DataCharacteristic modifiedVariable = createModifiedDataCharacteristic(existingCharacteristic, modifiedCharacteristics, rightHandSide);
        currentVariables.remove(existingCharacteristic);
        currentVariables.add(modifiedVariable);
    }

    /**
     * Get a data characteristic by a named reference
     * @param reference Named reference, which contains the name of the data characteristics
     * @return Returns an Optional containing the data characteristic, if the variable can be found in the list of currently
     * available data characteristics
     */
    private Optional<DataCharacteristic> getDataCharacteristicByReference(AbstractNamedReference reference) {
        String variableName = reference.getReferenceName();
        return this.currentVariables.stream()
                .filter(it -> it.variableName()
                        .equals(variableName))
                .findAny();
    }

    /**
     * Creates a modified data characteristic according to the old characteristics of the existing data characteristic and
     * the modified characteristics.
     * @param existingCharacteristic Existing data characteristic with the same name
     * @param modifiedCharacteristics Characteristics of the data characteristic that are modified
     * @param rightHandSide Right hand side of the variable characterization, to indicate whether a characteristic is added
     * or not
     * @return Returns a new data characteristic with the updated characteristics
     */
    private DataCharacteristic createModifiedDataCharacteristic(DataCharacteristic existingCharacteristic,
            List<CharacteristicValue> modifiedCharacteristics, Term rightHandSide) {
        DataCharacteristic computedVariable = new DataCharacteristic(existingCharacteristic.variableName());
        var unmodifiedCharacteristics = existingCharacteristic.getAllCharacteristics()
                .stream()
                .filter(it -> !modifiedCharacteristics.contains(it))
                .toList();

        for (CharacteristicValue unmodifiedCharacteristic : unmodifiedCharacteristics) {
            computedVariable = computedVariable.addCharacteristic(unmodifiedCharacteristic);
        }

        for (CharacteristicValue modifiedCharacteristic : modifiedCharacteristics) {
            if (evaluateTerm(rightHandSide, modifiedCharacteristic)) {
                List<CharacteristicValue> modifiedCharacteristicValues = computedVariable.getAllCharacteristics()
                        .stream()
                        .filter(it -> it.getTypeName()
                                .equals(modifiedCharacteristic.getTypeName()))
                        .toList();

                if (modifiedCharacteristicValues.stream()
                        .noneMatch(it -> it.getValueName()
                                .equals(modifiedCharacteristic.getValueName()))) {
                    computedVariable = computedVariable.addCharacteristic(modifiedCharacteristic);
                }
            }
        }
        return computedVariable;
    }

    /**
     * Calculates the list of modified characteristics with the given characteristic types and values
     * @param existingCharacteristic Data characteristic which should be modified
     * @param characteristicType Bound for the characteristic type. May be null to allow a wildcard
     * @param characteristicValue Bound for the characteristic value. May be null to allow a wildcard
     * @return Returns the list of all characteristics that are modified with the given bounds
     */
    private List<CharacteristicValue> calculateModifiedCharacteristics(DataCharacteristic existingCharacteristic,
            EnumCharacteristicType characteristicType, Literal characteristicValue) {
        if (characteristicValue == null && characteristicType != null) {
            return discoverNewVariables(existingCharacteristic, Optional.of(characteristicType));
        } else if (characteristicValue == null) {
            return discoverNewVariables(existingCharacteristic, Optional.empty());
        } else {
            return List.of(existingCharacteristic.getAllCharacteristics()
                    .stream()
                    .filter(it -> it.getValueName()
                            .equals(characteristicValue.getName()))
                    .filter(it -> it.getTypeName()
                            .equals(characteristicType.getName()))
                    .findAny()
                    .orElse(new PCMCharacteristicValue(characteristicType, characteristicValue)));
        }
    }

    /**
     * Evaluates the term (e.g. Right Hand Side) of a given Variable Characterization
     * @param term Right Hand Side of the expression
     * @param characteristicValue Characteristic value that is modified
     * @return Returns, whether the characteristic value should be set
     */
    private boolean evaluateTerm(Term term, CharacteristicValue characteristicValue) {
        if (term instanceof True) {
            return true;
        } else if (term instanceof False) {
            return false;
        } else if (term instanceof NamedEnumCharacteristicReference) {
            return evaluateNamedReference((NamedEnumCharacteristicReference) term, characteristicValue);
        } else if (term instanceof And andTerm) {
            return evaluateTerm(andTerm.getLeft(), characteristicValue) && evaluateTerm(andTerm.getRight(), characteristicValue);
        } else if (term instanceof Or orTerm) {
            return evaluateTerm(orTerm.getLeft(), characteristicValue) || evaluateTerm(orTerm.getRight(), characteristicValue);
        } else if (term instanceof Not notTerm) {
            return !evaluateTerm(notTerm.getTerm(), characteristicValue);
        } else {
            throw new IllegalArgumentException("Unknown type: " + term.getClass()
                    .getName());
        }
    }

    /**
     * Evaluates a named reference with a given characteristic value
     * @param characteristicReference Right hand side with a reference to a characteristic
     * @param characteristicValue Characteristic value that is modified
     * @return Returns, whether the characteristic reference evaluates to true or false (or is undefined)
     */
    private boolean evaluateNamedReference(NamedEnumCharacteristicReference characteristicReference, CharacteristicValue characteristicValue) {
        if (characteristicReference.getNamedReference()
                .getReferenceName()
                .isBlank()) {
            throw new IllegalArgumentException("Variable Name in right hand side of StoEx may not be blank!");
        }
        var optionalDataCharacteristic = getDataCharacteristicByReference(characteristicReference.getNamedReference());
        if (optionalDataCharacteristic.isEmpty()) {
            return false;
        }
        var dataCharacteristic = optionalDataCharacteristic.get();
        var characteristicReferenceTypeName = characteristicReference.getCharacteristicType() != null
                ? characteristicReference.getCharacteristicType()
                        .getName()
                : characteristicValue.getTypeName();
        var characteristicReferenceValueName = characteristicReference.getLiteral() != null ? characteristicReference.getLiteral()
                .getName() : characteristicValue.getValueName();

        var characteristic = dataCharacteristic.getAllCharacteristics()
                .stream()
                .filter(it -> it.getTypeName()
                        .equals(characteristicReferenceTypeName))
                .filter(it -> it.getValueName()
                        .equals(characteristicReferenceValueName))
                .findAny();
        return characteristic.isPresent() && dataCharacteristic.hasCharacteristic(characteristic.get());
    }

    /**
     * Discovers all possible characteristics of a characteristic with an optional bound for the characteristic type
     * @param characteristic Data characteristic of which the characteristics should be discovered
     * @param characteristicType Optional bound for the discovered characteristics
     * @return List of characteristics available for the given characteristic and satisfying the possible bound
     */
    private List<CharacteristicValue> discoverNewVariables(DataCharacteristic characteristic, Optional<EnumCharacteristicType> characteristicType) {
        List<CharacteristicValue> updatedCharacteristicValues = new ArrayList<>();
        var dataDictionaries = this.resourceLoader.lookupToplevelElement(DictionaryPackage.eINSTANCE.getPCMDataDictionary())
                .stream()
                .filter(PCMDataDictionary.class::isInstance)
                .map(PCMDataDictionary.class::cast)
                .toList();

        List<EnumCharacteristicType> characteristicTypes = dataDictionaries.stream()
                .flatMap(it -> it.getCharacteristicTypes()
                        .stream())
                .filter(it -> characteristicType.isEmpty() || it.getName()
                        .equals(characteristicType.get()
                                .getName()))
                .filter(EnumCharacteristicType.class::isInstance)
                .map(EnumCharacteristicType.class::cast)
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(EnumCharacteristicType::getName))),
                        ArrayList<EnumCharacteristicType>::new));

        characteristicTypes.forEach(enumCharacteristicType -> enumCharacteristicType.getType()
                .getLiterals()
                .forEach(characteristicValue -> updatedCharacteristicValues
                        .add(new PCMCharacteristicValue(enumCharacteristicType, characteristicValue))));
        return updatedCharacteristicValues;
    }

    /**
     * Returns the list of data characteristics that were calculated according to the VariableCharacterizations provided
     * @return List of data characteristics after evaluating
     */
    public List<DataCharacteristic> getCalculatedCharacteristics() {
        return this.currentVariables.stream()
                .filter(df -> !df.variableName()
                        .equals("container"))
                .collect(Collectors.toList());
    }
}
