package org.dataflowanalysis.converter.webdfd;

import java.util.List;

public record Model(String type, String id, List<Child> children) {}
