package org.dataflowanalysis.examplemodels.results;

import java.util.List;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.examplemodels.Activator;
import org.eclipse.core.runtime.Plugin;

public interface ExampleModelResult {
    default String getModelProjectName() {
        return "org.dataflowanalysis.examplemodels";
    }

    default Class<? extends Plugin> getPluginActivator() {
        return Activator.class;
    }

    default String getBaseFolderName() {
        return "models";
    }

    default String getFileName() {
        return "default";
    }

    String getModelName();

    List<AnalysisConstraint> getDSLConstraints();

    List<ExpectedViolation> getExpectedViolations();
}
