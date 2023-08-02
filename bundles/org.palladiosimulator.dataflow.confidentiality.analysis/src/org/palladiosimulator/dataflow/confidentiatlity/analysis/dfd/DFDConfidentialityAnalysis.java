package org.palladiosimulator.dataflow.confidentiatlity.analysis.dfd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.log4j.Level;
import org.palladiosimulator.dataflow.confidentiality.analysis.DataFlowConfidentialityAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd.DFDActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd.DFDActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.AbstractDFDActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.ActionSequence;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;
import mdpa.dfd.dataflowdiagram.DataFlowDiagram;

import mdpa.dfd.dataflowdiagram.Node;
import mdpa.dfd.datadictionary.Behaviour;
import mdpa.dfd.datadictionary.Label;
import mdpa.dfd.datadictionary.Assignment;
import mdpa.dfd.datadictionary.LabelType;

public class DFDConfidentialityAnalysis implements DataFlowConfidentialityAnalysis {
	private String pathToModel;
	DataFlowDiagram dfd;

	@Override
	public boolean initializeAnalysis() {
		this.dfd = DFDLoader.loadDFDModel(this.pathToModel);
		return true;
	}
	

	@Override
	public List<ActionSequence> findAllSequences() {
		Map<Node, List<Node>> graph = new HashMap<>();
		for (var dataFlow: this.dfd.getFlows()) {
			Node inputNode = dataFlow.getDestinationNode();
            Node outputNode = dataFlow.getDestinationNode();
            graph.putIfAbsent(inputNode, new ArrayList<>());
            graph.get(inputNode).add(outputNode);
		}
		
		List<ActionSequence> strands = new ArrayList<ActionSequence>();
		Set<Node> visited = new HashSet<>();
		
		for (Node node : this.dfd.getNodes()) {
            if (!visited.contains(node)) {
                List<Node> currentStrand = new ArrayList<Node>();
                depthFirstSearch(node, visited, currentStrand, graph);
                strands.add(convertNodeStrandToDFDActionSequence(currentStrand));
            }
        }
		
		return strands;
	}
	
	private DFDActionSequence convertNodeStrandToDFDActionSequence(List<Node> nodes) {
		List<AbstractActionSequenceElement<?>> actionSequence = new ArrayList<AbstractActionSequenceElement<?>>();
		var previousNode = nodes.get(0);
		for(var currentNode: nodes) {
			actionSequence.add(this.convertNodeToAbstractDFDActionSequenceElement(currentNode, previousNode));
			previousNode = currentNode;
		}
		return new DFDActionSequence(actionSequence);
	}
	
	private static void depthFirstSearch(Node node, Set<Node> visited, List<Node> currentStrand, Map<Node, List<Node>> graph) {
        visited.add(node);
        currentStrand.add(node);

        List<Node> neighbors = graph.getOrDefault(node, new ArrayList<>());
        for (Node neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                depthFirstSearch(neighbor, visited, currentStrand, graph);
            }
        }
    }
	
	private DFDActionSequenceElement convertNodeToAbstractDFDActionSequenceElement(Node node, Node previousNode) {
		List<DataFlowVariable> dataFlowVariables = new ArrayList<DataFlowVariable>();
		var behaviour = node.getBehaviour();
		for(var inputPin:  behaviour.getIn()) {
			dataFlowVariables.add(new DataFlowVariable(inputPin.getEntityName(), evaluateAssignment(previousNode)));
		}
		
		List<CharacteristicValue> nodeCharacteristics = new ArrayList<CharacteristicValue>();
		for(Assignment assignment: behaviour.getAssignment()) {
			nodeCharacteristics.add(new CharacteristicValue(/*TODO: LabelType)*/, new Literal() assignment.getLhsLabel());
			//Todo: LabelType auf das Enum von CharacteristicsType mappen
		}
		return new DFDActionSequenceElement(dataFlowVariables, nodeCharacteristics, node.getEntityName());
		
	}
	
	private List<CharacteristicValue> evaluateAssignment(Node node) {
		node.getBehaviour().getAssignment();
		
		return null;
	}
	
	private Literal mapLabelToLiteral(Label label) {
		return null;
	}

	@Override
	public List<ActionSequence> evaluateDataFlows(List<ActionSequence> sequences) {
		
		
		var actionSequences = new ArrayList<ActionSequence>();
		for(var node: dfd.getNodes()) {
			actionSequences.add();
		}
		
		return null;
	}

	@Override
	public List<AbstractActionSequenceElement<?>> queryDataFlow(ActionSequence sequence,
			Predicate<? super AbstractActionSequenceElement<?>> condition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLoggerLevel(Level level) {
		// TODO Auto-generated method stub
		
	}
	
	public String getPathToModel() {
		return this.pathToModel;
	}
	
	public void setPathToModel(String pathToModel) {
		this.pathToModel = pathToModel;
	}


}
