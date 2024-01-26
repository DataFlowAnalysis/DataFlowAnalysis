package org.dataflowanalysis.dfd2json.dfd;

import java.util.List;

public record Edge(String id, String type, String sourceId, String targetId, String text, List<Object> children) {}
