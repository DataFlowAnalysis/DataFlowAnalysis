package org.dataflowanalysis.analysis.dfd;

import java.util.List;
import java.util.Optional;

import java.util.ArrayList;
import java.util.function.Predicate;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.core.AbstractActionSequenceElement;
import org.dataflowanalysis.analysis.core.ActionSequence;
import org.dataflowanalysis.analysis.dfd.core.DFDActionSequence;
import org.dataflowanalysis.analysis.dfd.core.DFDActionSequenceFinder;
import org.dataflowanalysis.analysis.dfd.core.DFDCharacteristicsCalculator;
import org.dataflowanalysis.analysis.dfd.resource.DFDResourceProvider;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;

public class DFDConfidentialityAnalysis implements DataFlowConfidentialityAnalysis {
	private final Logger logger = Logger.getLogger(DFDConfidentialityAnalysis.class);
	
	private DFDResourceProvider resourceProvider;
	private Optional<Class<? extends Plugin>> modelProjectActivator;
	private String modelProjectName;
	
	public DFDConfidentialityAnalysis(DFDResourceProvider resourceProvider, Optional<Class<? extends Plugin>> modelProjectActivator, String modelProjectName) {
		this.resourceProvider = resourceProvider;
		this.modelProjectActivator = modelProjectActivator;
		this.modelProjectName = modelProjectName;
	}

	@Override
	public boolean initializeAnalysis() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("dataflowdiagram", new XMIResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("datadictionary", new XMIResourceFactoryImpl());

        EcorePlugin.ExtensionProcessor.process(null);
		
        try {
        	var initializationBuilder = StandaloneInitializerBuilder.builder()
                    .registerProjectURI(DFDConfidentialityAnalysis.class, 
                    		DFDConfidentialityAnalysis.PLUGIN_PATH);
                 
                 if (this.modelProjectActivator.isPresent()) {
                	 initializationBuilder.registerProjectURI(this.modelProjectActivator.get(), this.modelProjectName);
                 }
                 
                 initializationBuilder.build()
                    .init();

                logger.info("Successfully initialized standalone environment for the data flow analysis.");

        } catch (StandaloneInitializationException e) {
        	logger.error("Could not initialize analysis", e);
        	throw new IllegalStateException("Could not initialize analysis");
        }
        this.resourceProvider.loadRequiredResources();
        if(!this.resourceProvider.sufficientResourcesLoaded()) {
        	logger.error("Insufficient amount of resources loaded");
        	throw new IllegalStateException("Could not initialize analysis");
        }
		return true;
	}
	

	@Override
	public List<ActionSequence> findAllSequences() {
		return DFDActionSequenceFinder.findAllSequencesInDFD(this.resourceProvider.getDataFlowDiagram(), this.resourceProvider.getDataDictionary());
	}
	

	@Override
	public List<ActionSequence> evaluateDataFlows(List<ActionSequence> sequences) {
		List<ActionSequence> outSequences = new ArrayList<>();
		for (var dfdActionSequence : sequences) {
			outSequences.add(DFDCharacteristicsCalculator.fillDataFlowVariables((DFDActionSequence)dfdActionSequence));
		}
		return outSequences;
	}

	@Override
	public List<AbstractActionSequenceElement<?>> queryDataFlow(ActionSequence sequence,
			Predicate<? super AbstractActionSequenceElement<?>> condition) {
		return sequence.getElements()
	            .parallelStream()
	            .filter(condition)
	            .toList();
	}

	@Override
	public void setLoggerLevel(Level level) {
		logger.setLevel(level);
	}
}