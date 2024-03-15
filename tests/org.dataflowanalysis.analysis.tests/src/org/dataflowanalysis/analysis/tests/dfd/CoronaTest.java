package org.dataflowanalysis.analysis.tests.dfd;

import java.nio.file.Paths;

import org.dataflowanalysis.analysis.converter.*;
import org.dataflowanalysis.analysis.testmodels.Activator;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CoronaTest {
    @Test
    @DisplayName("Test Palladio to DFD")
    public void palladioToDfd() {
        String modelLocation = "org.dataflowanalysis.analysis.testmodels";

        String inputModel = "CoronaWarnApp";
        String inputFile = "default";

        final var usageModelPath = Paths.get("models", inputModel, inputFile + ".usagemodel").toString();
        final var allocationPath = Paths.get("models", inputModel, inputFile + ".allocation").toString();
        final var nodeCharPath = Paths.get("models", inputModel, inputFile + ".nodecharacteristics").toString();

        var complete = new PCMConverter().pcmToDFD(modelLocation,usageModelPath,allocationPath,nodeCharPath,Activator.class);

        var dfdConverter = new DataFlowDiagramConverter();
        var web = dfdConverter.dfdToWeb(complete);
        dfdConverter.storeWeb(web, "cwa.json");
    }

}
