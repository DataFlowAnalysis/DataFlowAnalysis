package org.dataflowanalysis.analysis.pcm.informationflow.core.extraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.pcm.informationflow.core.utils.IFConfidentialityVariableCharacterisationUtils;
import org.dataflowanalysis.analysis.pcm.informationflow.core.utils.IFPCMDataDictionaryUtils;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.dataflowanalysis.pcm.extension.model.confidentiality.expression.LhsEnumCharacteristicReference;

/**
 * An {@link IFPCMExtractionStrategy} which prefers defined {@link ConfidentialityVariableCharacterisation} to
 * calculated characterizations.
 */
public abstract class IFPCMExtractionStrategyPrefer extends IFPCMExtractionStrategy {

    private final static Logger logger = Logger.getLogger(IFPCMExtractionStrategyPrefer.class);

    public IFPCMExtractionStrategyPrefer(String latticeName) {
        super(latticeName);
    }

    @Override
    protected List<ConfidentialityVariableCharacterisation> calculateResultingConfidentialityVariableCharacterisations(
            List<ConfidentialityVariableCharacterisation> calculatedCharacterisations,
            List<ConfidentialityVariableCharacterisation> definedCharacterisations, Optional<DataFlowVariable> optionalSecurityContext) {

        List<ConfidentialityVariableCharacterisation> definedLatticeCharacterisations = new ArrayList<>();
        List<ConfidentialityVariableCharacterisation> definedNonLatticeCharacterisations = new ArrayList<>();
        for (var definedCharacterisation : definedCharacterisations) {
            categorizeCharacterisation(definedCharacterisation, definedLatticeCharacterisations, definedNonLatticeCharacterisations);
        }

        checkOnlyOneConfidentialityCharacterisationForEachLevel(definedLatticeCharacterisations);

        return calculateResultingConfidentialityCharacterisationsForCategorizedCharacterisations(calculatedCharacterisations,
                definedCharacterisations, definedLatticeCharacterisations, definedNonLatticeCharacterisations, optionalSecurityContext);
    }

    /**
     * Categorizes a given characterization into lattice and non lattice characterizations. The characterization is added to
     * the corresponding given List. Should wildcards be present in the characterization, the wildcards are resolved and the
     * resulting characterizations categorized.
     * @param characterisation the given characterization
     * @param latticeCharactisation the corresponding List for lattice characterizations
     * @param nonLatticeCharacterisation the corresponding List for non lattice characterizations
     */
    private void categorizeCharacterisation(ConfidentialityVariableCharacterisation characterisation,
            List<ConfidentialityVariableCharacterisation> latticeCharactisation,
            List<ConfidentialityVariableCharacterisation> nonLatticeCharacterisation) {

        LhsEnumCharacteristicReference lhsCharacterisation = (LhsEnumCharacteristicReference) characterisation.getLhs();

        Literal definedLiteral = lhsCharacterisation.getLiteral();
        EnumCharacteristicType definedCharacteristicType = (EnumCharacteristicType) lhsCharacterisation.getCharacteristicType();

        if (definedCharacteristicType == null) {
            List<EnumCharacteristicType> nonLatticCharacteristicTypes = IFPCMDataDictionaryUtils
                    .getAllEnumCharacteristicTypesExceptLattice(getResourceProvider(), getLatticeName());
            EnumCharacteristicType latticeCharacteristicType = IFPCMDataDictionaryUtils.getLatticeCharacteristicType(getResourceProvider(),
                    getLatticeName());

            var resolvedNonLatticeConfidentialityCharacterisations = IFConfidentialityVariableCharacterisationUtils
                    .resolveCharacteristicTypeWildcard(characterisation, nonLatticCharacteristicTypes);
            nonLatticeCharacterisation.addAll(resolvedNonLatticeConfidentialityCharacterisations);
            var resolvedLatticeCharacterisation = IFConfidentialityVariableCharacterisationUtils.resolveWildcards(characterisation,
                    List.of(latticeCharacteristicType));
            latticeCharactisation.addAll(resolvedLatticeCharacterisation);
        } else if (definedLiteral == null) {
            var resolvedCharacterisations = IFConfidentialityVariableCharacterisationUtils.resolveLiteralWildcard(characterisation);
            if (definedCharacteristicType.equals(getLatticeCharacteristicType())) {
                latticeCharactisation.addAll(resolvedCharacterisations);
            } else {
                nonLatticeCharacterisation.addAll(resolvedCharacterisations);
            }
        } else if (lhsCharacterisation.getLiteral()
                .getEnum()
                .equals(getLattice())) {
            latticeCharactisation.add(characterisation);
        } else {
            nonLatticeCharacterisation.add(characterisation);
        }
    }

