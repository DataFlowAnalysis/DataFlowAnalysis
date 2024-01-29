package org.dataflowanalysis.converter.microsecend;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public record InformationFlow (
    String sender,
    
    String receiver,
    
    List<String> stereotypes,
    
    @JsonProperty("tagged_values")
    @JsonDeserialize(using = TaggedValuesDeserializer.class)
    Map<String, List<String>> taggedValues
){}
