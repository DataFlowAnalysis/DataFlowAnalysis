package org.dataflowanalysis.analysis.converter.webdfd;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public record Child(String text, List<WebLabel> labels, List<Port> ports, String id, String type, String sourceId, String targetId, List<Child> children) {

	public boolean equals(Child other) {
		if(!this.type.equals(other.type)) {
			return false;
		}
		else if(this.type.split(":")[0].equals("node")) {
			return this.text.equals(other.text) && this.id.equals(other.id) && this.children.equals(other.children) && this.labels.equals(other.labels) && this.ports.equals(other.ports);
		}
		else {
			return this.text.equals(other.text) && this.id.equals(other.id) && this.children.equals(other.children) && this.sourceId.equals(other.sourceId) && this.targetId.equals(other.targetId);

		}
	}
}


