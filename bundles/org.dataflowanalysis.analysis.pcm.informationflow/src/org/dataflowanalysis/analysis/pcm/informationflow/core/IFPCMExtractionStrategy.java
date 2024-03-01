package org.dataflowanalysis.analysis.pcm.informationflow.core;

import java.util.List;

import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;

public abstract class IFPCMExtractionStrategy {

	public abstract List<ConfidentialityVariableCharacterisation> calculateEffectiveConfidentialityVariableCharacterisation(
			List<ConfidentialityVariableCharacterisation> confidentialityCharacteristaions,
			List<VariableCharacterisation> normalCharacterisations);

}
