package org.dataflowanalysis.analysis.converter.webdfd;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Model(String type, String id, List<Child> children) {
}
