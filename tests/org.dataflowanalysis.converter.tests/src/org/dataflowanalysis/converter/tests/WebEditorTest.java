package org.dataflowanalysis.converter.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.dataflowanalysis.converter.dfd2web.DataFlowDiagramAndDictionary;
import org.dataflowanalysis.converter.dfd2web.DataFlowDiagramConverter;
import org.dataflowanalysis.converter.web2dfd.Web2DFDConverter;
import org.dataflowanalysis.converter.web2dfd.WebEditorConverterModel;
import org.dataflowanalysis.converter.web2dfd.model.WebEditorDfd;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;

public class WebEditorTest extends ConverterTest {
    private DataFlowDiagramConverter dfdConverter;
    private Web2DFDConverter webConverter;

    private final WebEditorConverterModel minimalWebDFD = new WebEditorConverterModel(Paths.get(TEST_JSONS, "minimal.json")
            .toString());
    private final String tempWebDFD = "test";
    private final String TESTS = "org.dataflowanalysis.converter.tests";

    @BeforeEach
    public void setup() {
        dfdConverter = new DataFlowDiagramConverter();
        webConverter = new Web2DFDConverter();
    }

    @Test
    @DisplayName("Test Web -> DFD -> Web")
    public void webToDfdToWeb() throws StreamReadException, DatabindException, IOException {
        DataFlowDiagramAndDictionary dfdBefore = webConverter.convert(minimalWebDFD);
        WebEditorDfd webAfter = dfdConverter.convert(dfdBefore)
                .getModel();
        WebEditorDfd webBefore = minimalWebDFD.getModel();

        webAfter.constraints().addAll(webBefore.constraints());

        webBefore.sort();
        webAfter.sort();

        assertEquals(webBefore, webAfter);

        checkBehaviorAndPinNames(dfdBefore);
    }

    @Test
    @DisplayName("Test storing and loading functionality")
    public void testStoreLoad() throws StreamReadException, DatabindException, IOException, StandaloneInitializationException {
        DataFlowDiagramAndDictionary completeBefore = webConverter.convert(minimalWebDFD);

        minimalWebDFD.save(".", tempWebDFD);
        completeBefore.save("./bin", tempWebDFD);

        WebEditorConverterModel webAfter = new WebEditorConverterModel(tempWebDFD + ".json");
        DataFlowDiagramAndDictionary completeAfter = new DataFlowDiagramAndDictionary("./bin/test.dataflowdiagram", "./bin/test.datadictionary");

        assertEquals(minimalWebDFD.getModel(), webAfter.getModel());
        assertEquals(completeBefore.dataFlowDiagram()
                .getNodes()
                .size(),
                completeAfter.dataFlowDiagram()
                        .getNodes()
                        .size());
        assertEquals(completeBefore.dataFlowDiagram()
                .getFlows()
                .size(),
                completeAfter.dataFlowDiagram()
                        .getFlows()
                        .size());

        for (int i = 0; i < completeBefore.dataFlowDiagram()
                .getNodes()
                .size(); i++) {
            assertEquals(completeBefore.dataFlowDiagram()
                    .getNodes()
                    .get(i)
                    .getBehavior()
                    .getEntityName(),
                    completeAfter.dataFlowDiagram()
                            .getNodes()
                            .get(i)
                            .getBehavior()
                            .getEntityName());
        }

        cleanup("bin" + File.separator + "test.dataflowdiagram");
        cleanup("bin" + File.separator + "test.datadictionary");
        cleanup(tempWebDFD);
    }

    private void checkBehaviorAndPinNames(DataFlowDiagramAndDictionary dfd) {
        for (Node node : dfd.dataFlowDiagram()
                .getNodes()) {
            var behaviour = node.getBehavior();
            assertEquals(node.getEntityName(), behaviour.getEntityName());

            for (Pin inPin : behaviour.getInPin()) {
                String flowName = "";
                int matches = 0;

                for (Flow flow : dfd.dataFlowDiagram()
                        .getFlows()) {
                    if (flow.getDestinationPin()
                            .equals(inPin)) {
                        flowName = flow.getEntityName();
                        matches++;
                    }
                }
                assertTrue(inPin.getEntityName()
                        .startsWith(node.getEntityName() + "_in"));
                if (matches < 2) {
                    assertEquals(inPin.getEntityName(), node.getEntityName() + "_in_" + flowName);
                }
            }

            for (Pin outPin : behaviour.getOutPin()) {
                String flowName = "";
                int matches = 0;

                for (Flow flow : dfd.dataFlowDiagram()
                        .getFlows()) {
                    if (flow.getSourcePin()
                            .equals(outPin)) {
                        flowName = flow.getEntityName();
                        matches++;
                    }
                }

                assertTrue(outPin.getEntityName()
                        .startsWith(node.getEntityName() + "_out"));
                if (matches < 2) {
                    assertEquals(outPin.getEntityName(), node.getEntityName() + "_out_" + flowName);
                }
            }
        }
    }
}