    /**
     * Calculates the resulting {@link ConfidentialityVariableCharacterisation}s for the given defined and calculated
     * characterizations. The defined characterizations should be further split into lattice and non-lattice
     * characterizations. The resulting characterizations contain the defined non lattice characterizations and prefer
     * defined lattice characterizations over calculated characterizations. The handling of the security context in defined
     * lattice characterizations is handled by
     * {@link #modifyResultingConfidentialityCharacterisationsWithSecurityContext(List, DataFlowVariable)}.
     * @param calculatedCharacterisations the calculated characterizations
     * @param definedCharacterisations all defined characterizations
     * @param definedLatticeChars the defined lattice characterizations
     * @param definedNonLatticeChars the defined non lattice characterizations
     * @param optionalSecurityContext the optional security context
     * @return the resulting {@code ConfidentialityVariableCharacterisation}s
     */
    private List<ConfidentialityVariableCharacterisation> calculateResultingConfidentialityCharacterisationsForCategorizedCharacterisations(
            List<ConfidentialityVariableCharacterisation> calculatedCharacterisations,
            List<ConfidentialityVariableCharacterisation> definedCharacterisations, List<ConfidentialityVariableCharacterisation> definedLatticeChars,
            List<ConfidentialityVariableCharacterisation> definedNonLatticeChars, Optional<DataFlowVariable> optionalSecurityContext) {

        List<ConfidentialityVariableCharacterisation> resultingCharacterisations = new ArrayList<>(definedNonLatticeChars);

        if (definedLatticeChars.isEmpty()) {
            resultingCharacterisations.addAll(calculatedCharacterisations);
        } else if (optionalSecurityContext.isEmpty()) {
            resultingCharacterisations.addAll(definedCharacterisations);
        } else {
            resultingCharacterisations.addAll(
                    modifyResultingConfidentialityCharacterisationsWithSecurityContext(definedCharacterisations, optionalSecurityContext.get()));
        }
        return resultingCharacterisations;
    }

    private void checkOnlyOneConfidentialityCharacterisationForEachLevel(List<ConfidentialityVariableCharacterisation> latticeCharacterisations) {
        for (Literal level : getLattice().getLiterals()) {
            long characterisationsForLevel = latticeCharacterisations.stream()
                    .map(ConfidentialityVariableCharacterisation::getLhs)
                    .filter(LhsEnumCharacteristicReference.class::isInstance)
                    .map(LhsEnumCharacteristicReference.class::cast)
                    .filter(lhs -> lhs.getCharacteristicType()
                            .equals(getLatticeCharacteristicType()))
                    .filter(latticeLhs -> latticeLhs.getLiteral()
                            .equals(level))
                    .count();
            if (characterisationsForLevel > 1) {
                String errorMessage = "For the level '" + level.getName() + "' of the lattice there are multiple definitions in one element.";
                logger.warn(errorMessage);
            }
        }
    }

    /**
     * Modifies the defined {@link ConfidentialityVariableCharacterisation}s in case of implicit flows.
     * @param definedCharacterisations the defined characterizations
     * @param securityContext the security context for the implicit flow
     * @return the resulting characterizations
     */
    protected abstract List<ConfidentialityVariableCharacterisation> modifyResultingConfidentialityCharacterisationsWithSecurityContext(
            List<ConfidentialityVariableCharacterisation> definedCharacterisations, DataFlowVariable securityContext);

}
