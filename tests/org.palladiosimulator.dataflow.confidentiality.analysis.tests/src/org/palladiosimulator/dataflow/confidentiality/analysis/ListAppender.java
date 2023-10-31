package org.palladiosimulator.dataflow.confidentiality.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * This class intercepts all logging events of a logger
 */
public class ListAppender extends AppenderSkeleton {
	private List<LoggingEvent> loggingEvents = new ArrayList<>();
	
	/**
	 * Returns the list of all captured logging events
	 * @return List of all occured logging events
	 */
	public List<LoggingEvent> getLoggingEvents() {
		return Collections.unmodifiableList(this.loggingEvents);
	}
	
	/**
	 * Returns whether a logging event, with the given level, has occured
	 * @param level Level of the logging event
	 * @return Returns true, if a matching logging event could be found
	 */
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
