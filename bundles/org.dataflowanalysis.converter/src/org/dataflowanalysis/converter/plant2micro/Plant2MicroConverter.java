package org.dataflowanalysis.converter.plant2micro;

import java.io.IOException;
import java.util.Optional;
import org.dataflowanalysis.converter.Converter;
import org.dataflowanalysis.converter.ConverterModel;
import org.dataflowanalysis.converter.micro2dfd.MicroConverterModel;

public class Plant2MicroConverter extends Converter {
    @Override
    public MicroConverterModel convert(ConverterModel input) {
        Optional<PlantConverterModel> plantModel = input.toType(PlantConverterModel.class);
        if (plantModel.isEmpty()) {
            logger.error("Expected PlantConverterModel, but got: " + input.getClass()
                    .getSimpleName());
            throw new IllegalArgumentException("Invalid input for Model Conversion");
        }
        int exitCode = runPythonScript(plantModel.get()
                .getFilePath() + "/"
                + plantModel.get()
                        .getFileName(),
                "json", plantModel.get()
                        .getFileName() + ".json");
        if (exitCode != 0) {
            throw new IllegalStateException("Could not run plant2micro conversion");
        }
        return null;
    }

    // Tested with Python *3.11.5*, requires *argparse*, *ast* and *json* modules
    /**
     * Runs Python script for model conversion.
     * @param in Input file path.
     * @param format Desired output format.
     * @param out Output file path.
     * @return Exit code of the process (0 for success, -1 for error).
     */
    public int runPythonScript(String in, String format, String out) {
        String[] command = {"python3", "convert_model.py", in, format, "-op", out};

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process;
        try {
            process = processBuilder.start();
            return process.waitFor();
        } catch (IOException | InterruptedException e) {
            logger.error("Make sure python3 is installed and set in PATH", e);
        }
        return -1;
    }
}
