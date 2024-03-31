package org.dataflowanalysis.analysis.pcm.informationflow.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.dataflowanalysis.pcm.extension.model.confidentiality.expression.LhsEnumCharacteristicReference;

/**
 * An {@link IFPCMExtractionStrategy} which prefers defined
 * {@link ConfidentialityVariableCharacterisation} to calculated
 * characterizations.
 *
 */
public abstract class IFPCMExtractionStrategyPrefer extends IFPCMExtractionStrategy {

	public IFPCMExtractionStrategyPrefer(ResourceProvider resourceProvider) {
		super(resourceProvider);
	}

	// TODO check if only max one defined CVC for each Latticelevel?

	@Override
	protected List<ConfidentialityVariableCharacterisation> calculateResultingConfidentialityVaraibleCharacterisations(
			List<ConfidentialityVariableCharacterisation> calculatedCharacterisations,
			List<ConfidentialityVariableCharacterisation> definedCharacterisations,
			Optional<DataFlowVariable> optionalSecurityContext) {

		List<ConfidentialityVariableCharacterisation> definedLatticeChars = new ArrayList<>();
		List<ConfidentialityVariableCharacterisation> definedNonLatticeChars = new ArrayList<>();
		for (var definedChar : definedCharacterisations) {
			LhsEnumCharacteristicReference lhsDefinedChar = (LhsEnumCharacteristicReference) definedChar.getLhs();

			Literal definedLiteral = lhsDefinedChar.getLiteral();

			if (definedLiteral == null) {
				List<EnumCharacteristicType> nonLatticCharacteristicTypes = IFPCMDataDictionaryUtils
						.getAllEnumCharacteristicTypesExceptLattice(getResourceProvider());
				EnumCharacteristicType latticeCharacteristicType = IFPCMDataDictionaryUtils
						.getLatticeCharacteristicType(getResourceProvider());

				var resolvedNonLatticeConfChars = IFConfidentialityVariableCharacterisationUtils
						.resolveCharacteristicTypeWildcard(definedChar, nonLatticCharacteristicTypes);
				definedNonLatticeChars.addAll(resolvedNonLatticeConfChars);
				var resolvedLatticeChar = IFConfidentialityVariableCharacterisationUtils.resolveWildcards(definedChar,
						List.of(latticeCharacteristicType));
				definedLatticeChars.addAll(resolvedLatticeChar);
			} else if (lhsDefinedChar.getLiteral().getEnum().equals(getLattice())) {
				definedLatticeChars.add(definedChar);
			} else {
				definedNonLatticeChars.add(definedChar);
			}
		}

		List<ConfidentialityVariableCharacterisation> resultingCharacterisations = new ArrayList<>(
				definedNonLatticeChars);

		if (definedLatticeChars.isEmpty()) {
			resultingCharacterisations.addAll(calculatedCharacterisations);
		} else if (optionalSecurityContext.isEmpty()) {
			resultingCharacterisations.addAll(definedCharacterisations);
		} else {
			resultingCharacterisations.addAll(
					modifyResultingCvcsWithSecurityContext(definedCharacterisations, optionalSecurityContext.get()));
		}

		return resultingCharacterisations;
	}

	/**
	 * Modifies the defined {@link ConfidentialityVariableCharacterisation}s in case
	 * of implicit flows.
	 * 
	 * @param definedCharacterisations the defined characterizations
	 * @param securityContext          the security context for the implicit flow
	 * @return the resulting characterizations
	 */
	protected abstract List<ConfidentialityVariableCharacterisation> modifyResultingCvcsWithSecurityContext(
			List<ConfidentialityVariableCharacterisation> definedCharacterisations, DataFlowVariable securityContext);

}
