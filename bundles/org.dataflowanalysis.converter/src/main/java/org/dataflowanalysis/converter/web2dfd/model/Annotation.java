package org.dataflowanalysis.converter.web2dfd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Annotation(String message, String icon, String color, int tfg) {

}
