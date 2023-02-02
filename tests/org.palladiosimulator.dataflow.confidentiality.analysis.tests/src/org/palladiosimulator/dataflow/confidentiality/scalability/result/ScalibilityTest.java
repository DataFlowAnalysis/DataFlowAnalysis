package org.palladiosimulator.dataflow.confidentiality.scalability.result;

public interface ScalibilityTest {
	
	void run(ScalibilityParameter parameter);
	
	int getModelSize(int currentIndex);
}