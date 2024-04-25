package org.dataflowanalysis.analysis.pcm.informationflow.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.CharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Enumeration;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.And;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.ExpressionsFactory;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.False;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.Not;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.Or;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.Term;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.True;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityFactory;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.dataflowanalysis.pcm.extension.model.confidentiality.expression.ExpressionFactory;
import org.dataflowanalysis.pcm.extension.model.confidentiality.expression.LhsEnumCharacteristicReference;
import org.dataflowanalysis.pcm.extension.model.confidentiality.expression.NamedEnumCharacteristicReference;
import org.dataflowanalysis.pcm.extension.model.confidentiality.expression.VariableCharacterizationLhs;
import org.palladiosimulator.pcm.parameter.ParameterFactory;
import org.palladiosimulator.pcm.parameter.VariableUsage;

import de.uka.ipd.sdq.stoex.AbstractNamedReference;
import de.uka.ipd.sdq.stoex.StoexFactory;
import de.uka.ipd.sdq.stoex.VariableReference;

/**
 * A Utility class for creating {@link ConfidentialityVariableCharacterisation}.
 */
public class IFConfidentialityVariableCharacterisationUtils {

    private static final Logger logger = Logger.getLogger(IFConfidentialityVariableCharacterisationUtils.class);

    private final static StoexFactory stoexFactory = StoexFactory.eINSTANCE;
    private final static ConfidentialityFactory confidentialityFactory = ConfidentialityFactory.eINSTANCE;
    private final static ExpressionsFactory expressionsFactory = ExpressionsFactory.eINSTANCE;
    private final static ExpressionFactory expressionFactory = ExpressionFactory.eINSTANCE;
    private final static ParameterFactory parameterFactory = ParameterFactory.eINSTANCE;

    private IFConfidentialityVariableCharacterisationUtils() {
    }

    /**
     * Resolves wildcards for the {@link CharacteristicType} and {@link Literal} in a
     * {@link ConfidentialityVariableCharacterisation}. The resolving is achieved by creating a
     * {@link ConfidentialityVariableCharacterisation} for each given {@link EnumCharacteristicType} and its corresponding
     * {@link Literal}s.
     * @param characterisation the given characterization
     * @param allCharacteristicTypes the given characteristic types
     * @return the created characterizations
     */
    public static List<ConfidentialityVariableCharacterisation> resolveWildcards(ConfidentialityVariableCharacterisation characterisation,
            List<EnumCharacteristicType> allCharacteristicTypes) {
        return resolveCharacteristicTypeWildcard(characterisation, allCharacteristicTypes).stream()
                .flatMap(confidentialityCharacterisation -> resolveLiteralWildcard(confidentialityCharacterisation).stream())
                .toList();
    }

    /**
     * Resolves wildcards for the {@link Literal} in a {@link ConfidentialityVariableCharacterisation} by creating a
     * {@link ConfidentialityVariableCharacterisation} for each {@link Literal} in the corresponding {@link Enumeration}.
     * Assumes the {@link CharacteristicType} of the {@link ConfidentialityVariableCharacterisation} to be present and an
     * instance of {@link EnumCharacteristicType}.
     * @param characterisation the given characterization
     * @return the created characterizations
     */
    public static List<ConfidentialityVariableCharacterisation> resolveLiteralWildcard(ConfidentialityVariableCharacterisation characterisation) {

        var enumCharacteristicType = (EnumCharacteristicType) getLhsEnumCharacteristicReference(characterisation).getCharacteristicType();
        List<ConfidentialityVariableCharacterisation> resolvedCharacterisations = new ArrayList<>();
        if (getLhsEnumCharacteristicReference(characterisation).getLiteral() != null) {
            resolvedCharacterisations.add(copyConfidentialityVariableCharacterisation(characterisation));
            return resolvedCharacterisations;
        }

        for (Literal level : enumCharacteristicType.getType()
                .getLiterals()) {
            var resolvedCharacterisation = copyConfidentialityVariableCharacterisation(characterisation);

            var lhs = getLhsEnumCharacteristicReference(resolvedCharacterisation);
            lhs.setLiteral(level);

            List<NamedEnumCharacteristicReference> variablesInRhs = extractAllVariables(resolvedCharacterisation.getRhs());
            variablesInRhs.stream()
                    .forEach(variable -> variable.setLiteral(level));

            resolvedCharacterisations.add(resolvedCharacterisation);
        }
        return resolvedCharacterisations;
    }

