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
	private Map<Date, String> logEvents;
	private final int modelSize;
	
	public ScalibilityParameter(int modelSize) {
		this.modelSize = modelSize;
		this.logEvents = new HashMap<>();
	}
	
	public void startTiming() {
		this.startTime = Date.from(Instant.now());
	}
	
	public void logAction(String action) {
		logEvents.put(Date.from(Instant.now()), action);
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
	
	public Map<Date, String> getLogEvents() {
		return new HashMap<>(this.logEvents);
	}
}
