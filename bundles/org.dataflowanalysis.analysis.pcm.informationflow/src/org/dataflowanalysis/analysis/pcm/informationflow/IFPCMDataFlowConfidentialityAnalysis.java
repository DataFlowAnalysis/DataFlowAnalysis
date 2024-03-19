package org.dataflowanalysis.analysis.pcm.informationflow;

import java.util.Optional;

import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.pcm.core.PCMFlowGraph;
import org.dataflowanalysis.analysis.pcm.core.PCMPartialFlowGraphFinder;
import org.dataflowanalysis.analysis.pcm.core.finder.PCMUserFinder;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFPCMExtractionStrategy;
import org.dataflowanalysis.analysis.pcm.informationflow.core.finder.IFSEFFPCMVertextFactory;
import org.dataflowanalysis.analysis.pcm.informationflow.core.finder.IFUserPCMVertexFactory;
import org.eclipse.core.runtime.Plugin;

/**
 * A confidentiality analysis generated from PCM elements. In addition to the
 * {@link PCMDataFlowConfidentialityAnalysis}, this analysis also allows the
 * partial extraction of VariableConfidentialityCharacterisations from normal
 * VariableCharacteristations. Furthermore, implicit flows can be considered.
 *
 */
public class IFPCMDataFlowConfidentialityAnalysis extends PCMDataFlowConfidentialityAnalysis {

	private boolean considerImplicitFlows;
	private IFPCMExtractionStrategy extractionStrategy;

	public static final String PLUGIN_PATH = "org.dataflowanalysis.analysis.pcm.informationflow";

	/**
	 * Creates an {@link IFPCMDataFlowConfidentialityAnalysis} with the given
	 * parameters. Note, this is an proxy to a
	 * {@link PCMDataFlowConfidentialityAnalysis} apart from the graph generation.
	 * 
	 * @param analysis              the proxied PCMDataFlowconfidentialityAnalysis
	 * @param modelProjectName      the name of the modeled project
	 * @param modelProjectActivator the plugin class of the analysis
	 * @param considerImplicitFlows true, if the analysis should consider implicit
	 *                              flows. False, otherwise.
	 * @param extractionStrategy    the extraction strategy of the analysis
	 */
	public IFPCMDataFlowConfidentialityAnalysis(PCMDataFlowConfidentialityAnalysis analysis, String modelProjectName,
			Optional<Class<? extends Plugin>> modelProjectActivator, boolean considerImplicitFlows,
			IFPCMExtractionStrategy extractionStrategy) {

		super(analysis.getResourceProvider(), modelProjectName, modelProjectActivator);
		this.considerImplicitFlows = considerImplicitFlows;
		this.extractionStrategy = extractionStrategy;
	}

	@Override
	public PCMFlowGraph findFlowGraph() {

		var userElementFactory = new IFUserPCMVertexFactory(considerImplicitFlows, extractionStrategy);
		var seffElementFactory = new IFSEFFPCMVertextFactory(considerImplicitFlows, extractionStrategy);
		var userFinder = new PCMUserFinder(userElementFactory, seffElementFactory);
		var sequenceFinder = new PCMPartialFlowGraphFinder(resourceProvider, userFinder);

		return new PCMFlowGraph(resourceProvider, sequenceFinder);
	}
}
