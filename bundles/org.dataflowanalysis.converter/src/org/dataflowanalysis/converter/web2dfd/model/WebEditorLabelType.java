package org.dataflowanalysis.converter.web2dfd.model;

import java.util.List;

/**
 * Represents label type for the web editor
 * @param id The id of the label type
 * @param name The name of the label type
 * @param values A list of {@link Value}
 */
public record WebEditorLabelType(String id, String name, List<Value> values) {
}
