package org.dataflowanalysis.converter.task;

public enum ConversionOrigin {
    PCM("Palladio Component Model"),
    DFD("Data Flow Diagram Model"),
    WEB_DFD("WebEditor Diagram"),
    MICRO("MicroSecEnd Model");


    private String name;

    ConversionOrigin(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
