package org.dataflowanalysis.converter;

import java.util.Optional;

/**
 * This enum represents all model types the converters may interact with. It must have a full name and abbreviation for
 * usage with the CLI
 */
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

    /**
     * Returns the correct model type for the given abbreviation
     * @param abbreviation Given abbreviation corresponding to a model type
     * @return Returns an optional containing the correct model type, if it exists
     */
    public static Optional<ModelType> fromAbbreviation(String abbreviation) {
        for (ModelType modelType : ModelType.values()) {
            if (modelType.getAbbreviation()
                    .equalsIgnoreCase(abbreviation)) {
                return Optional.of(modelType);
            }
        }
        return Optional.empty();
    }
}
