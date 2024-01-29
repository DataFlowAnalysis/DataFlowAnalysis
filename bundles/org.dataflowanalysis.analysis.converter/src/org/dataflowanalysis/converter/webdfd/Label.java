package org.dataflowanalysis.converter.webdfd;

import java.util.List;

public record Label(String id, String name, List<Value> values) {}
