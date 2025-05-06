package org.dataflowanalysis.analysis.tests.integration.dfd.simple;

import org.dataflowanalysis.analysis.dfd.simple.DFDSimpleTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.simple.DFDSimpleVertex;
import org.dataflowanalysis.analysis.tests.integration.dfd.util.DFDTestUtil;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DFDSimpleTransposeFlowGraphFinderTest {
	private DataFlowDiagram dataFlowDiagram;
	private DataDictionary dataDictionary;

	@BeforeEach
	public void init() {
		dataFlowDiagram = DFDTestUtil.createDataFlowDiagram();
		dataDictionary = DFDTestUtil.createDataDictionary();
	}
	
	/**
	 * Tests equivalence of copied TFG
	 */
	@Test
	public void testCopying() {
		Node a = DFDTestUtil.createNode("a", dataFlowDiagram, dataDictionary);
		Node b = DFDTestUtil.createNode("b", dataFlowDiagram, dataDictionary);
		
		DFDTestUtil.createFlow(a, b, null, null, "a2b_1");
		DFDTestUtil.createFlow(a, b, null, null, "a2b_2");
		
		DFDTestUtil.createAndAddLabelTypeAndLabel(dataDictionary, null, null);		
		
		datadictionaryFactory ddFactory = datadictionaryFactory.eINSTANCE;
		
		a.getBehavior().getOutPin().forEach(pin -> {
			DFDTestUtil.createAndAddAssignment(a, null, pin, dataDictionary.getLabelTypes().get(0).getLabel(), ddFactory.createTRUE(), Assignment.class);
		});
				
		var tfg = new DFDSimpleTransposeFlowGraphFinder(dataDictionary, dataFlowDiagram).findTransposeFlowGraphs().get(0);
		
		var copiedFlowGraph = tfg.copy();
		
		assert(tfg.getVertices().stream().map(DFDSimpleVertex.class::cast).allMatch(vertex -> {
			return copiedFlowGraph.getVertices().stream().map(DFDSimpleVertex.class::cast).anyMatch(copiedVertex -> vertex.equalsSemantically(copiedVertex));
		}));
	}
}
