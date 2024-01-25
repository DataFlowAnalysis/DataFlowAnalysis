package org.dataflowanalysis.json2dfd.microsecend;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SystemConfiguration(
    List<Service> services,
    
    @JsonProperty("external_entities")
    List<ExternalEntity> externalEntities,
    
    @JsonProperty("information_flows")
    List<InformationFlow> informationFlows
){}
