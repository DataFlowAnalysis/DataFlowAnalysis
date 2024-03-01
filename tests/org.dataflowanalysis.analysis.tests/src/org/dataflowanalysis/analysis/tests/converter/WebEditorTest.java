package org.dataflowanalysis.analysis.tests.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.dataflowanalysis.analysis.converter.DataFlowDiagramAndDictionary;
import org.dataflowanalysis.analysis.converter.DataFlowDiagramConverter;
import org.dataflowanalysis.analysis.converter.webdfd.WebEditorDfd;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebEditorTest extends ConverterTest {
    private DataFlowDiagramConverter converter;

    private final String MINIMAL = Paths.get(packagePath, "minimal.json").toString();
    private final String TEST = Paths.get(packagePath, "test.json").toString();
    private final String MINIMALDFD = Paths.get(packagePath, "minimal.dataflowdiagram").toString();
    private final String MINIMALDD = Paths.get(packagePath, "minimal.datadictionary").toString();

    @BeforeEach
    public void setup() {
        converter = new DataFlowDiagramConverter();
    }

    @Test
    @DisplayName("Test Web -> DFD -> Web")
    public void webToDfdToWeb() throws StreamReadException, DatabindException, IOException {
        DataFlowDiagramAndDictionary dfdBefore = converter.webToDfd(MINIMAL);
        WebEditorDfd webAfter = converter.dfdToWeb(dfdBefore);

        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(MINIMAL);
        WebEditorDfd webBefore = objectMapper.readValue(file, WebEditorDfd.class);

        webBefore.sort();
        webAfter.sort();

        assertEquals(webBefore, webAfter);
    }

    @Test
    @DisplayName("Test storing and loading functionality")
    public void testStoreLoad() throws StreamReadException, DatabindException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(MINIMAL);

        WebEditorDfd webBefore = objectMapper.readValue(file, WebEditorDfd.class);
        DataFlowDiagramAndDictionary completeBefore = converter.webToDfd(webBefore);

        converter.store(webBefore, TEST);
        converter.store(completeBefore, MINIMAL);

        WebEditorDfd webAfter = converter.loadWeb(TEST).get();
        DataFlowDiagramAndDictionary completeAfter = converter.loadDFD(MINIMALDFD, MINIMALDD);

        assertEquals(webBefore, webAfter);
        assertNotNull(completeAfter);

        cleanup(MINIMALDFD);
        cleanup(MINIMALDD);
        cleanup(TEST);
    }
    
    @Test
    @DisplayName("Test manual conversion")
    public void testManual(){
        String dataflowdiagram = Paths.get("..", "org.dataflowanalysis.analysis.testmodels", "models", "OnlineShopDFD","onlineshop.dataflowdiagram").toString();
        String datadictionary = Paths.get("..", "org.dataflowanalysis.analysis.testmodels", "models", "OnlineShopDFD","onlineshop.datadictionary").toString();
        DataFlowDiagramAndDictionary manualDFD = converter.loadDFD(dataflowdiagram,datadictionary);
        
        DataFlowDiagramAndDictionary convertedDFD = converter.webToDfd(MINIMAL);
        
        assertEquals(manualDFD.dataFlowDiagram().getNodes().size(),convertedDFD.dataFlowDiagram().getNodes().size());
                
        List<String> nodeEntityNamesManual = manualDFD.dataFlowDiagram().getNodes().stream()
                .map(Node::getEntityName).collect(Collectors.toList());
        Collections.sort(nodeEntityNamesManual);
        
        List<String> nodeEntityNamesConverted = convertedDFD.dataFlowDiagram().getNodes().stream()
                .map(Node::getEntityName).collect(Collectors.toList());
        Collections.sort(nodeEntityNamesConverted);

        assertEquals(nodeEntityNamesManual,nodeEntityNamesConverted);
        
        assertEquals(manualDFD.dataFlowDiagram().getFlows().size(),convertedDFD.dataFlowDiagram().getFlows().size());
        
        assertTrue(manualDFD.dataFlowDiagram().getFlows().stream() // Stream over the first list
            .allMatch(flowA -> // Check if all elements of the first list
                    convertedDFD.dataFlowDiagram().getFlows().stream() // Stream over the second list
                    .anyMatch(flowB -> // Check if any element in the second list
                            flowA.getSourceNode().getEntityName().equals(flowB.getSourceNode().getEntityName()) && // matches the source name
                            flowA.getDestinationNode().getEntityName().equals(flowB.getDestinationNode().getEntityName()) // and destination name
                    )
            ));

    }
}
