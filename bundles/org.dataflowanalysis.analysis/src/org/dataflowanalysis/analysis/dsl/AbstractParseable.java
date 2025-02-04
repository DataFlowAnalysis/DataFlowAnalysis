package org.dataflowanalysis.analysis.dsl;

/**
 * This class represents a parsable DSL object and collects functionality common to parsable DSL objects
 */
public abstract class AbstractParseable {
    protected static final String DSL_SEPARATOR = ".";
    protected static final String DSL_DELIMITER = ",";
    protected static final String DSL_INVERTED_SYMBOL = "!";

    protected static final String DSL_PAREN_OPEN = "(";
    protected static final String DSL_PAREN_CLOSE = ")";
}
