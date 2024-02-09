package org.dataflowanalysis.analysis.converter.webdfd;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public record Port(String behavior, String id, String type, List<Object> children) {
	
	public boolean equals(Port other) {
		if(!this.type.equals(other.type)) {
			return false;
		}
		else if(this.type.equals("port:dfd-output")) {
			return this.id.equals(other.id) && this.behavior.equals(other.behavior) && this.children.equals(other.children);
		}
		else {
			return this.id.equals(other.id) && this.children.equals(other.children);
		}
	}
}
