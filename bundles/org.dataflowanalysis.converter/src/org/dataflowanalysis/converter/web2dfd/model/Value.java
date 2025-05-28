package org.dataflowanalysis.converter.web2dfd.model;

/**
 * Represents a value for the web editor
 * @param id The id of the value
 * @param text The content of the value
 */
public record Value(String id, String text) {
}