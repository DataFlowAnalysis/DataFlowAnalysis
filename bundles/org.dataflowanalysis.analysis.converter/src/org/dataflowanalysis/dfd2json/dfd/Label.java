package org.dataflowanalysis.dfd2json.dfd;

import java.util.List;

public record Label(String id, String name, List<Value> values) {}
