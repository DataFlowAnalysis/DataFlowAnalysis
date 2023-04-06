package org.palladiosimulator.dataflow.confidentiality.scalability;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityTest;
import org.palladiosimulator.dataflow.confidentiality.scalability.tests.BranchCountTest;
import org.palladiosimulator.dataflow.confidentiality.scalability.tests.CharacteristicsPropagationTest;
import org.palladiosimulator.dataflow.confidentiality.scalability.tests.NodeCharacteristicsTest;
import org.palladiosimulator.dataflow.confidentiality.scalability.tests.VariableActionsTest;

public class ScalibiltyMain {
	private static List<ScalibilityTest> tests = new ArrayList<>();
	
	public static void main(String[] args) {
		if (args.length > 0 && args[0].equalsIgnoreCase("-export")) {
			ResultExporter exporter = new ResultExporter();
			exporter.exportResults();
		} else if (args.length > 1 && args[0].equalsIgnoreCase("-start")) {
			int start = Integer.parseInt(args[1]);
			registerTests();
			TestRunner runner = new TestRunner(tests);
			runner.runTests(start);
		} else {
			registerTests();
			TestRunner runner = new TestRunner(tests);
			runner.runTests();
		}
	}
	
	private static void registerTests() {
		tests.add(new BranchCountTest());
	}
}
