package org.dataflowanalysis.converter.web2dfd.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a value for the web editor
 * @param id The id of the value
 * @param text The content of the value
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Value(String id, String text, List<Object> excludes, String additionalInformation) {
}