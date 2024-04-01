package org.dataflowanalysis.analysis.pcm.informationflow.core;

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
 *
 */
public class IFConfidentialityVariableCharacterisationUtils {

	private static final Logger logger = Logger.getLogger(IFConfidentialityVariableCharacterisationUtils.class);

	private final static StoexFactory stoexFac = StoexFactory.eINSTANCE;
	private final static ConfidentialityFactory confFac = ConfidentialityFactory.eINSTANCE;
	private final static ExpressionsFactory expsFac = ExpressionsFactory.eINSTANCE;
	private final static ExpressionFactory expFac = ExpressionFactory.eINSTANCE;
	private final static ParameterFactory paramFac = ParameterFactory.eINSTANCE;

	private IFConfidentialityVariableCharacterisationUtils() {
	}

	// TODO change usedReferences to Strings?

	/**
	 * Resolves wildcards for the {@link CharacteristicType} and {@link Literal} in
	 * a {@link ConfidentialityVariableCharacterisation}. The resolving is achieved
	 * by creating a {@link ConfidentialityVariableCharacterisation} for each given
	 * {@link EnumCharacteristicType} and its corresponding {@link Literal}s.
	 * 
	 * @param characterisation       the given characterization
	 * @param allCharacteristicTypes the given characteristic types
	 * @return the created characterizations
	 */
	public static List<ConfidentialityVariableCharacterisation> resolveWildcards(
			ConfidentialityVariableCharacterisation characterisation,
			List<EnumCharacteristicType> allCharacteristicTypes) {
		return resolveCharacteristicTypeWildcard(characterisation, allCharacteristicTypes).stream()
				.flatMap(confChar -> resolveLiteralWildcard(confChar).stream()).toList();
	}

	/**
	 * Resolves wildcards for the {@link Literal} in a
	 * {@link ConfidentialityVariableCharacterisation} by creating a
	 * {@link ConfidentialityVariableCharacterisation} for each {@link Literal} in
	 * the corresponding {@link Enumeration}. Assumes the {@link CharacteristicType}
	 * of the {@link ConfidentialityVariableCharacterisation} to be present and an
	 * instance of {@link EnumCharacteristicType}.
	 * 
	 * @param characterisation the given characterization
	 * @return the created characterizations
	 */
	public static List<ConfidentialityVariableCharacterisation> resolveLiteralWildcard(
			ConfidentialityVariableCharacterisation characterisation) {

		var enumCharacteristicType = (EnumCharacteristicType) getLhsEnumCharacteristicReference(characterisation)
				.getCharacteristicType();
		List<ConfidentialityVariableCharacterisation> resolvedCharacterisations = new ArrayList<>();
		for (Literal level : enumCharacteristicType.getType().getLiterals()) {
			var resolvedConfChar = copyConfidentialityVariableCharacterisation(characterisation);

			var lhs = getLhsEnumCharacteristicReference(resolvedConfChar);
			lhs.setLiteral(level);

			List<NamedEnumCharacteristicReference> varsInRhs = extractAllVariables(resolvedConfChar.getRhs());
			varsInRhs.stream().forEach(variable -> variable.setLiteral(level));

			resolvedCharacterisations.add(resolvedConfChar);
		}
		return resolvedCharacterisations;
	}

	/**
	 * Resolves wildcards for the {@link CharacteristicType} in a
	 * {@link ConfidentialityVariableCharacterisation} by creating a
	 * {@link ConfidentialityVariableCharacterisation} for each given
	 * {@link EnumCharacteristicType}.
	 * 
	 * @param characterisation       the given characterization
	 * @param allCharacteristicTypes the given characteristic types
	 * @return the created characterizations
	 */
	public static List<ConfidentialityVariableCharacterisation> resolveCharacteristicTypeWildcard(
			ConfidentialityVariableCharacterisation characterisation,
			List<EnumCharacteristicType> allCharacteristicTypes) {

		List<ConfidentialityVariableCharacterisation> resolvedCharacterisations = new ArrayList<>();
		for (var characteristicType : allCharacteristicTypes) {
			var resolvedConfChar = copyConfidentialityVariableCharacterisation(characterisation);

			var lhs = getLhsEnumCharacteristicReference(resolvedConfChar);
			lhs.setCharacteristicType(characteristicType);

			List<NamedEnumCharacteristicReference> varsInRhs = extractAllVariables(resolvedConfChar.getRhs());
			varsInRhs.stream().forEach(variable -> variable.setCharacteristicType(characteristicType));

			resolvedCharacterisations.add(resolvedConfChar);
		}
		return resolvedCharacterisations;
	}

