package org.dataflowanalysis.analysis.pcm.informationflow.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.dataflowanalysis.pcm.extension.model.confidentiality.expression.LhsEnumCharacteristicReference;

/**
 * An {@link IFPCMExtractionStrategy} which prefers defined
 * {@link ConfidentialityVariableCharacterisation} to calculated
 * characterizations. In case of implicit flows, defined characterizations are
 * not modified to consider implicit flow.
 *
 */
public class IFPCMExtractionStrategyPrefer extends IFPCMExtractionStrategy {

	public IFPCMExtractionStrategyPrefer(ResourceProvider resourceProvider) {
		super(resourceProvider);
	}

	// TODO Additional possible handling with modification to definedChars in case
	// of implicit flow?
	@Override
	protected List<ConfidentialityVariableCharacterisation> calculateResultingConfidentialityVaraibleCharacterisations(
			List<ConfidentialityVariableCharacterisation> calculatedCharacterisations,
			List<ConfidentialityVariableCharacterisation> definedCharacterisations,
			Optional<DataFlowVariable> optionalSecurityContext) {

		List<ConfidentialityVariableCharacterisation> resultingCharacterisations = new ArrayList<>(
				definedCharacterisations);
		for (ConfidentialityVariableCharacterisation calculatedCharacterisation : calculatedCharacterisations) {
			LhsEnumCharacteristicReference lhsCalcChar = (LhsEnumCharacteristicReference) calculatedCharacterisation
					.getLhs();

			boolean isDefined = false;
			for (ConfidentialityVariableCharacterisation definedCharacterisation : definedCharacterisations) {
				LhsEnumCharacteristicReference lhsDefChar = (LhsEnumCharacteristicReference) definedCharacterisation
						.getLhs();

				if (isMatchingDefinition(lhsCalcChar, lhsDefChar)) {
					isDefined = true;
				}
			}
			if (!isDefined) {
				resultingCharacterisations.add(calculatedCharacterisation);
			}
		}

		return resultingCharacterisations;
	}

	private boolean isMatchingDefinition(LhsEnumCharacteristicReference lhsCalcChar,
			LhsEnumCharacteristicReference lhsDefChar) {

		boolean defCtWildcard = lhsDefChar.getCharacteristicType() == null;
		boolean matchingCharacteristicType = defCtWildcard
				|| lhsCalcChar.getCharacteristicType().getName().equals(lhsDefChar.getCharacteristicType().getName());

		boolean defLiteralWildcard = lhsDefChar.getLiteral() == null;
		boolean matchingLiteral = defLiteralWildcard
				|| lhsCalcChar.getLiteral().getName().equals(lhsDefChar.getLiteral().getName());

		return matchingCharacteristicType && matchingLiteral;
	}

}