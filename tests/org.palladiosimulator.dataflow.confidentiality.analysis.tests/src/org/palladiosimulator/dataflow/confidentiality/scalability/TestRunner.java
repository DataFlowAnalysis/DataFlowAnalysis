package org.palladiosimulator.dataflow.confidentiality.scalability;

import java.util.List;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityParameter;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityTest;

public class TestRunner {
	private final Logger logger = Logger.getLogger(TestRunner.class);
	
	private List<ScalibilityTest> tests;
	
	public TestRunner(List<ScalibilityTest> tests) {
		this.tests = tests;
	}
	
	public void runTests() {
		tests.forEach(this::runTest);
	}
	
	private void runTest(ScalibilityTest test) {
		for(int i = 0; true; i++) {
			int modelSize = test.getModelSize(i);
			logger.info("Running test with model size " + modelSize);
			ScalibilityParameter parameter = new ScalibilityParameter(modelSize);
			test.run(parameter);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
