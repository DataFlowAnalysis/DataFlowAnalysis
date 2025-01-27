package org.dataflowanalysis.analysis.tests.dfd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.analysis.tests.dfd.util.DFDTestUtil;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.junit.jupiter.api.Test;

import tools.mdsd.modelingfoundations.identifier.Entity;

public class DFDVertexTest {
	@Test
	public void testEvaluation() {
		DataFlowDiagram dataFlowDiagram = DFDTestUtil.createDataFlowDiagram();
		DataDictionary dataDictionary = DFDTestUtil.createDataDictionary();
		
		Map<String, Entity> mapNameToEntity = DFDTestUtil.createBasicDFDandDD(dataFlowDiagram, dataDictionary);
		
		Flow a2b = ((Flow)mapNameToEntity.get("a2b"));
		Flow b2c = ((Flow)mapNameToEntity.get("b2c"));
		Flow c2d = ((Flow)mapNameToEntity.get("c2d"));
		Flow d2e = ((Flow)mapNameToEntity.get("d2e"));		
		
		DFDVertex aVertex = new DFDVertex((Node)mapNameToEntity.get("a"), new HashMap<>(), Map.of(a2b.getSourcePin(), a2b));
		DFDVertex bVertex = new DFDVertex((Node)mapNameToEntity.get("b"), Map.of(a2b.getDestinationPin(), aVertex), Map.of(a2b.getDestinationPin(), a2b));
		DFDVertex cVertex = new DFDVertex((Node)mapNameToEntity.get("c"), Map.of(b2c.getDestinationPin(), bVertex), Map.of(b2c.getDestinationPin(), b2c));
		DFDVertex dVertex = new DFDVertex((Node)mapNameToEntity.get("d"), Map.of(c2d.getDestinationPin(), cVertex), Map.of(c2d.getDestinationPin(), c2d));
		DFDVertex eVertex = new DFDVertex((Node)mapNameToEntity.get("e"), Map.of(d2e.getDestinationPin(), dVertex), Map.of(d2e.getDestinationPin(), d2e));
		
		eVertex.evaluateDataFlow();
		
		Label label1 = (Label)mapNameToEntity.get("label1");
		Label label2 = (Label)mapNameToEntity.get("label2");
		
		assertEquals(DFDTestUtil.getAllOutgoingLabel(aVertex), List.of(label1, label2));
		assertEquals(DFDTestUtil.getAllIncomingLabel(bVertex), List.of(label1, label2));
		assertEquals(DFDTestUtil.getAllOutgoingLabel(bVertex),  List.of(label1));
		assertEquals(DFDTestUtil.getAllIncomingLabel(cVertex),  List.of(label1));
		assertEquals(DFDTestUtil.getAllOutgoingLabel(cVertex),  List.of(label2));
		assertEquals(DFDTestUtil.getAllIncomingLabel(dVertex), List.of(label2));
		assertEquals(DFDTestUtil.getAllOutgoingLabel(dVertex), List.of());
		assertEquals(DFDTestUtil.getAllIncomingLabel(eVertex), List.of());
		
		var tfg = new DFDTransposeFlowGraphFinder(dataDictionary, dataFlowDiagram).findTransposeFlowGraphs().get(0);
		tfg.evaluate();
		
		assert(tfg.getVertices().stream().map(DFDVertex.class::cast).allMatch(it -> {
			return List.of(aVertex, bVertex, cVertex, dVertex, eVertex).stream().anyMatch(vertex -> vertex.equals(it));
		}));
	}
}
