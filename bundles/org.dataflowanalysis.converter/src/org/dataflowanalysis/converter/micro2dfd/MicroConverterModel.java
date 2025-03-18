package org.dataflowanalysis.converter.micro2dfd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.dataflowanalysis.converter.ModelType;
import org.dataflowanalysis.converter.PersistableConverterModel;
import org.dataflowanalysis.converter.micro2dfd.model.MicroSecEnd;

public class MicroConverterModel extends PersistableConverterModel {
    private static final String FILE_EXTENSION = ".json";

    private final MicroSecEnd model;

    public MicroConverterModel(MicroSecEnd model) {
        super(ModelType.MICRO);
        this.model = model;
    }

    public MicroConverterModel(String filePath) {
        super(ModelType.MICRO);
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(filePath);
        try {
            this.model = objectMapper.readValue(file, MicroSecEnd.class);
        } catch (IOException e) {
            logger.error("Could not load MicroSecEnd:", e);
            throw new IllegalArgumentException(e);
        }
    }

    public MicroSecEnd getModel() {
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
            logger.error("Could not store micro:", e);
        }
    }
}
