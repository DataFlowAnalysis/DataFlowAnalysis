package org.palladiosimulator.dataflow.confidentiality.scalability.result;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ScalibilityParameter implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Date startTime;
	private Date stopTime;
	private Map<String, Date> logEvents;
	private final int modelSize;
	private final String testName;
	
	public ScalibilityParameter(int modelSize, String testName) {
		this.modelSize = modelSize;
		this.logEvents = new HashMap<>();
		this.testName = testName;
	}
	
	public void startTiming() {
		this.startTime = Date.from(Instant.now());
	}
	
	public void logAction(String action) {
		logEvents.put(action, Date.from(Instant.now()));
	}
	
	public void stopTiming() {
		this.stopTime = Date.from(Instant.now());
	}
	
	public int getModelSize() {
		return modelSize;
	}
	
	public Date getStartTime() {
		return startTime;
	}
	
	public Date getStopTime() {
		return stopTime;
	}
	
	public long getDuration() {
		return this.stopTime.getTime() - this.startTime.getTime();
	}
	
	public Map<String, Date> getLogEvents() {
		return new HashMap<>(this.logEvents);
	}
	
	public String getTestName() {
		return testName;
	}
}
