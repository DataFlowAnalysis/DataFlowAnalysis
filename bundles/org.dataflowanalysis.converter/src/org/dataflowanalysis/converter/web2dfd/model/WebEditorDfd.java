package org.dataflowanalysis.converter.web2dfd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a web editor data flow diagram
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WebEditorDfd(Model model, List<WebEditorLabelType> labelTypes, String mode, List<Constraint> constraints, List<Violation> violations) {
	

	/**
	 * Returns a new {@link WebEditorDfd} with the given list of violations, keeping all other fields unchanged.
	 * <p/>
	 * Follows the copy-with pattern, producing a modified copy of this immutable record
	 * without altering the original instance.
	 * @param violations List of {@link Violation} objects to include in the new instance
	 * @return A new {@link WebEditorDfd} instance with the updated violations
	 */
	public WebEditorDfd withViolations(List<Violation> violations) {
        return new WebEditorDfd(model, labelTypes, mode, constraints, violations);
    }
	
    /**
     * Sorts the data flow diagram based on the id's of the contents
     */
    public void sort() {
        labelTypes().sort(Comparator.comparing(WebEditorLabelType::id));

        List<Child> children = model().children();

        children.sort(Comparator.comparing(Child::id));

        for (Child child : children) {
            if (child.labels() != null) {
                child.labels()
                        .sort(Comparator.comparing(WebEditorLabel::labelTypeId)
                                .thenComparing(WebEditorLabel::labelTypeValueId));
            }
            if (child.ports() != null) {
                child.ports()
                        .sort(Comparator.comparing(Port::id));
            }
        }
    }
    
}