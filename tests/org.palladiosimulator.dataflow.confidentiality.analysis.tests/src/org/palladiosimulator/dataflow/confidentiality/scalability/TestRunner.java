package org.palladiosimulator.dataflow.confidentiality.scalability;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityParameter;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityTest;

public class TestRunner {
	private static final int RUNS_PER_STAGE = 10;
	
	private final Logger logger = Logger.getLogger(TestRunner.class);
	
	private List<ScalibilityTest> tests;
	private List<ScalibilityParameter> results;
	
	public TestRunner(List<ScalibilityTest> tests) {
		this.tests = tests;
		this.results = new ArrayList<>();
	}
	
	public void runTests() {
		tests.forEach(it -> this.runTest(it, 0));
	}
	
	public void runTests(int start) {
		tests.forEach(it -> this.runTest(it, start));
	}
	
	private void runTest(ScalibilityTest test, int start) {
		for(int i = start; true; i++) {
			for (int j = 0; j < RUNS_PER_STAGE; j++) {
				int modelSize = test.getModelSize(i);
				logger.info("Running test with model size " + modelSize);
				ScalibilityParameter parameter = new ScalibilityParameter(modelSize, test.getTestName());
				test.run(parameter);
				this.results.add(parameter);
				saveResults();
			}
		}
	}
	
	private void saveResults() {
		try {
			FileOutputStream fileOutputStream
		      = new FileOutputStream("results.ser");
		    ObjectOutputStream objectOutputStream 
		      = new ObjectOutputStream(fileOutputStream);
		    objectOutputStream.writeObject(this.results);
		    objectOutputStream.flush();
		    objectOutputStream.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
