package org.dataflowanalysis.analysis.tests.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.dataflowanalysis.analysis.converter.DataFlowDiagramAndDictionary;
import org.dataflowanalysis.analysis.converter.DataFlowDiagramConverter;
import org.dataflowanalysis.analysis.converter.webdfd.WebEditorDfd;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebEditorTest extends ConverterTest {
    private DataFlowDiagramConverter converter;
    
    private final String MINIMAL = Paths.get(packagePath,"minimal.json").toString();

    @BeforeEach
    public void setup() {
        converter = new DataFlowDiagramConverter();
    }

    @Test
    @DisplayName("Test Web -> DFD -> Web")
    public void webToDfdToWeb() throws StreamReadException, DatabindException, IOException {
        DataFlowDiagramAndDictionary dfdBefore = converter.webToDfd(packagePath + "minimal");
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

        converter.store(webBefore, packagePath + "test");
        converter.store(completeBefore, packagePath + "minimal");

        WebEditorDfd webAfter = converter.loadWeb(packagePath + "test");
        DataFlowDiagramAndDictionary completeAfter = converter.loadDFD(packagePath + "minimal");

        assertEquals(webBefore, webAfter);
        assertNotNull(completeAfter);

        Paths.get(packagePath, "minimal.datadictionary").toFile().delete();
        Paths.get(packagePath, "minimal.dataflowdiagram").toFile().delete();
        Paths.get(packagePath, "test.json").toFile().delete();
    }
}
