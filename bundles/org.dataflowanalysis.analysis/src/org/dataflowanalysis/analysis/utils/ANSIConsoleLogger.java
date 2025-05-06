package org.dataflowanalysis.analysis.utils;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;

public class ANSIConsoleLogger extends ConsoleAppender {
    private static final int NORMAL = 0;
    private static final int BRIGHT = 1;
    private static final int FOREGROUND_RED = 31;
    private static final int FOREGROUND_YELLOW = 33;
    private static final int FOREGROUND_BLUE = 34;
    private static final int FOREGROUND_CYAN = 36;

    private static final String PREFIX = "\u001b[";
    private static final String SUFFIX = "m";
    private static final char SEPARATOR = ';';
    private static final String END_COLOR = PREFIX + SUFFIX;

    private static final String FATAL_COLOR = PREFIX + BRIGHT + SEPARATOR + FOREGROUND_RED + SUFFIX;
    private static final String ERROR_COLOR = PREFIX + NORMAL + SEPARATOR + FOREGROUND_RED + SUFFIX;
    private static final String WARN_COLOR = PREFIX + NORMAL + SEPARATOR + FOREGROUND_YELLOW + SUFFIX;
    // Info messages should have normal foreground color
    private static final String INFO_COLOR = PREFIX + SUFFIX;
    private static final String DEBUG_COLOR = PREFIX + NORMAL + SEPARATOR + FOREGROUND_CYAN + SUFFIX;
    private static final String TRACE_COLOR = PREFIX + NORMAL + SEPARATOR + FOREGROUND_BLUE + SUFFIX;

    public ANSIConsoleLogger(Layout layout) {
        super(layout);
    }

    /**
     * Wraps the ANSI control characters around the output from the super-class Appender.
     */
    protected void subAppend(LoggingEvent event) {
        this.qw.write(getColor(event.getLevel()));
        super.subAppend(event);
        this.qw.write(END_COLOR);

        if (this.immediateFlush) {
            this.qw.flush();
        }
    }

    /**
     * Determines the correct color for the corresponding logging level
     * @param level Logging level of the message
     * @return Returns a string that colors the message according to it's level
     */
    private String getColor(Level level)
    {
        return switch (level.toInt()) {
            case Priority.FATAL_INT -> FATAL_COLOR;
            case Priority.ERROR_INT -> ERROR_COLOR;
            case Priority.WARN_INT -> WARN_COLOR;
            case Priority.INFO_INT -> INFO_COLOR;
            case Priority.DEBUG_INT -> DEBUG_COLOR;
            default -> TRACE_COLOR;
        };
    }

}
