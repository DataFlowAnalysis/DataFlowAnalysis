package org.dataflowanalysis.converter.plant2micro;

import org.dataflowanalysis.converter.ConverterModel;
import org.dataflowanalysis.converter.ModelType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class PlantConverterModel extends ConverterModel {
    private final String filePath;
    private final String fileName;

    public PlantConverterModel(String filePath, String fileName) {
        super(ModelType.PLANT);
        this.filePath = filePath;
        this.fileName = fileName;
    }

    public PlantConverterModel(Scanner scanner) {
        super(ModelType.PLANT);
        String plantPath = this.getFilePath(scanner, "json");
        Path path = Paths.get(plantPath).toAbsolutePath().normalize();
        this.filePath = path.getParent().toString();
        this.fileName = path.getFileName().toString();
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
