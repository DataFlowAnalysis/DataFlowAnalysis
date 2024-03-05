package org.dataflowanalysis.analysis.converter.webdfd;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a child (node/edge) in the web editor model
 * @param text The content of the child
 * @param labels List of {@link WebEditorLabel}
 * @param ports List of {@link Port}
 * @param id The id of the child
 * @param type <b>node</b> or <b>edge</b>
 * @param sourceId Id of the source Node
 * @param targetId Id of the target Node
 * @param children List of {@link Child}
 */

// The WebEditor is susceptible to changes, and to accommodate new fields, we disregard any unseen fields
@JsonIgnoreProperties(ignoreUnknown = true)
public record Child(String text, List<WebEditorLabel> labels, List<Port> ports, String id, String type, String sourceId, String targetId,
        List<Child> children) {

    /**
     * Overrides equals method to support child type specific equality checks.
     */
    public boolean equals(Child other) {
        if (!this.type.equals(other.type)) {
            return false;
        } else if (this.type.split(":")[0].equals("node")) {
            return this.text.equals(other.text) && this.id.equals(other.id) && this.children.equals(other.children)
                    && this.labels.equals(other.labels) && this.ports.equals(other.ports);
        } else {
            return this.text.equals(other.text) && this.id.equals(other.id) && this.children.equals(other.children)
                    && this.sourceId.equals(other.sourceId) && this.targetId.equals(other.targetId);

        }
    }
}
