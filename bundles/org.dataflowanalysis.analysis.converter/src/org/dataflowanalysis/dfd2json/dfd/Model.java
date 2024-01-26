package org.dataflowanalysis.dfd2json.dfd;

import java.util.List;

public record Model(String type, String id, List<Node> children) {}
