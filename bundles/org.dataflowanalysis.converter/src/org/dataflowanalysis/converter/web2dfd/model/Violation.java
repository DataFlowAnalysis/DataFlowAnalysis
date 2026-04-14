package org.dataflowanalysis.converter.web2dfd.model;

import java.util.List;

/**
 * Represents a constraint violation found during data flow analysis, for use in the web editor format.
 *
 * @param constraint String representation of the violated constraint
 * @param tfg String representation of the transpose flow graph in which the violation was found
 * @param violatedVertices String representation of the vertices where the constraint is violated
 * @param inducingVertices String representation of the vertices where the violating characteristic was first introduced
 */
public record Violation(
	    String constraint,
	    List<String> tfg,
	    List<String> violatedVertices,
	    List<String> inducingVertices
	) {}
