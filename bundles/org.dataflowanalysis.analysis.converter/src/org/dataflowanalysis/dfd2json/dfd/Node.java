package org.dataflowanalysis.dfd2json.dfd;

import java.util.List;

public record Node(String text, List<LabelType> labels, List<Port> ports, String id, String type, String sourceId, String targetId, List<Object> children) {}