    /**
     * Resolves wildcards for the {@link CharacteristicType} in a {@link ConfidentialityVariableCharacterisation} by
     * creating a {@link ConfidentialityVariableCharacterisation} for each given {@link EnumCharacteristicType}.
     * @param characterisation the given characterization
     * @param allCharacteristicTypes the given characteristic types
     * @return the created characterizations
     */
    public static List<ConfidentialityVariableCharacterisation> resolveCharacteristicTypeWildcard(
            ConfidentialityVariableCharacterisation characterisation, List<EnumCharacteristicType> allCharacteristicTypes) {

        List<ConfidentialityVariableCharacterisation> resolvedCharacterisations = new ArrayList<>();
        if (getLhsEnumCharacteristicReference(characterisation).getCharacteristicType() != null) {
            resolvedCharacterisations.add(copyConfidentialityVariableCharacterisation(characterisation));
            return resolvedCharacterisations;
        }

        for (var characteristicType : allCharacteristicTypes) {
            var resolvedCharacterisation = copyConfidentialityVariableCharacterisation(characterisation);

            var lhs = getLhsEnumCharacteristicReference(resolvedCharacterisation);
            lhs.setCharacteristicType(characteristicType);

            List<NamedEnumCharacteristicReference> varsInRhs = extractAllVariables(resolvedCharacterisation.getRhs());
            varsInRhs.stream()
                    .forEach(variable -> variable.setCharacteristicType(characteristicType));

            resolvedCharacterisations.add(resolvedCharacterisation);
        }
        return resolvedCharacterisations;
    }

    private static ConfidentialityVariableCharacterisation copyConfidentialityVariableCharacterisation(
            ConfidentialityVariableCharacterisation confidentialityCharacterisation) {

        var lhs = getLhsEnumCharacteristicReference(confidentialityCharacterisation);
        var characterisedVariable = confidentialityCharacterisation.getVariableUsage_VariableCharacterisation()
                .getNamedReference__VariableUsage();

        return createCharacterisation(characterisedVariable, lhs.getCharacteristicType(), lhs.getLiteral(),
                copyTerm(confidentialityCharacterisation.getRhs()));
    }

