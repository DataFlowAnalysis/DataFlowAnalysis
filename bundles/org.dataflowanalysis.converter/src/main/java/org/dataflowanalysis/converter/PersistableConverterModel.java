package org.dataflowanalysis.converter;

/**
 * Represents a {@link ConverterModel} that is able to save to disk
 */
public abstract class PersistableConverterModel extends ConverterModel {
    public PersistableConverterModel(ModelType modelType) {
        super(modelType);
    }

    public abstract void save(String filePath, String fileName);
}
