package org.palladiosimulator.dataflow.confidentiality.analysis.scalibility;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.scalability.AnalysisExecutor;
import org.palladiosimulator.dataflow.confidentiality.scalability.ResultExporter;
import org.palladiosimulator.dataflow.confidentiality.scalability.TestRunner;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityTest;
import org.palladiosimulator.dataflow.confidentiality.scalability.tests.BranchCountTest;
import org.palladiosimulator.dataflow.confidentiality.scalability.tests.CharacteristicsPropagationTest;
import org.palladiosimulator.dataflow.confidentiality.scalability.tests.NodeCharacteristicsTest;
import org.palladiosimulator.dataflow.confidentiality.scalability.tests.SEFFParameterTest;
import org.palladiosimulator.dataflow.confidentiality.scalability.tests.VariableActionsTest;
import org.palladiosimulator.dataflow.confidentiality.scalability.tests.VariableCountTest;

public class ScalibiliyMainOld {
	private static List<ScalibilityTest> tests = new ArrayList<>();
	private static AnalysisExecutor analysisExecutor = new OldAnalysisExecutor();
	
	public static void main(String[] args) {
		if (args.length > 0 && args[0].equalsIgnoreCase("-export")) {
			ResultExporter exporter = new ResultExporter();
			exporter.exportResults();
		} else if (args.length > 1 && args[0].equalsIgnoreCase("-start")) {
			int start = Integer.parseInt(args[1]);
			registerTests();
			TestRunner runner = new TestRunner(tests, analysisExecutor, true);
			runner.runTests(start);
		} else {
			registerTests();
			TestRunner runner = new TestRunner(tests, analysisExecutor, true);
			runner.runTests();
		}
	}
	
	private static void registerTests() {
		tests.add(new CharacteristicsPropagationTest());
		tests.add(new NodeCharacteristicsTest());
		tests.add(new SEFFParameterTest());
		tests.add(new VariableActionsTest());
		tests.add(new VariableCountTest());
		tests.add(new BranchCountTest());
	}
}
