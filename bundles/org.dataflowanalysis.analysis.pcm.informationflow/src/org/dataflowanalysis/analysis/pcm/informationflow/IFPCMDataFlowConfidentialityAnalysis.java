package org.dataflowanalysis.analysis.pcm.informationflow;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.dataflowanalysis.analysis.core.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.FlowGraph;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.pcm.core.PCMFlowGraph;
import org.dataflowanalysis.analysis.pcm.core.PCMPartialFlowGraphFinder;
import org.dataflowanalysis.analysis.pcm.core.finder.PCMUserFinder;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFPCMExtractionStrategy;
import org.dataflowanalysis.analysis.pcm.informationflow.core.finder.IFSEFFPCMVertextFactory;
import org.dataflowanalysis.analysis.pcm.informationflow.core.finder.IFUserPCMVertexFactory;
import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.eclipse.core.runtime.Plugin;

/**
 * A confidentiality analysis generated from PCM elements. In addition to the
 * {@link PCMDataFlowConfidentialityAnalysis}, this analysis also allows the
 * partial extraction of VariableConfidentialityCharacterisations from normal
 * VariableCharacteristations. Furthermore, implicit flows can be considered.
 *
 */
public class IFPCMDataFlowConfidentialityAnalysis extends PCMDataFlowConfidentialityAnalysis {

	/*
	 * This class is a proxy to a PCMDataFlowConfidentialityAnalysis. Therefore,
	 * this class should use the methods of the given analysis instead of the
	 * inherited methods. Every method should be overwritten.
	 */
	private PCMDataFlowConfidentialityAnalysis analysis;
	private boolean considerImplicitFlows;
	private IFPCMExtractionStrategy extractionStrategy;

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
		/*
		 * Assumes the getResourceProvider()-method of
		 * PCMDataFlowConfidentialityAnalysis returns a PCMResourceProvider. This
		 * assumption is used in the PCMDataFlowConfidentialityAnalysis internally as
		 * well.
		 */
		super((PCMResourceProvider) analysis.getResourceProvider(), modelProjectName, modelProjectActivator);
	}

	@Override
	public void initializeAnalysis() {
		analysis.initializeAnalysis();
	}

	@Override
	public PCMFlowGraph findFlowGraph() {

		var userElementFactory = new IFUserPCMVertexFactory(considerImplicitFlows, extractionStrategy);
		var seffElementFactory = new IFSEFFPCMVertextFactory(considerImplicitFlows, extractionStrategy);
		var userFinder = new PCMUserFinder(userElementFactory, seffElementFactory);
		var sequenceFinder = new PCMPartialFlowGraphFinder(resourceProvider, userFinder);
		/*
		 * TODO Code duplication from PCMFlowGraph#findPartialFlowGraphs. Could not
		 * change the used PartialFlowGraphFinder without changes to the
		 * FlowGraph-Constructor: The Constructor either expects already calculated
		 * PartialFlowGraphs or calls the method findPartialFlowGraphs() directly. Since
		 * the super-Constructor has to be called first, I found no option to change the
		 * PCMPartialFlowGraphFinder (in a flexible way) without changing FlowGraph.
		 * (Flexible here means that the used Finder is not hardcoded in a subclass,
		 * meaning there is no option of configuration).
		 */
		List<AbstractPartialFlowGraph> partialFlowGraphs = sequenceFinder.findPartialFlowGraphs().parallelStream()
				.map(AbstractPartialFlowGraph.class::cast).collect(Collectors.toList());

		return new PCMFlowGraph(partialFlowGraphs, resourceProvider);
	}

	@Override
	public PCMFlowGraph evaluateFlowGraph(FlowGraph flowGraph) {
		return analysis.evaluateFlowGraph(flowGraph);
	}

	@Override
	public List<? extends AbstractVertex<?>> queryDataFlow(AbstractPartialFlowGraph partialFlowGraph,
			Predicate<? super AbstractVertex<?>> condition) {
		return analysis.queryDataFlow(partialFlowGraph, condition);
	}

	@Override
	public void setLoggerLevel(Level level) {
		analysis.setLoggerLevel(level);
	}

	@Override
	public ResourceProvider getResourceProvider() {
		return super.getResourceProvider();
	}
}
