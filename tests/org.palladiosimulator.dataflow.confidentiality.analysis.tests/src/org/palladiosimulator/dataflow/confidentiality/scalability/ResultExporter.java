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

import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityParameter;

public class ResultExporter {
	
	public void exportResults() {
		try {
			FileInputStream input = new FileInputStream("results.ser");
			ObjectInputStream inputObjects = new ObjectInputStream(input);
			List<ScalibilityParameter> inputData = (ArrayList<ScalibilityParameter>) inputObjects.readObject();
			FileOutputStream output = new FileOutputStream("results.txt");
			inputData.forEach(it -> exportParameter(it, output));
			inputObjects.close();
			input.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void exportParameter(ScalibilityParameter parameter, FileOutputStream file) {
		StringJoiner string = new StringJoiner(",");
		string.add(parameter.getTestName());
		string.add(Integer.toString(parameter.getModelSize()));
		string.add(Long.toString(parameter.getStartTime().toInstant().toEpochMilli()));
		string.add(Long.toString(parameter.getStopTime().toInstant().toEpochMilli()));
		
		for(Entry<Date, String> entry : parameter.getLogEvents().entrySet()) {
			string.add(entry.getKey().toInstant().toEpochMilli() + ":" + entry.getValue());
		}
		
		try {
			file.write(string.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
