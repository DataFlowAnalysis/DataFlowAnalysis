package org.dataflowanalysis.converter.webdfd;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public record Child(String text, List<LabelType> labels, List<Port> ports, String id, String type, String sourceId, String targetId, List<Child> children) {}
