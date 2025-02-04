package org.dataflowanalysis.analysis.tests.dfd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.analysis.dfd.resource.DFDModelResourceProvider;
import org.dataflowanalysis.analysis.tests.dfd.util.DFDTestUtil;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.ForwardingAssignment;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.SetAssignment;
import org.dataflowanalysis.dfd.datadictionary.UnsetAssignment;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.mdsd.modelingfoundations.identifier.Entity;

public class AssignmentsTest {
    private DataFlowDiagram dataFlowDiagram;
    private DataDictionary dataDictionary;

    @BeforeEach
    public void init() {    
    	dataFlowDiagram = DFDTestUtil.createDataFlowDiagram();
        dataDictionary = DFDTestUtil.createDataDictionary();

        LabelType type = datadictionaryFactory.eINSTANCE.createLabelType();
        type.setEntityName("type");
        Label label = datadictionaryFactory.eINSTANCE.createLabel();
        label.setEntityName("value");
        type.getLabel()
                .add(label);
        dataDictionary.getLabelTypes()
                .add(type);
    }

    @Test
    public void testTFGBuildingWithSetAssignments() {
    	//Test whether Set Assignment starts TFG of 2 Nodes
    	Node a = DFDTestUtil.createNode("a", dataFlowDiagram, dataDictionary);
    	Node b = DFDTestUtil.createNode("b", dataFlowDiagram, dataDictionary);
    	
    	DFDTestUtil.createFlow(a, b, null, null, "a2b");
    	DFDTestUtil.createAndAddLabelTypeAndLabel(dataDictionary, "type", "value");
    	DFDTestUtil.createAndAddAssignment(a, null, null, dataDictionary.getLabelTypes().get(0).getLabel(), null, SetAssignment.class);
    	
    	var analysis = new DFDDataFlowAnalysisBuilder().standalone().useCustomResourceProvider(new DFDModelResourceProvider(dataDictionary, dataFlowDiagram)).build();
    	var tfg = analysis.findFlowGraphs();
    	tfg.evaluate();
    	
    	assertEquals(tfg.getTransposeFlowGraphs().size(), 1);
    	assertEquals(tfg.getTransposeFlowGraphs().get(0).getVertices().size(), 2);
    	
    	//Test whether new node with set assignments creates unconnected TFG
    	Node c = DFDTestUtil.createNode("c", dataFlowDiagram, dataDictionary);
    	DFDTestUtil.createFlow(c, a, null, null, "c2a");
    	
    	DFDTestUtil.createAndAddAssignment(c, null, null, dataDictionary.getLabelTypes().get(0).getLabel(), null, SetAssignment.class);
    	
    	analysis = new DFDDataFlowAnalysisBuilder().standalone().useCustomResourceProvider(new DFDModelResourceProvider(dataDictionary, dataFlowDiagram)).build();
    	tfg = analysis.findFlowGraphs();
    	tfg.evaluate();
    	
    	assertEquals(tfg.getTransposeFlowGraphs().size(), 2);    
    	
    	//Tests whether assignment with input Pins connects the 2 tfgs
    	DFDTestUtil.createAndAddAssignment(a, null, null, null, null, ForwardingAssignment.class);
    	
    	analysis = new DFDDataFlowAnalysisBuilder().standalone().useCustomResourceProvider(new DFDModelResourceProvider(dataDictionary, dataFlowDiagram)).build();
    	tfg = analysis.findFlowGraphs();
    	tfg.evaluate();
    	
    	assertEquals(tfg.getTransposeFlowGraphs().size(), 1);
    }

    @Test
    public void testSetAndUnsetBehavior() {
    	//Test whether Set Assignment sets Label
    	Node a = DFDTestUtil.createNode("a", dataFlowDiagram, dataDictionary);
    	Node b = DFDTestUtil.createNode("b", dataFlowDiagram, dataDictionary);
    	
    	DFDTestUtil.createFlow(a, b, null, null, "a2b");
    	DFDTestUtil.createAndAddLabelTypeAndLabel(dataDictionary, "type", "value");
    	DFDTestUtil.createAndAddAssignment(a, null, null, dataDictionary.getLabelTypes().get(0).getLabel(), null, SetAssignment.class);
    	
    	var analysis = new DFDDataFlowAnalysisBuilder().standalone().useCustomResourceProvider(new DFDModelResourceProvider(dataDictionary, dataFlowDiagram)).build();
    	var tfg = analysis.findFlowGraphs();
    	tfg.evaluate();
    	
    	tfg.getTransposeFlowGraphs().forEach(fg -> {
    		fg.getVertices().forEach(vertex -> {
    			if (((Entity)vertex.getReferencedElement()).getEntityName().equals("a")) {
    				assertEquals(getAllCharacteristicValues((DFDVertex)vertex).size(), 1);
    			}
    		});
    	});
    	
    	//Test whether Unset Assignment removes Label
    	DFDTestUtil.createAndAddAssignment(a, null, null, dataDictionary.getLabelTypes().get(0).getLabel(), null, UnsetAssignment.class);
    	
    	analysis = new DFDDataFlowAnalysisBuilder().standalone().useCustomResourceProvider(new DFDModelResourceProvider(dataDictionary, dataFlowDiagram)).build();
    	tfg = analysis.findFlowGraphs();
    	tfg.evaluate();
    	
    	tfg.getTransposeFlowGraphs().forEach(fg -> {
    		fg.getVertices().forEach(vertex -> {
    			if (((Entity)vertex.getReferencedElement()).getEntityName().equals("a")) {
    				assertEquals(getAllCharacteristicValues((DFDVertex)vertex).size(), 0);
    			}
    		});
    	});
    	
    	//Test Whether the same works for other assignments
    	UnsetAssignment unsetAssignment = a.getBehavior().getAssignment().stream()
    			.filter(UnsetAssignment.class::isInstance)
    			.map(UnsetAssignment.class::cast)
    			.findAny()
    			.orElseThrow();
    	a.getBehavior().getAssignment().remove(unsetAssignment);
    	
    	Node c = DFDTestUtil.createNode("c", dataFlowDiagram, dataDictionary);
    	DFDTestUtil.createFlow(b, c, null, null, "b2c");
    	
    	DFDTestUtil.createAndAddAssignment(b, null, null, null, null, ForwardingAssignment.class);
    	
    	DFDTestUtil.createAndAddAssignment(b, null, null, dataDictionary.getLabelTypes().get(0).getLabel(), null, UnsetAssignment.class);
    	
    	analysis = new DFDDataFlowAnalysisBuilder().standalone().useCustomResourceProvider(new DFDModelResourceProvider(dataDictionary, dataFlowDiagram)).build();
    	tfg = analysis.findFlowGraphs();
    	tfg.evaluate();
    	
    	tfg.getTransposeFlowGraphs().forEach(fg -> {
    		fg.getVertices().forEach(vertex -> {
    			if (((Entity)vertex.getReferencedElement()).getEntityName().equals("b")) {
    				assertEquals(getAllCharacteristicValues((DFDVertex)vertex).size(), 0);
    			}
    		});
    	});
    }

    private List<CharacteristicValue> getAllCharacteristicValues(DFDVertex vertex) {
        return vertex.getAllOutgoingDataCharacteristics()
                .stream()
                .flatMap(it -> it.getAllCharacteristics()
                        .stream())
                .collect(Collectors.toList());
    }
}
