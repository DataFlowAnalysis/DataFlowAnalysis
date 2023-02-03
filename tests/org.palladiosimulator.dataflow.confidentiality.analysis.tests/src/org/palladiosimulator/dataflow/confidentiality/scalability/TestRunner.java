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
	private final Logger logger = Logger.getLogger(TestRunner.class);
	
	private List<ScalibilityTest> tests;
	private List<ScalibilityParameter> results;
	
	public TestRunner(List<ScalibilityTest> tests) {
		this.tests = tests;
		this.results = new ArrayList<>();
	}
	
	public void runTests() {
		tests.forEach(this::runTest);
	}
	
	private void runTest(ScalibilityTest test) {
		for(int i = 0; true; i++) {
			// TODO: Build average of iterations
			int modelSize = test.getModelSize(i);
			logger.info("Running test with model size " + modelSize);
			ScalibilityParameter parameter = new ScalibilityParameter(modelSize);
			test.run(parameter);
			this.results.add(parameter);
			saveResults();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void saveResults() {
		try {
			FileOutputStream fileOutputStream
		      = new FileOutputStream("results.txt");
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