	private static ConfidentialityVariableCharacterisation copyConfidentialityVariableCharacterisation(
			ConfidentialityVariableCharacterisation confChar) {

		var copiedConfChar = confFac.createConfidentialityVariableCharacterisation();
		var lhs = getLhsEnumCharacteristicReference(confChar);

		copiedConfChar.setLhs(createLhs(lhs.getCharacteristicType(), lhs.getLiteral()));
		copiedConfChar.setRhs(copyTerm(confChar.getRhs()));
		copiedConfChar.setVariableUsage_VariableCharacterisation(createVariableUsage(
				confChar.getVariableUsage_VariableCharacterisation().getNamedReference__VariableUsage()));
		return copiedConfChar;
	}

	/**
	 * Creates a {@link ConfidentialityVariableCharacterisation} for each level of
	 * the given lattice for the given latticeCharacteristicType. The behavior of
	 * the resulting {@link ConfidentialityVariableCharacterisation} is to set the
	 * maximum Label of the given characterizations and the constraint. Assumes that
	 * the given characterizations only set one level. Assumes that the given
	 * characterizations do not have any wildcards. Assumes that only one
	 * specification is present for each level - not more.
	 * 
	 * @param confChars                 the given characterizations
	 * @param constraint                the given constraint
	 * @param latticeCharacteristicType the given CharacteristicType
	 * @param lattice                   the given lattice
	 * @return the resulting {@link ConfidentialityVariableCharacterisation} for the
	 *         lattice
	 */
	public static List<ConfidentialityVariableCharacterisation> createModifiedCharacterisationsForAdditionalHigherEqualConstraint(
			List<ConfidentialityVariableCharacterisation> confChars, AbstractNamedReference constraint,
			CharacteristicType latticeCharacteristicType, Enumeration lattice) {
		Map<Literal, ConfidentialityVariableCharacterisation> literalToDefinedConfCar = new HashMap<>();
		for (var confChar : confChars) {
			LhsEnumCharacteristicReference lhsConfChar = (LhsEnumCharacteristicReference) confChar.getLhs();
			literalToDefinedConfCar.put(lhsConfChar.getLiteral(), confChar);
		}

		List<ConfidentialityVariableCharacterisation> modifiedCharacterisations = new ArrayList<>();
		for (Literal latticeLevel : lattice.getLiterals()) {
			modifiedCharacterisations.add(createModifiedCvcForLevel(literalToDefinedConfCar, constraint, latticeLevel,
					latticeCharacteristicType, lattice));
		}

		return modifiedCharacterisations;
	}

	// TODO handling if a level has no specified confChar.
	private static ConfidentialityVariableCharacterisation createModifiedCvcForLevel(
			Map<Literal, ConfidentialityVariableCharacterisation> literalToDefinedConfChar,
			AbstractNamedReference constraint, Literal level, CharacteristicType latticeCharacteristicType,
			Enumeration lattice) {

		var definedLevelConfChar = literalToDefinedConfChar.get(level);
		var characterisedVariable = definedLevelConfChar.getVariableUsage_VariableCharacterisation()
				.getNamedReference__VariableUsage();

		List<Term> lowerDefinedTerms = new ArrayList<>();
		for (Literal latticeLevel : lattice.getLiterals()) {
			if (IFLatticeUtils.isLowerLevel(latticeLevel, level)) {
				lowerDefinedTerms.add(copyTerm(literalToDefinedConfChar.get(latticeLevel).getRhs()));
			}
		}

		Term levelTerm = copyTerm(literalToDefinedConfChar.get(level).getRhs());

		Term levelConstraintVariable = createNamedEnumCharacteristicReference(constraint, latticeCharacteristicType,
				level);

		List<Term> higherConstraintVariables = new ArrayList<>();
		for (Literal latticeLevel : lattice.getLiterals()) {
			if (IFLatticeUtils.isHigherLevel(latticeLevel, level)) {
				higherConstraintVariables.add(
						createNamedEnumCharacteristicReference(constraint, latticeCharacteristicType, latticeLevel));
			}
		}

		var confChar = confFac.createConfidentialityVariableCharacterisation();

		confChar.setLhs(createLhs(latticeCharacteristicType, level));
		confChar.setRhs(createRhsForAdditionalHigherEqualConstraint(lowerDefinedTerms, levelTerm,
				levelConstraintVariable, higherConstraintVariables));
		confChar.setVariableUsage_VariableCharacterisation(createVariableUsage(characterisedVariable));

		return confChar;
	}

