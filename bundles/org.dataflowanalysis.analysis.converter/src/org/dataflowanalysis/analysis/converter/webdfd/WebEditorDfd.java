package org.dataflowanalysis.analysis.converter.webdfd;

import java.util.Comparator;
import java.util.List;

public record WebEditorDfd(Model model, List<WebEditorLabelType> labelTypes) {

    public void sort() {
        labelTypes().sort(Comparator.comparing(WebEditorLabelType::id));

        List<Child> children = model().children();

        children.sort(Comparator.comparing(Child::id));

        for (Child child : children) {
            if (child.labels() != null) {
                child.labels().sort(Comparator.comparing(WebEditorLabel::labelTypeId).thenComparing(WebEditorLabel::labelTypeValueId));
            }
            if (child.ports() != null) {
                child.ports().sort(Comparator.comparing(Port::id));
            }
        }
    }
}