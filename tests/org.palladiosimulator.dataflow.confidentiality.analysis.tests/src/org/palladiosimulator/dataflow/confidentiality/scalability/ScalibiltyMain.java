package org.palladiosimulator.dataflow.confidentiality.scalability;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityTest;
import org.palladiosimulator.dataflow.confidentiality.scalability.tests.CharacteristicsPropagationTest;
import org.palladiosimulator.dataflow.confidentiality.scalability.tests.NodeCharacteristicsTest;

public class ScalibiltyMain {
	private static List<ScalibilityTest> tests = new ArrayList<>();
	
	public static void main(String[] args) {
		if (args.length > 0 && args[1].equalsIgnoreCase("-export")) {
			ResultExporter exporter = new ResultExporter();
			exporter.exportResults();
		} else {
			registerTests();
			TestRunner runner = new TestRunner(tests);
			runner.runTests();
		}
	}
	
	private static void registerTests() {
		tests.add(new NodeCharacteristicsTest());
	}
}
