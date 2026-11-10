package org.dataflowanalysis.converter.plant2micro;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import org.dataflowanalysis.converter.ConverterModel;
import org.dataflowanalysis.converter.ModelType;

public class PlantConverterModel extends ConverterModel {
    private static final String FILE_EXTENSION = ".json";

    private final String filePath;
    private final String fileName;

    public PlantConverterModel(String filePath, String fileName) {
        super(ModelType.PLANT);
        this.filePath = filePath;
        this.fileName = fileName;
    }

    public PlantConverterModel(Scanner scanner) {
        super(ModelType.PLANT);
        String plantPath = this.getFilePath(scanner, FILE_EXTENSION);
        Path path = Paths.get(plantPath)
                .toAbsolutePath()
                .normalize();
        this.filePath = path.getParent()
                .toString();
        this.fileName = path.getFileName()
                .toString();
    }

    /**
     * Returns the file path to the Plant UML model folder
     * @return Returns the path to the folder containing the model files
     */
    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }
}
