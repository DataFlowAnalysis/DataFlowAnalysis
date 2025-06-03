package org.dataflowanalysis.examplemodels.results.dfd;

import java.nio.file.Paths;
import org.dataflowanalysis.examplemodels.results.ExampleModelResult;

public interface DFDExampleModelResult extends ExampleModelResult {
    default String getDataFlowDiagram() {
        return Paths.get(this.getBaseFolderName(), "dfd", this.getModelName(), String.format("%s.dataflowdiagram", this.getFileName()))
                .toString();
    }

    default String getDataDictionary() {
        return Paths.get(this.getBaseFolderName(), "dfd", this.getModelName(), String.format("%s.datadictionary", this.getFileName()))
                .toString();
    }
}
