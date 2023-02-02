package org.palladiosimulator.dataflow.confidentiality.scalability;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityTest;
import org.palladiosimulator.dataflow.confidentiality.scalability.tests.ExampleTest;

public class ScalibiltyMain {
	private static List<ScalibilityTest> tests = new ArrayList<>();
	
	public static void main(String[] args) {
		// TODO: Export argument for exporting saved results into readable form
		registerTests();
		TestRunner runner = new TestRunner(tests);
		runner.runTests();
	}
	
	private static void registerTests() {
		tests.add(new ExampleTest());
	}
}
