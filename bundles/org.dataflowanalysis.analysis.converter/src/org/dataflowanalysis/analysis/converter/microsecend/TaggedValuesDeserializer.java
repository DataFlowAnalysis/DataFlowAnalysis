package org.dataflowanalysis.analysis.converter.microsecend;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class TaggedValuesDeserializer extends JsonDeserializer<Map<String, List<String>>> {

	@Override
    public Map<String, List<String>> deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        Map<String, List<String>> result = new HashMap<>();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            parser.nextToken(); // Move to the value

            if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                // If it's an array, deserialize as List<String>
                @SuppressWarnings("unchecked")
				List<String> values = parser.readValueAs(List.class);
                result.put(fieldName, values);
            } else {
                // If it's a single value, wrap it in a list
                String singleValue = getValueAsString(parser.readValueAsTree());
                result.put(fieldName, List.of(singleValue));
            }
        }

        return result;
    }

    private String getValueAsString(JsonNode node) {
        if (node.isTextual()) {
            return node.asText();
        } else {
            // If it's not a textual node, convert to string
            return node.toString();
        }
    }
}
