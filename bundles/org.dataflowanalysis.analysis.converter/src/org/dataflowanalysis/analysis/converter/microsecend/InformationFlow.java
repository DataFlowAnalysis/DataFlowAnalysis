package org.dataflowanalysis.analysis.converter.microsecend;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents the flow of information between entities in a system.
 * @param sender The name of the entity sending the information.
 * @param receiver The name of the entity receiving the information.
 * @param stereotypes A list of stereotypes that categorize the nature of the information flow.
 * @param taggedValues A map of tagged values, where each key is a tag name and the associated value is a list of
 * strings. This map is deserialized using the {@link TaggedValuesDeserializer} class.
 */
public record InformationFlow(String sender,

        String receiver,

        List<String> stereotypes,

        @JsonProperty("tagged_values") @JsonDeserialize(using = TaggedValuesDeserializer.class) Map<String, List<String>> taggedValues) {

    public InformationFlow {
        stereotypes = stereotypes.stream().map(stereotype -> stereotype.trim().replaceAll("[^a-zA-Z0-9]", "")).collect(Collectors.toList());
    }
}
