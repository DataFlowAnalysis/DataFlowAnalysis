package org.palladiosimulator.dataflow.confidentiality.analysis.scalibility;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.palladiosimulator.dataflow.confidentiality.scalability.AnalysisExecutor;
import org.palladiosimulator.dataflow.confidentiality.scalability.GraphExporter;
import org.palladiosimulator.dataflow.confidentiality.scalability.ResultExporter;
import org.palladiosimulator.dataflow.confidentiality.scalability.TestRunner;

public class ScalibiliyMainOld implements IApplication {
	private static AnalysisExecutor analysisExecutor = new OldAnalysisExecutor();
	
	public static void main(String[] args) {
		if (args.length > 0 && args[0].equalsIgnoreCase("-export")) {
			ResultExporter exporter = new ResultExporter();
			exporter.exportResults(TestRunner.getTests(), analysisExecutor);
		} else if (args.length > 1 && args[0].equalsIgnoreCase("-start")) {
			int start = Integer.parseInt(args[1]);
			TestRunner runner = new TestRunner(TestRunner.getTests(), analysisExecutor, true);
			runner.runTests(start);
		} else if (args.length > 0 && args[0].equalsIgnoreCase("-graph")) {
			GraphExporter exporter = new GraphExporter();
			exporter.exportResults(TestRunner.getTests(), analysisExecutor);
		} else {
			TestRunner runner = new TestRunner(TestRunner.getTests(), analysisExecutor, true);
			runner.runTests();
		}
	}

	@Override
	public Object start(IApplicationContext context) throws Exception {
		ScalibiliyMainOld.main(new String[] {});
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}
}
