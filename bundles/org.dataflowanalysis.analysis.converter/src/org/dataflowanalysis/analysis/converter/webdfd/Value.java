package org.dataflowanalysis.analysis.converter.webdfd;

/**
 * Represents a value for the web editor
 * @param id The id of the value
 * @param text The content of the value
 */
public record Value(String id, String text) {
}