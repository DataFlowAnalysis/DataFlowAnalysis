package org.palladiosimulator.dataflow.confidentiality.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

public class ListAppender extends AppenderSkeleton {
	private List<LoggingEvent> loggingEvents = new ArrayList<>();
	
	public List<LoggingEvent> getLoggingEvents() {
		return Collections.unmodifiableList(this.loggingEvents);
	}
	
	public boolean loggedLevel(Level level) {
		return loggingEvents.stream()
				.anyMatch(it -> it.getLevel().equals(level));
	}

	@Override
	public void close() {
	
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	@Override
	protected void append(LoggingEvent loggingEvent) {
		loggingEvents.add(loggingEvent);
	}
}
