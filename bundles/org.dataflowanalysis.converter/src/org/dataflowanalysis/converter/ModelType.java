package org.dataflowanalysis.converter;

import java.util.Optional;

public enum ModelType {
    PCM("Palladio Component Model", "pcm"),
    DFD("Data Flow Diagram Model", "dfd"),
    WEB_DFD("WebEditor Diagram", "web"),
    MICRO("MicroSecEnd Model", "micro"),
    PLANT("PlantUML Model", "plant");

    private final String name;
    private final String abbreviation;

    ModelType(String name, String abbreviation) {
        this.name = name;
        this.abbreviation = abbreviation;
    }

    public String getName() {
        return name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public static Optional<ModelType> fromAbbreviation(String abbreviation) {
        for (ModelType modelType : ModelType.values()) {
            if (modelType.getAbbreviation().equalsIgnoreCase(abbreviation)) {
                return Optional.of(modelType);
            }
        }
        return Optional.empty();
    }
}
