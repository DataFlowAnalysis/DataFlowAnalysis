package org.dataflowanalysis.analysis.tests.dfd.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dataflowanalysis.analysis.dfd.simple.DFDSimpleTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.simple.DFDSimpleVertex;
import org.dataflowanalysis.analysis.tests.dfd.util.DFDTestUtil;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tools.mdsd.modelingfoundations.identifier.Entity;

public class DFDSimpleVertexTest {
	private DataFlowDiagram dataFlowDiagram;
	private DataDictionary dataDictionary;

	@BeforeEach
	public void init() {
		dataFlowDiagram = DFDTestUtil.createDataFlowDiagram();
		dataDictionary = DFDTestUtil.createDataDictionary();
	}
	
	@Test
	public void testEvaluation() {		
		Map<String, Entity> mapNameToEntity = DFDTestUtil.createBasicDFDandDD(dataFlowDiagram, dataDictionary);
		
		Flow a2b = ((Flow)mapNameToEntity.get("a2b"));
		Flow b2c = ((Flow)mapNameToEntity.get("b2c"));
		Flow c2d = ((Flow)mapNameToEntity.get("c2d"));
		Flow d2e = ((Flow)mapNameToEntity.get("d2e"));		
		
		DFDSimpleVertex aVertex = new DFDSimpleVertex((Node)mapNameToEntity.get("a"), new HashSet<>(), Map.of(a2b.getSourcePin(), a2b));
		DFDSimpleVertex bVertex = new DFDSimpleVertex((Node)mapNameToEntity.get("b"), Set.of(aVertex), Map.of(a2b.getDestinationPin(), a2b, b2c.getSourcePin(), b2c));
		DFDSimpleVertex cVertex = new DFDSimpleVertex((Node)mapNameToEntity.get("c"), Set.of(bVertex), Map.of(b2c.getDestinationPin(), b2c, c2d.getSourcePin(), c2d));
		DFDSimpleVertex dVertex = new DFDSimpleVertex((Node)mapNameToEntity.get("d"), Set.of(cVertex), Map.of(c2d.getDestinationPin(), c2d, d2e.getSourcePin(), d2e));
		DFDSimpleVertex eVertex = new DFDSimpleVertex((Node)mapNameToEntity.get("e"), Set.of(dVertex), Map.of(d2e.getDestinationPin(), d2e));
		
		eVertex.evaluateDataFlow();
		
		Label label1 = (Label)mapNameToEntity.get("label1");
		Label label2 = (Label)mapNameToEntity.get("label2");
		
		assertEquals(DFDTestUtil.getAllOutgoingLabel(aVertex), List.of(label1, label2));
		assertEquals(DFDTestUtil.getAllIncomingLabel(bVertex), List.of(label1, label2));
		assertEquals(DFDTestUtil.getAllOutgoingLabel(bVertex), List.of(label1));
		assertEquals(DFDTestUtil.getAllIncomingLabel(cVertex), List.of(label1));
		assertEquals(DFDTestUtil.getAllOutgoingLabel(cVertex), List.of(label2));
		assertEquals(DFDTestUtil.getAllIncomingLabel(dVertex), List.of(label2));
		assertEquals(DFDTestUtil.getAllOutgoingLabel(dVertex), List.of());
		assertEquals(DFDTestUtil.getAllIncomingLabel(eVertex), List.of());
		
		var tfg = new DFDSimpleTransposeFlowGraphFinder(dataDictionary, dataFlowDiagram).findTransposeFlowGraphs().get(0);
		tfg.evaluate();
		
		assert(tfg.getVertices().stream().map(DFDSimpleVertex.class::cast).allMatch(it -> {
			return List.of(aVertex, bVertex, cVertex, dVertex, eVertex).stream().anyMatch(vertex -> vertex.equalsSemantically(it));
		}));
	}
	
	@Test
	public void testToString() {
		Node a = DFDTestUtil.createNode("a", dataFlowDiagram, dataDictionary);
		DFDSimpleVertex vertex = new DFDSimpleVertex(a, new HashSet<>(), new HashMap<>());
		assertEquals(vertex.getName(), "a");
		assertEquals(vertex.toString(), "(a, " + a.getId() + ")");
	}
}
