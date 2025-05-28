package org.dataflowanalysis.converter.micro2dfd.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for deserializing <b>target_values</b> from the MicroSecEnd dataset.
 */
public class TaggedValuesDeserializer extends JsonDeserializer<Map<String, List<String>>> {

    /**
     * Overrides the default Jackson deserializer
     */
    @Override
    public Map<String, List<String>> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        Map<String, List<String>> result = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName()
                    .trim()
                    .replaceAll("[^a-zA-Z0-9_]", "");
            parser.nextToken();

            if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                List<String> values = objectMapper.readValue(parser, new TypeReference<>() {
                });
                List<String> sanitizedValues = new ArrayList<>();
                for (String value : values) {
                    var sanitizedValue = value.trim()
                            .replaceAll("[^a-zA-Z0-9_]", "");
                    if (!sanitizedValue.isEmpty()) {
                        sanitizedValues.add(sanitizedValue);
                    }
                }
                result.put(fieldName, sanitizedValues);
            } else {
                String singleValue = getValueAsString(parser.readValueAsTree());
                result.put(fieldName, List.of(singleValue));
            }
        }

        return result;
    }

    private String getValueAsString(JsonNode node) {
        return (node.isTextual() ? node.asText() : node.toString()).trim()
                .replaceAll("[^a-zA-Z0-9_]", "");
    }
}
