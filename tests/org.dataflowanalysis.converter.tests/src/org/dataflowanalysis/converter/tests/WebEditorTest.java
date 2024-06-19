package org.dataflowanalysis.converter.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.dataflowanalysis.converter.DataFlowDiagramAndDictionary;
import org.dataflowanalysis.converter.DataFlowDiagramConverter;
import org.dataflowanalysis.examplemodels.Activator;
import org.dataflowanalysis.converter.webdfd.WebEditorDfd;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;

public class WebEditorTest extends ConverterTest {
    private DataFlowDiagramConverter converter;

    private final String minimalWebDFD = Paths.get(TEST_JSONS, "minimal.json")
            .toString();
    private final String tempWebDFD = "test.json";
    private final String TESTS = "org.dataflowanalysis.converter.tests";

    @BeforeEach
    public void setup() {
        converter = new DataFlowDiagramConverter();
    }

    @Test
    @DisplayName("Test Web -> DFD -> Web")
    public void webToDfdToWeb() throws StreamReadException, DatabindException, IOException {
        DataFlowDiagramAndDictionary dfdBefore = converter.webToDfd(minimalWebDFD);
        WebEditorDfd webAfter = converter.dfdToWeb(dfdBefore);

        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(minimalWebDFD);
        WebEditorDfd webBefore = objectMapper.readValue(file, WebEditorDfd.class);

        webBefore.sort();
        webAfter.sort();

        assertEquals(webBefore, webAfter);

        checkBehaviorAndPinNames(dfdBefore);
    }

    @Test
    // @Disabled("Does not run on the CICD pipeline and has no priority right now")
    @DisplayName("Test storing and loading functionality")
    public void testStoreLoad() throws StreamReadException, DatabindException, IOException, StandaloneInitializationException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(minimalWebDFD);

        WebEditorDfd webBefore = objectMapper.readValue(file, WebEditorDfd.class);
        DataFlowDiagramAndDictionary completeBefore = converter.webToDfd(webBefore);

        converter.storeWeb(webBefore, tempWebDFD);
        converter.storeDFD(completeBefore, "bin" + File.separator + tempWebDFD);

        WebEditorDfd webAfter = converter.loadWeb(tempWebDFD)
                .get();
        DataFlowDiagramAndDictionary completeAfter = converter.loadDFD(TESTS, "bin/test.dataflowdiagram", "bin/test.datadictionary",
                org.dataflowanalysis.converter.tests.Activator.class);

        assertEquals(webBefore, webAfter);
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
                    .getBehaviour()
                    .getEntityName(),
                    completeAfter.dataFlowDiagram()
                            .getNodes()
                            .get(i)
                            .getBehaviour()
                            .getEntityName());
        }

        cleanup("bin" + File.separator + "test.dataflowdiagram");
        cleanup("bin" + File.separator + "test.datadictionary");
        cleanup(tempWebDFD);
    }

    @Test
    @DisplayName("Test manual conversion")
    public void testManual() throws StandaloneInitializationException {
        String dataflowdiagram = Paths.get("models", "OnlineShopDFD", "onlineshop.dataflowdiagram")
                .toString();
        String datadictionary = Paths.get("models", "OnlineShopDFD", "onlineshop.datadictionary")
                .toString();
        DataFlowDiagramAndDictionary manualDFD = converter.loadDFD(TEST_MODELS, dataflowdiagram, datadictionary,
                Activator.class);

        DataFlowDiagramAndDictionary convertedDFD = converter.webToDfd(minimalWebDFD);

        assertEquals(manualDFD.dataFlowDiagram()
                .getNodes()
                .size(),
                convertedDFD.dataFlowDiagram()
                        .getNodes()
                        .size());

        List<String> nodeEntityNamesManual = manualDFD.dataFlowDiagram()
                .getNodes()
                .stream()
                .map(Node::getEntityName)
                .collect(Collectors.toList());
        Collections.sort(nodeEntityNamesManual);

        List<String> nodeEntityNamesConverted = convertedDFD.dataFlowDiagram()
                .getNodes()
                .stream()
                .map(Node::getEntityName)
                .collect(Collectors.toList());
        Collections.sort(nodeEntityNamesConverted);

        assertEquals(nodeEntityNamesManual, nodeEntityNamesConverted);

        assertEquals(manualDFD.dataFlowDiagram()
                .getFlows()
                .size(),
                convertedDFD.dataFlowDiagram()
                        .getFlows()
                        .size());

        assertTrue(manualDFD.dataFlowDiagram()
                .getFlows()
                .stream()
                .allMatch(flowA -> convertedDFD.dataFlowDiagram()
                        .getFlows()
                        .stream()
                        .anyMatch(flowB -> flowA.getSourceNode()
                                .getEntityName()
                                .equals(flowB.getSourceNode()
                                        .getEntityName())
                                && flowA.getDestinationNode()
                                        .getEntityName()
                                        .equals(flowB.getDestinationNode()
                                                .getEntityName()))));

        checkBehaviorAndPinNames(manualDFD);

    }

    private void checkBehaviorAndPinNames(DataFlowDiagramAndDictionary dfd) {
        for (Node node : dfd.dataFlowDiagram()
                .getNodes()) {
            var behaviour = node.getBehaviour();
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
