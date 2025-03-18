package org.dataflowanalysis.converter.interactive;

public enum ModelType {
    PCM("Palladio Component Model"),
    DFD("Data Flow Diagram Model"),
    WEB_DFD("WebEditor Diagram"),
    MICRO("MicroSecEnd Model"),
    PLANT("PlantUML Model");

    private final String name;

    ModelType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
