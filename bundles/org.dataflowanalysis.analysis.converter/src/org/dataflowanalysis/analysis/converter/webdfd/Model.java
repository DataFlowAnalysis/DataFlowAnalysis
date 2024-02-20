package org.dataflowanalysis.analysis.converter.webdfd;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// The WebEditor is susceptible to changes, and to accommodate new fields, we disregard any unseen fields
@JsonIgnoreProperties(ignoreUnknown = true)
public record Model(String type, String id, List<Child> children) {
}
