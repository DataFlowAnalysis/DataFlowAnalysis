package org.dataflowanalysis.converter.web2dfd.model;

import java.util.List;

//TODO: DOCUMENTATION
public record Violation(String constraint, List<String> violationCauseGraph) {

}
