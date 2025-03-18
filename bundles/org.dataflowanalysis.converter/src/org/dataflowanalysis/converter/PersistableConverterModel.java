package org.dataflowanalysis.converter;

public abstract class PersistableConverterModel extends ConverterModel{
    public PersistableConverterModel(ModelType modelType) {
        super(modelType);
    }

    public abstract void save(String filePath, String fileName);
}
