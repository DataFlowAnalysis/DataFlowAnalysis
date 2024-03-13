package org.dataflowanalysis.analysis.pcm.informationflow.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.CharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Enumeration;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.ExpressionsFactory;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.Not;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.Or;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.Term;
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

	private IFConfidentialityVariableCharacterisationUtils() {
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

		// Assumes the ids of the lattice are ordered from lowest to highest.
		List<Literal> higherLevels = lattice.getLiterals().stream().filter(l -> l.getId().compareTo(level.getId()) > 0)
				.toList();

		List<Term> dependencies = new ArrayList<>();
		for (AbstractNamedReference reference : references) {
			dependencies.add(createNamedEnumCharacteristicReference(reference, latticeCharacteristicType, level));
			for (Literal latticeLevel : higherLevels) {
				dependencies.add(createNot(
						createNamedEnumCharacteristicReference(reference, latticeCharacteristicType, latticeLevel)));
			}
		}

		ConfidentialityFactory confFactory = ConfidentialityFactory.eINSTANCE;
		var confVar = confFactory.createConfidentialityVariableCharacterisation();

		confVar.setLhs(createLhs(latticeCharacteristicType, level));
		confVar.setRhs(createOrRhs(dependencies));
		confVar.setVariableUsage_VariableCharacterisation(createVariableUsage(characterisedVariable));
		return confVar;
	}

	private static VariableCharacterizationLhs createLhs(CharacteristicType latticeCharacteristicType,
			Literal literal) {
		// DataCharacteristicCalculator assumes lhs is a LhsEnumCharacteristicReference
		ExpressionFactory expFac = ExpressionFactory.eINSTANCE;
		LhsEnumCharacteristicReference lhs = expFac.createLhsEnumCharacteristicReference();

		lhs.setCharacteristicType(latticeCharacteristicType);
		lhs.setLiteral(literal);
		return lhs;
	}

	private static Term createOrRhs(List<Term> variables) {
		if (variables.size() < 1) {
			String errorMsg = "The creation of a ConfidentialityVariableCharacterisation without Variable dependencies is undefined.";
			logger.error(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		}

		ExpressionsFactory expsFac = ExpressionsFactory.eINSTANCE;

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
		ParameterFactory paramFac = ParameterFactory.eINSTANCE;
		VariableUsage variableUsage = paramFac.createVariableUsage();

		variableUsage.setNamedReference__VariableUsage(createCopiedReference(characterisedVariable));
		return variableUsage;
	}

	private static Not createNot(Term term) {
		ExpressionsFactory expsFac = ExpressionsFactory.eINSTANCE;
		Not negatedTerm = expsFac.createNot();
		negatedTerm.setTerm(term);
		return negatedTerm;
	}

	private static NamedEnumCharacteristicReference createNamedEnumCharacteristicReference(
			AbstractNamedReference reference, CharacteristicType laticeCharacteristicType, Literal literal) {

		// PCMDataCharacteristicsCalculator expect NamedEnumCharacteristicReferences
		ExpressionFactory expFac = ExpressionFactory.eINSTANCE;
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
		StoexFactory stoexFac = StoexFactory.eINSTANCE;
		VariableReference copiedReference = stoexFac.createVariableReference();
		copiedReference.setReferenceName(reference.getReferenceName());
		return copiedReference;
	}

}
