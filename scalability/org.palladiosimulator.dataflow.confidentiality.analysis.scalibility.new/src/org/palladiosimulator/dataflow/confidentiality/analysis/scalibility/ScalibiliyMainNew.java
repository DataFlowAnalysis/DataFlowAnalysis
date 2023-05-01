package org.palladiosimulator.dataflow.confidentiality.analysis.scalibility;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.scalability.AnalysisExecutor;
import org.palladiosimulator.dataflow.confidentiality.scalability.ResultExporter;
import org.palladiosimulator.dataflow.confidentiality.scalability.TestRunner;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityTest;
import org.palladiosimulator.dataflow.confidentiality.scalability.tests.BranchCountTest;

public class ScalibiliyMainNew {
	private static List<ScalibilityTest> tests = new ArrayList<>();
	private static AnalysisExecutor analysisExecutor = new NewAnalysisExecutor();
	
	public static void main(String[] args) {
		if (args.length > 0 && args[0].equalsIgnoreCase("-export")) {
			ResultExporter exporter = new ResultExporter();
			exporter.exportResults();
		} else if (args.length > 1 && args[0].equalsIgnoreCase("-start")) {
			int start = Integer.parseInt(args[1]);
			registerTests();
			TestRunner runner = new TestRunner(tests, analysisExecutor, false);
			runner.runTests(start);
		} else {
			registerTests();
			TestRunner runner = new TestRunner(tests, analysisExecutor, false);
			runner.runTests();
		}
	}
	
	private static void registerTests() {
		tests.add(new BranchCountTest());
	}
}
