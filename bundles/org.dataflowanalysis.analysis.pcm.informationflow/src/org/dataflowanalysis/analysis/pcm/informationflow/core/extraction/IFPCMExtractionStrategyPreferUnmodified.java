package org.dataflowanalysis.analysis.pcm.informationflow.core.extraction;

import java.util.List;

import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;

/**
 * An {@link IFPCMExtractionStrategyPrefer} which does not modify defined
 * {@link ConfidentialityVariableCharacterisation}s to consider implicit flows. As specified in
 * {@link IFPCMExtractionStrategyPrefer}, defined {@link ConfidentialityVariableCharacterisation} are preferred to
 * calculated characterizations.
 */
public class IFPCMExtractionStrategyPreferUnmodified extends IFPCMExtractionStrategyPrefer {

    public IFPCMExtractionStrategyPreferUnmodified(ResourceProvider resourceProvider) {
        super(resourceProvider);
    }

    @Override
    protected List<ConfidentialityVariableCharacterisation> modifyResultingConfidentialityCharacterisationsWithSecurityContext(
            List<ConfidentialityVariableCharacterisation> definedCharacterisations, DataFlowVariable securityContext) {
        return definedCharacterisations;
    }

}
