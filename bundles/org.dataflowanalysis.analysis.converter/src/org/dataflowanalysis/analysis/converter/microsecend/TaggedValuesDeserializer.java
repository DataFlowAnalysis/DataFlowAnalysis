package org.dataflowanalysis.analysis.converter.microsecend;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TaggedValuesDeserializer extends JsonDeserializer<Map<String, List<String>>> {

    @Override
    public Map<String, List<String>> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        Map<String, List<String>> result = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            parser.nextToken();

            if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                List<String> values = objectMapper.readValue(parser, new TypeReference<List<String>>() {
                });
                result.put(fieldName, values);
            } else {
                String singleValue = getValueAsString(parser.readValueAsTree());
                result.put(fieldName, List.of(singleValue));
            }
        }

        return result;
    }

    private String getValueAsString(JsonNode node) {
        return node.isTextual() ? node.asText() : node.toString();
    }
}
