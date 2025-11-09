package org.dataflowanalysis.analysis.utils;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class LoggerManager {
    private static final Level DEFAULT_LOG_LEVEL = Level.INFO;

    private static final LoggerManager instance = new LoggerManager();

    private final List<Logger> loggers;

    public LoggerManager() {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure(new ANSIConsoleLogger(new EnhancedPatternLayout("%-6r [%p] %-35C{1} - %m%n")));
        this.loggers = new ArrayList<>();
    }

    public void setLevel(Level level) {
        this.loggers.forEach(it -> it.setLevel(level));
    }

    public void resetLevel() {
        this.loggers.forEach(it -> it.setLevel(DEFAULT_LOG_LEVEL));
    }

    public static Logger getLogger(Class<?> clazz) {
        Logger logger = Logger.getLogger(clazz);
        logger.setLevel(DEFAULT_LOG_LEVEL);
        instance.loggers.add(logger);
        return logger;
    }

    public static LoggerManager getInstance() {
        return instance;
    }
}
