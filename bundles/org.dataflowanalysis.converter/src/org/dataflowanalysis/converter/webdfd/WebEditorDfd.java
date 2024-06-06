package org.dataflowanalysis.converter.webdfd;

import java.util.Comparator;
import java.util.List;

/**
 * Represents a web editor data flow diagram
 * @param model The model containing the nodes and flows
 * @param labelTypes A list of {@link WebEditorLabelType}
 */
public record WebEditorDfd(Model model, List<WebEditorLabelType> labelTypes) {

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