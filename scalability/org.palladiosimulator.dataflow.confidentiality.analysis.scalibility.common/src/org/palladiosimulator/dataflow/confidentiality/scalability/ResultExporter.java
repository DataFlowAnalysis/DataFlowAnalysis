package org.palladiosimulator.dataflow.confidentiality.scalability;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;

import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityEvent;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityParameter;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityTest;

public class ResultExporter {
	private final List<String> prefixes = List.of("New", "Old");
	
	public void exportResults(List<ScalibilityTest> tests, AnalysisExecutor analysisExecutor) {
		for(String prefix : prefixes) {
			tests.forEach(it -> this.exportResult(it, analysisExecutor, prefix));
		}
	}
	
	public void exportResult(ScalibilityTest test, AnalysisExecutor analysisExecutor, String prefix) {
		try {
			FileInputStream input = new FileInputStream(TestRunner.BASE_PATH +  "/results/" + prefix + test.getTestName() + ".ser");
			ObjectInputStream inputObjects = new ObjectInputStream(input);
			List<ScalibilityParameter> inputData = (ArrayList<ScalibilityParameter>) inputObjects.readObject();
			Instant timestamp = Instant.now();
			FileOutputStream output = new FileOutputStream(TestRunner.BASE_PATH + "/results/" + prefix + test.getTestName() + timestamp.toString() + ".csv");
			this.writeHeader(output);
			for (ScalibilityParameter parameter : inputData) {
				if(!parameter.getTestName().equals(test.getTestName())) {
					continue;
				}
				exportParameter(parameter, output);
				output.write(System.lineSeparator().getBytes());
			}
			inputObjects.close();
			input.close();
			System.out.println("Exported test " + prefix + test.getTestName());
		} catch (FileNotFoundException e) {
			System.out.println("Skipping test " + prefix + test.getTestName());
			return;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void writeHeader(FileOutputStream file) {
		StringJoiner string = new StringJoiner(",");
		string.add("TestName");
		string.add("ModelSize");
		string.add(ScalibilityEvent.ANALYSIS_INITIALZATION.getName());
		string.add(ScalibilityEvent.SEQUENCE_FINDING.getName());
		string.add(ScalibilityEvent.PROPAGATION.getName());
		string.add("Total");
		
		try {
			file.write((string.toString() + System.lineSeparator()).getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void exportParameter(ScalibilityParameter parameter, FileOutputStream file) {
		long start = parameter.getStartTime().toInstant().toEpochMilli();
		
		StringJoiner string = new StringJoiner(",");
		string.add(parameter.getTestName());
		string.add(Integer.toString(parameter.getModelSize()));
		string.add(Long.toString(parameter.getLogEvents().get(ScalibilityEvent.ANALYSIS_INITIALZATION).toInstant().toEpochMilli() - start));
		string.add(Long.toString(parameter.getLogEvents().get(ScalibilityEvent.SEQUENCE_FINDING).toInstant().toEpochMilli() - start));
		string.add(Long.toString(parameter.getLogEvents().get(ScalibilityEvent.PROPAGATION).toInstant().toEpochMilli() - start));
		string.add(Long.toString(parameter.getStopTime().toInstant().toEpochMilli() - start));
		
		try {
			file.write(string.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