	private static Term createRhsForAdditionalHigherEqualConstraint(List<Term> lowerDefinedTerms, Term levelTerm,
			Term levelConstraintVariable, List<Term> higherConstraintVariables) {

		Term levelShouldBeSet = levelTerm;
		if (!higherConstraintVariables.isEmpty()) {
			Term notHigherConstraintsORed = createNot(createOrTerm(higherConstraintVariables));
			And isApplyableAndConfirmsSecurityContext = expsFac.createAnd();
			isApplyableAndConfirmsSecurityContext.setLeft(levelTerm);
			isApplyableAndConfirmsSecurityContext.setRight(notHigherConstraintsORed);
			levelShouldBeSet = isApplyableAndConfirmsSecurityContext;
		}

		if (!lowerDefinedTerms.isEmpty()) {
			Term lowerDefinedTermsORed = createOrTerm(lowerDefinedTerms);
			And oneLowerTermTrueAndIsExactlyConstraint = expsFac.createAnd();
			oneLowerTermTrueAndIsExactlyConstraint.setLeft(lowerDefinedTermsORed);
			oneLowerTermTrueAndIsExactlyConstraint.setRight(levelConstraintVariable);

			Or levelConfirmsWithConstraintOrConstraintWhenLowerTrue = expsFac.createOr();
			levelConfirmsWithConstraintOrConstraintWhenLowerTrue.setLeft(levelShouldBeSet);
			levelConfirmsWithConstraintOrConstraintWhenLowerTrue.setRight(oneLowerTermTrueAndIsExactlyConstraint);
			levelShouldBeSet = levelConfirmsWithConstraintOrConstraintWhenLowerTrue;
		}

		return levelShouldBeSet;
	}

	/**
	 * Creates a {@link ConfidentialityVariableCharacterisation} for each level of
	 * the given lattice for the given latticeCharacteristicType. The behavior of
	 * the resulting {@link ConfidentialityVariableCharacterisation} is to only set
	 * the maximum Label of the given references. There should always be at least on
	 * reference.
	 * 
	 * @param characterisedVariable     the Reference for the Variable to be
	 *                                  characterized
	 * @param references                the References for Variables which the
	 *                                  characterisedVariable depends upon. This
	 *                                  should be at least one Reference
	 * @param latticeCharacteristicType the used CharacterisationType
	 * @param lattice                   the lattice for which the
	 *                                  ConfidentialityVariableCharacterisations are
	 *                                  created
	 * @return the resulting ConfidentialityVariableCharacterisations for the
	 *         lattice
	 */
	public static List<ConfidentialityVariableCharacterisation> createMaximumJoinCharacterisationsForLattice(
			AbstractNamedReference characterisedVariable, List<AbstractNamedReference> references,
			CharacteristicType latticeCharacteristicType, Enumeration lattice) {

		return lattice.getLiterals().stream().map(it -> createMaximumJoinCharacterisationForLevel(characterisedVariable,
				references, latticeCharacteristicType, lattice, it)).toList();
	}

	private static ConfidentialityVariableCharacterisation createMaximumJoinCharacterisationForLevel(
			AbstractNamedReference characterisedVariable, List<AbstractNamedReference> references,
			CharacteristicType latticeCharacteristicType, Enumeration lattice, Literal level) {

		List<Literal> higherLevels = lattice.getLiterals().stream().filter(l -> IFLatticeUtils.isHigherLevel(l, level))
				.toList();

		List<Term> levelDependencies = new ArrayList<>();
		List<Term> higherDependencies = new ArrayList<>();
		for (AbstractNamedReference reference : references) {
			levelDependencies.add(createNamedEnumCharacteristicReference(reference, latticeCharacteristicType, level));
			for (Literal latticeLevel : higherLevels) {
				higherDependencies.add(
						createNamedEnumCharacteristicReference(reference, latticeCharacteristicType, latticeLevel));
			}
		}

		var confVar = confFac.createConfidentialityVariableCharacterisation();

		confVar.setLhs(createLhs(latticeCharacteristicType, level));
		confVar.setRhs(createRhs(levelDependencies, higherDependencies));
		confVar.setVariableUsage_VariableCharacterisation(createVariableUsage(characterisedVariable));
		return confVar;
	}

