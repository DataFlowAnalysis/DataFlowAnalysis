package org.dataflowanalysis.converter.micro2dfd;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import org.dataflowanalysis.converter.ConverterModel;
import org.dataflowanalysis.converter.interactive.ModelType;
import org.dataflowanalysis.converter.micro2dfd.model.MicroSecEnd;

public class MicroConverterModel extends ConverterModel {
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

    }
}
