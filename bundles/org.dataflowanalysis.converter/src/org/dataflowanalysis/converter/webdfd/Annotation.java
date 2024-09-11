package org.dataflowanalysis.converter.webdfd;



public record Annotation(String message, String item, String color) {
	public boolean equals(Annotation other) {
		return this.message.equals(other.message);
	}
}
