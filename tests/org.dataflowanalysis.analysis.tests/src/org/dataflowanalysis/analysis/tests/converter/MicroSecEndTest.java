package org.dataflowanalysis.analysis.tests.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.dataflowanalysis.analysis.converter.DataFlowDiagramAndDictionary;
import org.dataflowanalysis.analysis.converter.MicroSecEndProcessor;
import org.dataflowanalysis.analysis.converter.microsecend.ExternalEntity;
import org.dataflowanalysis.analysis.converter.microsecend.InformationFlow;
import org.dataflowanalysis.analysis.converter.microsecend.MicroSecEnd;
import org.dataflowanalysis.analysis.converter.microsecend.MicroSecEndProcess;
import org.dataflowanalysis.analysis.converter.microsecend.Service;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MicroSecEndTest extends ConverterTest{
    @Test
    @DisplayName("Test JSON -> Plant -> JSON")
    public void jsonToPlantToJson() throws StreamReadException, DatabindException, IOException {
        converter.runPythonScript(packagePath + "anilallewar.json", "txt", packagePath + "toPlant.txt");
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(packagePath + "anilallewar.json");
        MicroSecEnd microBefore = objectMapper.readValue(file, MicroSecEnd.class);

        converter.runPythonScript(packagePath + "toPlant.txt", "json", packagePath + "fromPlant.json");
        objectMapper = new ObjectMapper();
        file = new File(packagePath + "fromPlant.json");
        MicroSecEnd microAfter = objectMapper.readValue(file, MicroSecEnd.class);
        
        microBefore.sort();
        microAfter.sort();

        assertEquals(microBefore, microAfter);

        cleanup(packagePath + "toPlant.txt");
        cleanup(packagePath + "FromPlant.json");
    }
    
    @Test
    @DisplayName("Test Micro -> DFD")
    public void microToDfd() throws StreamReadException, DatabindException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(packagePath + "anilallewar.json");
        MicroSecEnd micro = objectMapper.readValue(file, MicroSecEnd.class);
        DataFlowDiagramAndDictionary complete = new MicroSecEndProcessor().processMicro(micro);

        DataFlowDiagram dfd = complete.dataFlowDiagram();

        assertEquals(micro.externalEntities().size() + micro.services().size(), dfd.getNodes().size());
        assertEquals(micro.informationFlows().size(), dfd.getFlows().size());

        for (Service service : micro.services()) {
            checkEntityName(service,dfd);
        }

        for (ExternalEntity ee : micro.externalEntities()) {
            checkEntityName(ee,dfd);
        }

        int match = 0;
        for (InformationFlow iflow : micro.informationFlows()) {
            for (Flow flow : dfd.getFlows()) {
                if (iflow.sender().equals(flow.getSourceNode().getEntityName())
                        && iflow.receiver().equals(flow.getDestinationNode().getEntityName())) {
                    Pin outpin = flow.getSourcePin();
                    List<Pin> outpins = flow.getSourceNode().getBehaviour().getOutPin();
                    assertTrue(outpins.contains(outpin));
                    Assignment assignment = (Assignment) flow.getSourceNode().getBehaviour().getAssignment().get(outpins.indexOf(outpin));
                    assertEquals(assignment.getOutputLabels().size(), flow.getSourceNode().getProperties().size());
                    match++;
                }
            }
        }
        assertEquals(match, micro.informationFlows().size());
    }
    
    private void checkEntityName(MicroSecEndProcess process, DataFlowDiagram dfd) {
        for(Node node : dfd.getNodes()) {
            if(process.name().equals(node.getEntityName())) {
                assertEquals(process.stereotypes().size(),node.getProperties().size());
                for(int i=0;i<process.stereotypes().size();i++) {
                    assertEquals(process.stereotypes().get(i),node.getProperties().get(i).getEntityName());
                }
            }
        }   
    }
}