	private static VariableCharacterizationLhs createLhs(CharacteristicType latticeCharacteristicType,
			Literal literal) {
		// DataCharacteristicCalculator assumes lhs is a LhsEnumCharacteristicReference
		LhsEnumCharacteristicReference lhs = expFac.createLhsEnumCharacteristicReference();

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

		And andTerm = expsFac.createAnd();
		andTerm.setLeft(positiveTerm);
		andTerm.setRight(negatedTerm);
		return andTerm;
	}

	private static Term createOrTerm(List<Term> variables) {
		if (variables.size() < 1) {
			String errorMsg = "The creation of a ConfidentialityVariableCharacterisation without Variable dependencies is undefined.";
			logger.error(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		}

		Term term = variables.get(0);
		for (int i = 1; i < variables.size(); i++) {
			Or orTerm = expsFac.createOr();
			orTerm.setLeft(term);
			orTerm.setRight(variables.get(i));
			term = orTerm;
		}

		return term;
	}

	private static VariableUsage createVariableUsage(AbstractNamedReference characterisedVariable) {
		VariableUsage variableUsage = paramFac.createVariableUsage();

		variableUsage.setNamedReference__VariableUsage(createCopiedReference(characterisedVariable));
		return variableUsage;
	}

	private static Not createNot(Term term) {
		Not negatedTerm = expsFac.createNot();
		negatedTerm.setTerm(term);
		return negatedTerm;
	}

	private static NamedEnumCharacteristicReference createNamedEnumCharacteristicReference(
			AbstractNamedReference reference, CharacteristicType laticeCharacteristicType, Literal literal) {

		// PCMDataCharacteristicsCalculator expects NamedEnumCharacteristicReferences
		NamedEnumCharacteristicReference variable = expFac.createNamedEnumCharacteristicReference();

		variable.setCharacteristicType(laticeCharacteristicType);
		variable.setLiteral(literal);
		variable.setNamedReference(createCopiedReference(reference));

		return variable;
	}

	// TODO Maybe look into how EReferences in EMF work?
	// TODO Still might not be as intended.
	/*
	 * VariableReferences need to be copied since they have a bidirectional
	 * connection to their container. E.g.: If a new
	 * NamedEnumCharacteristicReference sets a NamedReference, this NamedReference
	 * is lost in the old NamedEnumCharacteristicReference.
	 */
	private static AbstractNamedReference createCopiedReference(AbstractNamedReference reference) {
		VariableReference copiedReference = stoexFac.createVariableReference();
		copiedReference.setReferenceName(reference.getReferenceName());
		return copiedReference;
	}

	/*
	 * Copy Term
	 */
	private static Term copyTerm(Term term) {
		if (term instanceof True) {
			return expsFac.createTrue();
		} else if (term instanceof False) {
			return expsFac.createFalse();
		} else if (term instanceof NamedEnumCharacteristicReference namedRef) {
			return createNamedEnumCharacteristicReference(namedRef.getNamedReference(),
					namedRef.getCharacteristicType(), namedRef.getLiteral());
		} else if (term instanceof And andTerm) {
			And andCopied = expsFac.createAnd();
			andCopied.setLeft(copyTerm(andTerm.getLeft()));
			andCopied.setRight(copyTerm(andTerm.getRight()));
			return andCopied;
		} else if (term instanceof Or orTerm) {
			Or orCopied = expsFac.createOr();
			orCopied.setLeft(copyTerm(orTerm.getLeft()));
			orCopied.setRight(copyTerm(orTerm.getRight()));
			return orCopied;
		} else if (term instanceof Not notTerm) {
			Not notCopied = expsFac.createNot();
			notCopied.setTerm(copyTerm(notTerm.getTerm()));
			return notCopied;
		} else {
			String errorMsg = "Tried to copy unknown term element: " + term;
			logger.error(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		}
	}

	private static List<NamedEnumCharacteristicReference> extractAllVariables(Term term) {
		if (term instanceof True) {
			return new ArrayList<>();
		} else if (term instanceof False) {
			return new ArrayList<>();
		} else if (term instanceof NamedEnumCharacteristicReference namedRef) {
			List<NamedEnumCharacteristicReference> variableList = new ArrayList<>();
			variableList.add(namedRef);
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
			String errorMsg = "Tried to copy unknown term element: " + term;
			logger.error(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		}
	}

	private static LhsEnumCharacteristicReference getLhsEnumCharacteristicReference(
			ConfidentialityVariableCharacterisation confChar) {
		return (LhsEnumCharacteristicReference) confChar.getLhs();
	}

}
