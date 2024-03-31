package org.dataflowanalysis.analysis.pcm.informationflow.core;

import java.util.List;

import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;

/**
 * An {@link IFPCMExtractionStrategyPrefer} which modifies defined
 * {@link ConfidentialityVariableCharacterisation}s to consider implicit flows.
 * As specified in {@link IFPCMExtractionStrategyPrefer}, defined
 * {@link ConfidentialityVariableCharacterisation} are preferred to calculated
 * characterizations.
 *
 */
public class IFPCMExtractionStrategyPreferConsider extends IFPCMExtractionStrategyPrefer {

	public IFPCMExtractionStrategyPreferConsider(ResourceProvider resourceProvider) {
		super(resourceProvider);
	}

	@Override
	protected List<ConfidentialityVariableCharacterisation> modifyResultingCvcsWithSecurityContext(
			List<ConfidentialityVariableCharacterisation> definedCharacterisations, DataFlowVariable securityContext) {

		var constraintRef = IFStoexUtils.createReferenceFromName(securityContext.getVariableName());
		return IFConfidentialityVariableCharacterisationUtils
				.createModifiedCharacterisationsForAdditionalHigherEqualConstraint(definedCharacterisations,
						constraintRef, getLatticeCharacteristicType(), getLattice());
	}

}