    /**
     * Creates a {@link ConfidentialityVariableCharacterisation} for each level of the given lattice for the given
     * latticeCharacteristicType. The behavior of the resulting {@link ConfidentialityVariableCharacterisation} is to set
     * the maximum Label of the given characterizations and the constraint. Assumes that the given characterizations only
     * set one level. Assumes that the given characterizations do not have any wildcards. Assumes that only one
     * specification is present for each level - not more. Assumes at least one characterization to be present.
     * @param characterisations the given characterizations
     * @param constraint the given constraint
     * @param latticeCharacteristicType the given CharacteristicType
     * @param lattice the given lattice
     * @return the resulting {@link ConfidentialityVariableCharacterisation} for the lattice
     */
    public static List<ConfidentialityVariableCharacterisation> createModifiedCharacterisationsForAdditionalHigherEqualConstraint(
            List<ConfidentialityVariableCharacterisation> characterisations, AbstractNamedReference constraint,
            CharacteristicType latticeCharacteristicType, Enumeration lattice) {

        if (characterisations.isEmpty()) {
            String errorMessage = "At least one characterization has to be given.";
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        AbstractNamedReference characterizedVariable = characterisations.get(0)
                .getVariableUsage_VariableCharacterisation()
                .getNamedReference__VariableUsage();

        Map<Literal, ConfidentialityVariableCharacterisation> literalToDefinedConfidentialityCharacterisation = new HashMap<>();
        for (var characterisation : characterisations) {
            LhsEnumCharacteristicReference lhsCharacterisation = (LhsEnumCharacteristicReference) characterisation.getLhs();
            literalToDefinedConfidentialityCharacterisation.put(lhsCharacterisation.getLiteral(), characterisation);
        }

        List<ConfidentialityVariableCharacterisation> modifiedCharacterisations = new ArrayList<>();
        for (Literal latticeLevel : lattice.getLiterals()) {
            modifiedCharacterisations.add(createModifiedCharacterisationForLevel(characterizedVariable,
                    literalToDefinedConfidentialityCharacterisation, constraint, latticeLevel, latticeCharacteristicType, lattice));
        }

        return modifiedCharacterisations;
    }

    private static ConfidentialityVariableCharacterisation createModifiedCharacterisationForLevel(AbstractNamedReference characterizedVariable,
            Map<Literal, ConfidentialityVariableCharacterisation> literalToDefinedConfidentialityCharacterisation, AbstractNamedReference constraint,
            Literal level, CharacteristicType latticeCharacteristicType, Enumeration lattice) {

        List<Term> lowerDefinedTerms = new ArrayList<>();
        for (Literal latticeLevel : lattice.getLiterals()) {
            if (IFLatticeUtils.isLowerLevel(latticeLevel, level) && literalToDefinedConfidentialityCharacterisation.get(latticeLevel) != null) {
                lowerDefinedTerms.add(copyTerm(literalToDefinedConfidentialityCharacterisation.get(latticeLevel)
                        .getRhs()));
            }
        }

        Term levelTerm = null;
        if (literalToDefinedConfidentialityCharacterisation.get(level) != null) {
            levelTerm = copyTerm(literalToDefinedConfidentialityCharacterisation.get(level)
                    .getRhs());
        }

        Term levelConstraintVariable = createNamedEnumCharacteristicReference(constraint, latticeCharacteristicType, level);

        List<Term> higherConstraintVariables = new ArrayList<>();
        for (Literal latticeLevel : lattice.getLiterals()) {
            if (IFLatticeUtils.isHigherLevel(latticeLevel, level)) {
                higherConstraintVariables.add(createNamedEnumCharacteristicReference(constraint, latticeCharacteristicType, latticeLevel));
            }
        }

        return createCharacterisation(constraint, latticeCharacteristicType, level,
                createRhsForAdditionalHigherEqualConstraint(lowerDefinedTerms, levelTerm, levelConstraintVariable, higherConstraintVariables));
    }

    private static Term createRhsForAdditionalHigherEqualConstraint(List<Term> lowerDefinedTerms, Term levelTerm, Term levelConstraintVariable,
            List<Term> higherConstraintVariables) {

        Term levelShouldBeSet = expressionsFactory.createFalse();
        if (levelTerm != null) {
            levelShouldBeSet = levelTerm;
        }

        if (!higherConstraintVariables.isEmpty()) {
            Term notHigherConstraintsORed = createNot(createOrTerm(higherConstraintVariables));
            And isApplyableAndConfirmsSecurityContext = expressionsFactory.createAnd();
            isApplyableAndConfirmsSecurityContext.setLeft(levelShouldBeSet);
            isApplyableAndConfirmsSecurityContext.setRight(notHigherConstraintsORed);
            levelShouldBeSet = isApplyableAndConfirmsSecurityContext;
        }

        if (!lowerDefinedTerms.isEmpty()) {
            Term lowerDefinedTermsORed = createOrTerm(lowerDefinedTerms);
            And oneLowerTermTrueAndIsExactlyConstraint = expressionsFactory.createAnd();
            oneLowerTermTrueAndIsExactlyConstraint.setLeft(lowerDefinedTermsORed);
            oneLowerTermTrueAndIsExactlyConstraint.setRight(levelConstraintVariable);

            Or levelConfirmsWithConstraintOrConstraintWhenLowerTrue = expressionsFactory.createOr();
            levelConfirmsWithConstraintOrConstraintWhenLowerTrue.setLeft(levelShouldBeSet);
            levelConfirmsWithConstraintOrConstraintWhenLowerTrue.setRight(oneLowerTermTrueAndIsExactlyConstraint);
            levelShouldBeSet = levelConfirmsWithConstraintOrConstraintWhenLowerTrue;
        }

        return levelShouldBeSet;
    }

    /**
     * Creates a {@link ConfidentialityVariableCharacterisation} for each level of the given lattice for the given
     * latticeCharacteristicType. The behavior of the resulting {@link ConfidentialityVariableCharacterisation} is to only
     * set the lowest level of the lattice.
     * @param characterisedVariable the Reference for the Variable to be characterized
     * @param latticeCharacteristicType the used CharacterisationType
     * @param lattice the lattice for which the ConfidentialityVariableCharacterisations are created
     * @return the resulting ConfidentialityVariableCharacterisations for the lattice
     */
    public static List<ConfidentialityVariableCharacterisation> createSetLowestLevelCharacterisationsForLattice(
            AbstractNamedReference characterisedVariable, CharacteristicType latticeCharacteristicType, Enumeration lattice) {

        Literal lowestLevel = IFLatticeUtils.getLowestLevel(lattice);

        List<ConfidentialityVariableCharacterisation> characterisations = new ArrayList<>();
        for (Literal level : lattice.getLiterals()) {
            if (level.equals(lowestLevel)) {
                characterisations.add(createTrueCharacteristationForLevel(characterisedVariable, latticeCharacteristicType, lowestLevel));
            } else {
                characterisations.add(createFalseCharacteristationForLevel(characterisedVariable, latticeCharacteristicType, lowestLevel));
            }
        }
        return characterisations;
    }

    private static ConfidentialityVariableCharacterisation createTrueCharacteristationForLevel(AbstractNamedReference characterisedVariable,
            CharacteristicType latticeCharacteristicType, Literal level) {

        Term trueTerm = expressionsFactory.createTrue();

        return createCharacterisation(characterisedVariable, latticeCharacteristicType, level, trueTerm);
    }

    private static ConfidentialityVariableCharacterisation createFalseCharacteristationForLevel(AbstractNamedReference characterisedVariable,
            CharacteristicType latticeCharacteristicType, Literal level) {

        Term falseTerm = expressionsFactory.createFalse();
        return createCharacterisation(characterisedVariable, latticeCharacteristicType, level, falseTerm);
    }

    /**
     * Creates a {@link ConfidentialityVariableCharacterisation} for each level of the given lattice for the given
     * latticeCharacteristicType. The behavior of the resulting {@link ConfidentialityVariableCharacterisation} is to only
     * set the maximum Label of the given references. There should always be at least on reference.
     * @param characterisedVariable the Reference for the Variable to be characterized
     * @param references the References for Variables which the characterisedVariable depends upon. This should be at least
     * one Reference
     * @param latticeCharacteristicType the used CharacterisationType
     * @param lattice the lattice for which the ConfidentialityVariableCharacterisations are created
     * @return the resulting ConfidentialityVariableCharacterisations for the lattice
     */
    public static List<ConfidentialityVariableCharacterisation> createMaximumJoinCharacterisationsForLattice(
            AbstractNamedReference characterisedVariable, List<AbstractNamedReference> references, CharacteristicType latticeCharacteristicType,
            Enumeration lattice) {

        return lattice.getLiterals()
                .stream()
                .map(it -> createMaximumJoinCharacterisationForLevel(characterisedVariable, references, latticeCharacteristicType, lattice, it))
                .toList();
    }

    private static ConfidentialityVariableCharacterisation createMaximumJoinCharacterisationForLevel(AbstractNamedReference characterisedVariable,
            List<AbstractNamedReference> references, CharacteristicType latticeCharacteristicType, Enumeration lattice, Literal level) {

        List<Literal> higherLevels = lattice.getLiterals()
                .stream()
                .filter(l -> IFLatticeUtils.isHigherLevel(l, level))
                .toList();

        List<Term> levelDependencies = new ArrayList<>();
        List<Term> higherDependencies = new ArrayList<>();
        for (AbstractNamedReference reference : references) {
            levelDependencies.add(createNamedEnumCharacteristicReference(reference, latticeCharacteristicType, level));
            for (Literal latticeLevel : higherLevels) {
                higherDependencies.add(createNamedEnumCharacteristicReference(reference, latticeCharacteristicType, latticeLevel));
            }
        }

        return createCharacterisation(characterisedVariable, latticeCharacteristicType, level, createRhs(levelDependencies, higherDependencies));
    }

    private static VariableCharacterizationLhs createLhs(CharacteristicType latticeCharacteristicType, Literal literal) {
        // DataCharacteristicCalculator assumes lhs is a LhsEnumCharacteristicReference
        LhsEnumCharacteristicReference lhs = expressionFactory.createLhsEnumCharacteristicReference();

        lhs.setCharacteristicType(latticeCharacteristicType);
        lhs.setLiteral(literal);
        return lhs;
    }

    private static Term createRhs(List<Term> levelVariables, List<Term> higherVariables) {
        Term positiveTerm = createOrTerm(levelVariables);

        if (higherVariables.size() < 1) {
            return positiveTerm;
        }
        Term negatedTerm = createNot(createOrTerm(higherVariables));

        And andTerm = expressionsFactory.createAnd();
        andTerm.setLeft(positiveTerm);
        andTerm.setRight(negatedTerm);
        return andTerm;
    }

    private static Term createOrTerm(List<Term> variables) {
        if (variables.isEmpty()) {
            String errorMessage = "The creation of a ConfidentialityVariableCharacterisation without Variable dependencies is undefined.";
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        Term term = variables.get(0);
        for (int i = 1; i < variables.size(); i++) {
            Or orTerm = expressionsFactory.createOr();
            orTerm.setLeft(term);
            orTerm.setRight(variables.get(i));
            term = orTerm;
        }

        return term;
    }

    private static VariableUsage createVariableUsage(AbstractNamedReference characterisedVariable) {
        VariableUsage variableUsage = parameterFactory.createVariableUsage();

        variableUsage.setNamedReference__VariableUsage(createCopiedReference(characterisedVariable));
        return variableUsage;
    }

    private static Not createNot(Term term) {
        Not negatedTerm = expressionsFactory.createNot();
        negatedTerm.setTerm(term);
        return negatedTerm;
    }

    private static NamedEnumCharacteristicReference createNamedEnumCharacteristicReference(AbstractNamedReference reference,
            CharacteristicType laticeCharacteristicType, Literal literal) {

        // PCMDataCharacteristicsCalculator expects NamedEnumCharacteristicReferences
        NamedEnumCharacteristicReference variable = expressionFactory.createNamedEnumCharacteristicReference();

        variable.setCharacteristicType(laticeCharacteristicType);
        variable.setLiteral(literal);
        variable.setNamedReference(createCopiedReference(reference));

        return variable;
    }

    /*
     * VariableReferences need to be copied since they have a bidirectional connection to their container. E.g.: If a new
     * NamedEnumCharacteristicReference sets a NamedReference, this NamedReference is lost in the old
     * NamedEnumCharacteristicReference.
     */
    private static AbstractNamedReference createCopiedReference(AbstractNamedReference reference) {
        VariableReference copiedReference = stoexFactory.createVariableReference();
        copiedReference.setReferenceName(reference.getReferenceName());
        return copiedReference;
    }

    /*
     * Copy Term
     */
    private static Term copyTerm(Term term) {
        if (term instanceof True) {
            return expressionsFactory.createTrue();
        } else if (term instanceof False) {
            return expressionsFactory.createFalse();
        } else if (term instanceof NamedEnumCharacteristicReference namedReference) {
            return createNamedEnumCharacteristicReference(namedReference.getNamedReference(), namedReference.getCharacteristicType(),
                    namedReference.getLiteral());
        } else if (term instanceof And andTerm) {
            And andCopied = expressionsFactory.createAnd();
            andCopied.setLeft(copyTerm(andTerm.getLeft()));
            andCopied.setRight(copyTerm(andTerm.getRight()));
            return andCopied;
        } else if (term instanceof Or orTerm) {
            Or orCopied = expressionsFactory.createOr();
            orCopied.setLeft(copyTerm(orTerm.getLeft()));
            orCopied.setRight(copyTerm(orTerm.getRight()));
            return orCopied;
        } else if (term instanceof Not notTerm) {
            Not notCopied = expressionsFactory.createNot();
            notCopied.setTerm(copyTerm(notTerm.getTerm()));
            return notCopied;
        } else {
            String errorMessage = "Tried to copy unknown term element: " + term;
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private static List<NamedEnumCharacteristicReference> extractAllVariables(Term term) {
        if (term instanceof True) {
            return new ArrayList<>();
        } else if (term instanceof False) {
            return new ArrayList<>();
        } else if (term instanceof NamedEnumCharacteristicReference namedReference) {
            List<NamedEnumCharacteristicReference> variableList = new ArrayList<>();
            variableList.add(namedReference);
            return variableList;
        } else if (term instanceof And andTerm) {
            var variables = extractAllVariables(andTerm.getLeft());
            variables.addAll(extractAllVariables(andTerm.getRight()));
            return variables;
        } else if (term instanceof Or orTerm) {
            var variables = extractAllVariables(orTerm.getLeft());
            variables.addAll(extractAllVariables(orTerm.getRight()));
            return variables;
        } else if (term instanceof Not notTerm) {
            return extractAllVariables(notTerm.getTerm());
        } else {
            String errorMessage = "Tried to copy unknown term element: " + term;
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private static LhsEnumCharacteristicReference getLhsEnumCharacteristicReference(ConfidentialityVariableCharacterisation characterisation) {
        return (LhsEnumCharacteristicReference) characterisation.getLhs();
    }

    private static ConfidentialityVariableCharacterisation createCharacterisation(AbstractNamedReference characterisedVariable,
            CharacteristicType characteristicType, Literal literal, Term rhs) {
        var characterisation = confidentialityFactory.createConfidentialityVariableCharacterisation();
        characterisation.setLhs(createLhs(characteristicType, literal));
        characterisation.setRhs(rhs);
        characterisation.setVariableUsage_VariableCharacterisation(createVariableUsage(characterisedVariable));
        return characterisation;
    }

}
