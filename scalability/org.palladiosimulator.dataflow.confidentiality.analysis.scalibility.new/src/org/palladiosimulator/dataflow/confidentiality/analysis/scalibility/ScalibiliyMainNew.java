package org.palladiosimulator.dataflow.confidentiality.analysis.scalibility;

import org.palladiosimulator.dataflow.confidentiality.scalability.AnalysisExecutor;
import org.palladiosimulator.dataflow.confidentiality.scalability.GraphExporter;
import org.palladiosimulator.dataflow.confidentiality.scalability.ResultExporter;
import org.palladiosimulator.dataflow.confidentiality.scalability.TestRunner;

public class ScalibiliyMainNew {
	private static AnalysisExecutor analysisExecutor = new NewAnalysisExecutor();
	
	public static void main(String[] args) {
		if (args.length > 0 && args[0].equalsIgnoreCase("-export")) {
			ResultExporter exporter = new ResultExporter();
			exporter.exportResults(TestRunner.getAllTests(), analysisExecutor);
		} else if (args.length > 1 && args[0].equalsIgnoreCase("-start")) {
			int start = Integer.parseInt(args[1]);
			TestRunner runner = new TestRunner(TestRunner.getTests(), analysisExecutor, false);
			runner.runTests(start);
		} else if (args.length > 0 && args[0].equalsIgnoreCase("-graph")) {
			GraphExporter exporter = new GraphExporter();
			exporter.exportResults(TestRunner.getAllTests(), analysisExecutor);
		} else {
			TestRunner runner = new TestRunner(TestRunner.getTests(), analysisExecutor, false);
			runner.runTests();
		}
	}
}
