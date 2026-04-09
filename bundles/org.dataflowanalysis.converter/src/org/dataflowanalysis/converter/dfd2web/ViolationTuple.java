package org.dataflowanalysis.converter.dfd2web;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;

/**
 * Pairs an {@link AnalysisConstraint} with the {@link DSLResult} produced when that constraint is violated.
 * <p/>
 * Used internally to carry violation data through the conversion pipeline before
 * being transformed into {@link org.dataflowanalysis.converter.web2dfd.model.Violation} objects for the web editor format.
 *
 * @param constraint The constraint that was violated
 * @param result The DSL result containing the matched vertices and transpose flow graph
 */
public record ViolationTuple(AnalysisConstraint constraint, DSLResult result) {
	
}
