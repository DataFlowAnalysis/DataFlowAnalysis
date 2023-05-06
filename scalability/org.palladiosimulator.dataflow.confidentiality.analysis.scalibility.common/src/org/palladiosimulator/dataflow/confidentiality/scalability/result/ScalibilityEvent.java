package org.palladiosimulator.dataflow.confidentiality.scalability.result;

public enum ScalibilityEvent {
	ANALYSIS_INITIALZATION("AnalysisInitialization"), SEQUENCE_FINDING("SequenceFinding"), PROPAGATION("Propagation");
	
	private String name;
	
	ScalibilityEvent(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
