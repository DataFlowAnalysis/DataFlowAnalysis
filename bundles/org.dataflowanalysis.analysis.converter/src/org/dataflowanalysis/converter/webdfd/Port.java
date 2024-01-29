package org.dataflowanalysis.converter.webdfd;

import java.util.List;

public record Port(String behavior, String id, String type, List<Object> children) {}
