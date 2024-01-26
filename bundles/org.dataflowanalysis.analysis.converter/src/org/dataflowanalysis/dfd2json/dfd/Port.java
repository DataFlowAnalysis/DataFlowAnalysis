package org.dataflowanalysis.dfd2json.dfd;

import java.util.List;

public record Port(String behavior, String id, String type, List<Object> children) {}
