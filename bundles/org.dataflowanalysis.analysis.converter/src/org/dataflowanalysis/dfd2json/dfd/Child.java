package org.dataflowanalysis.dfd2json.dfd;

import java.util.List;

public record Child(String text, List<LabelType> labels, List<Port> ports, String id, String type, String sourceId, String targetId, List<Child> children) {}
