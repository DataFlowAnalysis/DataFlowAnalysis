package org.dataflowanalysis.converter.web2dfd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Represents a web editor model consisting of type, id, and a list of children.
 * @param type The type of the model usually <b>root</b>
 * @param id The id of the model
 * @param children A list of {@link Child}
 */

// The WebEditor is susceptible to changes, and to accommodate new fields, we disregard any unseen fields
@JsonIgnoreProperties(ignoreUnknown = true)
public record Model(String type, String id, List<Child> children) {
}
