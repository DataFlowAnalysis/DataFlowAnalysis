package org.dataflowanalysis.analysis.converter.microsecend;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MicroSecEnd(
    List<Service> services,
    
    @JsonProperty("external_entities")
    List<ExternalEntity> externalEntities,
    
    @JsonProperty("information_flows")
    List<InformationFlow> informationFlows
){}
