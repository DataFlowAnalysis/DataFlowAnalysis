package org.dataflowanalysis.analysis.converter.microsecend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MicroSecEnd(List<Service> services,

        @JsonProperty("external_entities") List<ExternalEntity> externalEntities,

        @JsonProperty("information_flows") List<InformationFlow> informationFlows) {
    
    public void sort() {
        services().sort(Comparator.comparing(Service::name));

        externalEntities().sort(Comparator.comparing(ExternalEntity::name));

        informationFlows().sort(Comparator.comparing(InformationFlow::sender).thenComparing(InformationFlow::receiver));

        List<List<String>> allStereotypes = new ArrayList<>();
        allStereotypes.add(services().stream().flatMap(node -> node.stereotypes().stream()).collect(Collectors.toList()));
        allStereotypes.add(externalEntities().stream().flatMap(node -> node.stereotypes().stream()).collect(Collectors.toList()));
        allStereotypes.add(informationFlows().stream().flatMap(node -> node.stereotypes().stream()).collect(Collectors.toList()));
        for (List<String> stereotypes : allStereotypes) {
            Collections.sort(stereotypes);
        }
    }
}
