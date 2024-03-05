package org.dataflowanalysis.analysis.converter.microsecend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a MicroSecEnd model consisting of services, external entities, and information flows.
 * @param services A list of {@link Service}
 * @param externalEntities A list of {@link ExternalEntity}
 * @param informationFlows A list of {@link InformationFlow}
 */
public record MicroSecEnd(List<Service> services,

        @JsonProperty("external_entities") List<ExternalEntity> externalEntities,

        @JsonProperty("information_flows") List<InformationFlow> informationFlows) {

    /**
     * Sorts the components of the MicroSecEnd model based on predefined criteria. Services and external entities are sorted
     * alphabetically by their names. Information flows are sorted first by the sender's name and then by the receiver's
     * name. Additionally, stereotypes within services, external entities, and information flows are also sorted
     * alphabetically.
     */
    public void sort() {
        services().sort(Comparator.comparing(Service::name));

        externalEntities().sort(Comparator.comparing(ExternalEntity::name));

        informationFlows().sort(Comparator.comparing(InformationFlow::sender).thenComparing(InformationFlow::receiver));

        List<List<String>> allStereotypes = new ArrayList<>();
        allStereotypes.add(services().stream().flatMap(node -> node.stereotypes().stream()).collect(Collectors.toList()));
        allStereotypes.add(externalEntities().stream().flatMap(node -> node.stereotypes().stream()).collect(Collectors.toList()));
        allStereotypes.add(informationFlows().stream().flatMap(node -> node.stereotypes().stream()).collect(Collectors.toList()));
        allStereotypes.forEach(stereotype -> Collections.sort(stereotype));
    }
}
