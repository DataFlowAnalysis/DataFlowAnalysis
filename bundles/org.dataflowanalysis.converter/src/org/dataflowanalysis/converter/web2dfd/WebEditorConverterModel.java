package org.dataflowanalysis.converter.web2dfd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

import org.dataflowanalysis.converter.ModelType;
import org.dataflowanalysis.converter.PersistableConverterModel;
import org.dataflowanalysis.converter.web2dfd.model.WebEditorDfd;

public class WebEditorConverterModel extends PersistableConverterModel {
    private static final String FILE_EXTENSION = ".json";

    private final WebEditorDfd model;

    public WebEditorConverterModel(WebEditorDfd model) {
        super(ModelType.WEB_DFD);
        this.model = model;
    }

    /**
     * Loads a Web Editor Converter Model from a specified input file.
     * @param inputPath The path to the web dfd model that should be loaded
     */
    public WebEditorConverterModel(String inputPath) {
        super(ModelType.WEB_DFD);
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(inputPath);
        try {
            this.model = objectMapper.readValue(file, WebEditorDfd.class);
        } catch (IOException e) {
            logger.error("Could not load web dfd:", e);
            throw new IllegalArgumentException(e);
        }
    }

    public WebEditorConverterModel(Scanner scanner) {
        super(ModelType.WEB_DFD);
        String inputPath = this.getFilePath(scanner, "json");

        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(inputPath);
        try {
            this.model = objectMapper.readValue(file, WebEditorDfd.class);
        } catch (IOException e) {
            logger.error("Could not load web dfd:", e);
            throw new IllegalArgumentException(e);
        }
    }

    public WebEditorDfd getModel() {
        return model;
    }

    @Override
    public void save(String filePath, String fileName) {
        if (!fileName.endsWith(FILE_EXTENSION))
            fileName = fileName + FILE_EXTENSION;

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        Path outputFilePath = Path.of(filePath, fileName)
                .toAbsolutePath()
                .normalize();
        try {
            objectMapper.writeValue(new File(outputFilePath.toString()), this.model);
        } catch (IOException e) {
            logger.error("Could not store web dfd:", e);
        }
    }
}
