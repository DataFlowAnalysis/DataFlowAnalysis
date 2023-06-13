package org.palladiosimulator.dataflow.confidentiality.scalability;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityParameter;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityTest;
import org.palladiosimulator.dataflow.confidentiality.scalability.tests.BranchCountTest;
import org.palladiosimulator.dataflow.confidentiality.scalability.tests.CharacteristicsPropagationTest;
import org.palladiosimulator.dataflow.confidentiality.scalability.tests.NodeCharacteristicsTest;
import org.palladiosimulator.dataflow.confidentiality.scalability.tests.SEFFParameterTest;
import org.palladiosimulator.dataflow.confidentiality.scalability.tests.VariableActionsTest;
import org.palladiosimulator.dataflow.confidentiality.scalability.tests.VariableCountTest;

public class TestRunner {
	private static final int RUNS_PER_STAGE = 10;
	public static final String BASE_PATH = "/home/felix/Fluidtrust/Repositories/Palladio-Addons-DataFlowConfidentiality-Analysis/scalability";
	
	private final Logger logger = Logger.getLogger(TestRunner.class);
	
	private List<ScalibilityTest> tests;
	private List<ScalibilityParameter> results;
	private AnalysisExecutor analysisExecutor;
	private boolean legacy;
	
	public TestRunner(List<ScalibilityTest> tests, AnalysisExecutor analysisExecutor, boolean legacy) {
		this.tests = tests;
		this.results = new ArrayList<>();
		this.analysisExecutor = analysisExecutor;
		this.legacy = legacy;
	}
	
	public void runTests() {
		this.runTests(0);
	}
	
	public void runTests(int start) {
		for(int i = start; i < 3; i++) {
			for(ScalibilityTest test : this.tests) {
				logger.info("Running test with name " + test.getTestName());
				this.runTest(test, i);
			}
		}
	}
	
	private void runTest(ScalibilityTest test, int index) {
		for (int i = 0; i < 5; i++) {
			logger.info("Running warmup " + i + "/5");
			ScalibilityParameter parameter = new ScalibilityParameter(10, test.getTestName(), legacy);
			test.run(parameter, analysisExecutor);
		}
		for (int j = 0; j < RUNS_PER_STAGE; j++) {
			int modelSize = test.getModelSize(index);
			String modelName = test.getTestName();
			logger.info("Running test with model " + modelName + " and size " + modelSize + ", Iteration: " + j);
			ScalibilityParameter parameter = new ScalibilityParameter(modelSize, test.getTestName(), legacy);
			test.run(parameter, analysisExecutor);
			this.results.add(parameter);
			saveResults(analysisExecutor.getPrefix() + parameter.getTestName());
		}
	}
	
	private void saveResults(String testName) {
		try {
			FileOutputStream fileOutputStream
		      = new FileOutputStream(TestRunner.BASE_PATH + "/results/" + testName + ".ser");
		    ObjectOutputStream objectOutputStream 
		      = new ObjectOutputStream(fileOutputStream);
		    objectOutputStream.writeObject(this.results);
		    objectOutputStream.flush();
		    objectOutputStream.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static List<ScalibilityTest> getTests() {
		return List.of(
				//new CharacteristicsPropagationTest()//,
				//new BranchCountTest(),
				//new NodeCharacteristicsTest(),
				new SEFFParameterTest()
				//new VariableActionsTest()
		);
	}
	
	public static List<ScalibilityTest> getAllTests() {
		return List.of(
				new CharacteristicsPropagationTest(),
				new BranchCountTest(),
				new NodeCharacteristicsTest(),
				new SEFFParameterTest(),
				new VariableActionsTest()
		);
	}
}
