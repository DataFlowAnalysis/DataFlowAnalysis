package org.palladiosimulator.dataflow.confidentiality.scalability;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityEvent;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityParameter;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityTest;

public class GraphExporter {
	private final List<String> prefixes = List.of("New", "Old");

	public void exportResults(List<ScalibilityTest> tests, AnalysisExecutor analysisExecutor) {
		for(String prefix : prefixes) {
			tests.forEach(it -> this.exportResult(it, analysisExecutor, prefix));
		}	
	}
	
	public void exportResult(ScalibilityTest test, AnalysisExecutor analysisExecutor, String prefix) {
		try {
			FileInputStream input = new FileInputStream(TestRunner.BASE_PATH + "/results/" + prefix + test.getTestName() + ".ser");
			ObjectInputStream inputObjects = new ObjectInputStream(input);
			List<ScalibilityParameter> inputData = (ArrayList<ScalibilityParameter>) inputObjects.readObject();
			Instant timestamp = Instant.now();
			FileOutputStream output = new FileOutputStream(TestRunner.BASE_PATH + "/results/graphs/" + prefix + test.getTestName() + timestamp.toString() + ".csv");
			this.writeHeader(output);
			Map<String, List<ScalibilityParameter>> indexedData = new HashMap<>();
			for (ScalibilityParameter parameter : inputData) {
				if(!parameter.getTestName().equals(test.getTestName())) {
					continue;
				}
				String key = parameter.getTestName() + parameter.getModelSize();
				if(indexedData.containsKey(key)) {
					indexedData.get(key).add(parameter);
				} else {
					List<ScalibilityParameter> data = new ArrayList<>();
					data.add(parameter);
					indexedData.put(key, data);
				}
			}
			TreeMap<String, List<ScalibilityParameter>> sortedData = new TreeMap<>(indexedData);
			for(List<ScalibilityParameter> parameters : sortedData.values()) {
				if(parameters.size() != 10) {
					continue;
				}
				exportParameter(parameters, output);
				output.write(System.lineSeparator().getBytes());
			}
			inputObjects.close();
			input.close();
			System.out.println("Created Graph data: " + prefix + test.getTestName());
		} catch (FileNotFoundException e) {
			System.out.println("Skipping test: " + prefix + test.getTestName());
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void writeHeader(FileOutputStream file) {
		StringJoiner string = new StringJoiner(",");
		string.add("index");
		string.add("median");
		string.add("box_top");
		string.add("box_bottom");
		string.add("whisker_top");
		string.add("whisker_bottom");
		
		try {
			file.write((string.toString() + System.lineSeparator()).getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void exportParameter(List<ScalibilityParameter> parameters, FileOutputStream file) {
		long whisherTop = 0;
		long whiskerBottom = Long.MAX_VALUE;
		List<Long> totals = new ArrayList<>();
		for(ScalibilityParameter parameter : parameters) {
			long total = parameter.getStopTime().toInstant().toEpochMilli() - parameter.getLogEvents().get(ScalibilityEvent.ANALYSIS_INITIALZATION).toInstant().toEpochMilli();
			totals.add(total);
			whisherTop = Math.max(whisherTop, total);
			whiskerBottom = Math.min(whiskerBottom, total);
		}
		Collections.sort(totals);
		long median = totals.get(totals.size() / 2);
		long boxTop = totals.get((int) (totals.size() - (0.25f * totals.size())));
		long boxBottom = totals.get((int) (totals.size() - (0.75f * totals.size())));
		StringJoiner string = new StringJoiner(",");
		string.add(Long.toString(parameters.get(0).getModelSize()));
		string.add(Long.toString(median));
		string.add(Long.toString(boxTop));
		string.add(Long.toString(boxBottom));
		string.add(Long.toString(whisherTop));
		string.add(Long.toString(whiskerBottom));
		
		
		
		try {
			file.write(string.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
