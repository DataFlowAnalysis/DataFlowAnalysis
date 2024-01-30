package org.dataflowanalysis.converter.webdfd;

import java.util.List;

public record WebLabelType(String id, String name, List<Value> values) {}
