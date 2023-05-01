package org.palladiosimulator.dataflow.confidentiality.analysis.builder;

import java.nio.file.Paths;
import java.util.Optional;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.common.util.URI;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.node.LegacyPCMNodeCharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.node.PCMNodeCharacteristicsCalculatorImpl;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.variable.PCMDataCharacteristicsCalculatorFactory;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.PCMURIResourceLoader;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.ResourceLoader;

public class AnalysisBuilderData {
	protected boolean standalone;
	protected String modelProjectName;
	
	
	/**
	 * Sets the model porject name
	 * @param modelProjectName Project name of the modelling project
	 */
	public void setModelProjectName(String modelProjectName) {
		this.modelProjectName = modelProjectName;
	}
	
	/**
	 * Returns the configured name of the modelling project
	 * @return Saved name of the modelling project
	 */
	public String getModelProjectName() {
		return modelProjectName;
	}
	
	/**
	 * Sets the standalone mode of the analysis 
	 * @param standalone New configured mode of the analysis
	 */
	public void setStandalone(boolean standalone) {
		this.standalone = standalone;
	}
	
	/**
	 * Returns, whether the analysis should run in standalon mode or not
	 * @return Returns true, if the analysis is in standalone mode. Otherwise, the method returns false
	 */
	public boolean isStandalone() {
		return standalone;
	}
}
