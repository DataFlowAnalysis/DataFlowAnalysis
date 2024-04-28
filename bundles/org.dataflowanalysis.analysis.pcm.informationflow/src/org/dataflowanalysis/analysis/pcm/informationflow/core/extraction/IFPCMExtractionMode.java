package org.dataflowanalysis.analysis.pcm.informationflow.core.extraction;

import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;

/**
 * Defines an extraction mode for {@link ConfidentialityVariableCharacterisation}s.
 */
public enum IFPCMExtractionMode {
    /**
     * {@link ConfidentialityVariableCharacterisation}s are preferred over calculated characterizations. Further, implicit
     * flow is considered even for defined {@link ConfidentialityVariableCharacterisation}s.
     */
    PreferConsider {

        @Override
        public IFPCMExtractionStrategy createExtractionStrategy(ResourceProvider resourceProvider) {
            return new IFPCMExtractionStrategyPreferConsider(resourceProvider);
        }

    },
    /**
     * {@link ConfidentialityVariableCharacterisation}s are preferred over calculated characterizations. Further, defined
     * {@link ConfidentialityVariableCharacterisation}s are not modified to consider implicit flow.
     */
    PreferUnmodified {

        @Override
        public IFPCMExtractionStrategy createExtractionStrategy(ResourceProvider resourceProvider) {
            return new IFPCMExtractionStrategyPreferUnmodified(resourceProvider);
        }

    };

    /**
     * Creates a corresponding {@link IFPCMExtractionStrategy} with the given ResourceProvider
     * @param resourceProvider the ResourceProvider
     * @return the corresponding {@code IFPCMExtractionStrategy}
     */
    public abstract IFPCMExtractionStrategy createExtractionStrategy(ResourceProvider resourceProvider);

}
