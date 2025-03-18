package org.dataflowanalysis.converter.plant2micro;

import org.dataflowanalysis.converter.ConverterModel;
import org.dataflowanalysis.converter.ModelType;

public class PlantConverterModel extends ConverterModel {
    private final String filePath;
    private final String fileName;

    public PlantConverterModel(String filePath, String fileName) {
        super(ModelType.PLANT);
        this.filePath = filePath;
        this.fileName = fileName;
    }

    /**
     * Returns the file path to the Plant UML model folder
     * @return
     */
    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }
}
