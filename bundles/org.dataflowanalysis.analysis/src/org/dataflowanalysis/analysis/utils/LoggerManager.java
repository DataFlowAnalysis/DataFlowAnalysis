package org.dataflowanalysis.analysis.utils;

import java.util.HashMap;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoggerManager {
    private static final Level DEFAULT_LOG_LEVEL = Level.INFO;
    
    private static final AtomicBoolean CONFIGURED = new AtomicBoolean(false);
    
    private static final LoggerManager instance = new LoggerManager();

    
    private final HashMap<Class<?>,Logger> loggers;
    

    private LoggerManager() {
        if (CONFIGURED.compareAndSet(false, true)) {
            org.apache.log4j.LogManager.resetConfiguration();
            BasicConfigurator.configure(
                new ANSIConsoleLogger(
                    new EnhancedPatternLayout("%-6r [%p] %-35C{1} - %m%n")
                )
            );
        }
        this.loggers = new HashMap<>();
    }

    public void setLevel(Level level) {
        this.loggers.values().forEach(it -> it.setLevel(level));
    }

    public void setLevel(Level level, Class<?> clazz) {
        Logger logger = this.loggers.get(clazz);
        logger.setLevel(level);
    }

    public void resetLevel() {
        this.loggers.values().forEach(it -> it.setLevel(DEFAULT_LOG_LEVEL));
    }

    public static Logger getLogger(Class<?> clazz) {
        Logger logger = instance.loggers.get(clazz);
        if (logger == null) {
            logger = Logger.getLogger(clazz);
            logger.setLevel(DEFAULT_LOG_LEVEL);
            instance.loggers.put(clazz,logger);
        }
        HashMap<Class<?>,Logger> log = instance.loggers;
        
        return logger;
    }

    public static LoggerManager getInstance() {
        return instance;
    }
}
