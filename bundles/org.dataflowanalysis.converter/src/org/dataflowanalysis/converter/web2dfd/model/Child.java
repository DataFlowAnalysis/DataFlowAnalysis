package org.dataflowanalysis.converter.web2dfd.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.dataflowanalysis.converter.web2dfd.ChildSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
@JsonSerialize(using = ChildSerializer.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Child(String text, List<WebEditorLabel> labels, List<Port> ports, String id, String type, String sourceId, String targetId, 
        List<Annotation> annotations, List<Child> children, Position position, Size size) {

    @JsonCreator
    public static Child create(
            @JsonProperty("text") String text,
            @JsonProperty("labels") List<WebEditorLabel> labels,
            @JsonProperty("ports") List<Port> ports,
            @JsonProperty("id") String id,
            @JsonProperty("type") String type,
            @JsonProperty("sourceId") String sourceId,
            @JsonProperty("targetId") String targetId,
            @JsonProperty("annotations") List<Annotation> annotations,
            @JsonProperty("children") List<Child> children,
            @JsonProperty("position") Position position,
            @JsonProperty("size") Size size
    ) {
        return new Child(
                text,
                labels,
                ports,
                id,
                type,
                sourceId,
                targetId,
                annotations == null && type.startsWith("node")? new ArrayList<>() : annotations,
                children, 
                position,
                size
        );
    }
    
    /**
     * Overrides equals method to support child type specific equality checks.
     */
    @Override
    public boolean equals(Object otherObject) {
        if (!(otherObject instanceof Child other)) {
            return false;
        }
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
