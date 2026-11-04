package org.dataflowanalysis.analysis.tests.integration.dsl.advanced;

import static org.dataflowanalysis.analysis.tests.integration.AnalysisUtils.TEST_MODEL_PROJECT_NAME;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.dataflowanalysis.examplemodels.Activator;
import org.junit.jupiter.params.provider.Arguments;

/**
 * Base class for advanced {@link AnalysisConstraint} DSL tests, providing the shared constraint lookup helper and the
 * model evaluation helpers for the example models used by its subclasses.
 */
abstract class AbstractAdvancedDSLTest {
    private final Path dataFlowDiagramPath;
    private final Path dataDictionaryPath;

    public AbstractAdvancedDSLTest(Path dataFlowDiagramPath, Path dataDictionaryPath) {
        this.dataFlowDiagramPath = dataFlowDiagramPath;
        this.dataDictionaryPath = dataDictionaryPath;
    }

    protected String getConstraintAtIndex(Stream<Arguments> constraints, int index) {
        return (String) constraints.toList()
                .get(index)
                .get()[0];
    }

    protected List<DSLResult> evaluateModel(String dslString) {
        ParseResult<? extends AnalysisConstraint> constraint = AnalysisConstraint.fromString(new StringView(dslString));
        if (constraint.failed()) {
            fail(constraint.getError());
        }

        var analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(dataFlowDiagramPath.toString())
                .useDataDictionary(dataDictionaryPath.toString())
                .build();
        analysis.initializeAnalysis();
        var flowGraphCollection = analysis.findFlowGraphs();
        flowGraphCollection.evaluate();

        return constraint.getResult()
                .findViolations(flowGraphCollection);
    }
}
