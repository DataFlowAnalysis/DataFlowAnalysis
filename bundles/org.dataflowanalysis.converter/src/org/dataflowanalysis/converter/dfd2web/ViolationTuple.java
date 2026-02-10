package org.dataflowanalysis.converter.dfd2web;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;

//TODO: DOCUMENTATION
public record ViolationTuple(AnalysisConstraint constraint, DSLResult result) {
	
}
