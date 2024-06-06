package org.dataflowanalysis.converter.webdfd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Represents a port in the web editor dfd
 * @param behavior Expression containing behavior. Can be empty
 * @param id The id of the port
 * @param type The type of the port (in/out)
 * @param children A list of {@link Child}
 */

// The WebEditor is susceptible to changes, and to accommodate new fields, we disregard any unseen fields
@JsonIgnoreProperties(ignoreUnknown = true)
public record Port(String behavior, String id, String type, List<Object> children) {

    public boolean equals(Port other) {
        if (!this.type.equals(other.type)) {
            return false;
        } else if (this.type.equals("port:dfd-output")) {
            return this.id.equals(other.id) && this.behavior.equals(other.behavior) && this.children.equals(other.children);
        } else {
            return this.id.equals(other.id) && this.children.equals(other.children);
        }
    }
}
