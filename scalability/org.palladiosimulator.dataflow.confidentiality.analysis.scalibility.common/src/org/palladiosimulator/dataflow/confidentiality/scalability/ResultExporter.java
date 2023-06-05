package org.palladiosimulator.dataflow.confidentiality.scalability;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;

import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityEvent;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityParameter;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityTest;

public class ResultExporter {
	
	public void exportResults(List<ScalibilityTest> tests, AnalysisExecutor analysisExecutor) {
		tests.forEach(it -> this.exportResult(it, analysisExecutor));
	}
	
	public void exportResult(ScalibilityTest test, AnalysisExecutor analysisExecutor) {
		try {
			FileInputStream input = new FileInputStream("./results/" + analysisExecutor.getPrefix() + test.getTestName() + ".ser");
			ObjectInputStream inputObjects = new ObjectInputStream(input);
			List<ScalibilityParameter> inputData = (ArrayList<ScalibilityParameter>) inputObjects.readObject();
			FileOutputStream output = new FileOutputStream("./results/" + analysisExecutor.getPrefix() + test.getTestName() + ".csv");
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
