package org.dataflowanalysis.analysis.converter;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.core.*;
import org.dataflowanalysis.analysis.flowgraph.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.flowgraph.AbstractVertex;
import org.dataflowanalysis.analysis.flowgraph.FlowGraph;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.flowgraph.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.flowgraph.seff.*;
import org.dataflowanalysis.analysis.pcm.flowgraph.user.*;
import org.dataflowanalysis.analysis.testmodels.Activator;
import org.palladiosimulator.pcm.core.entity.Entity;

import org.dataflowanalysis.dfd.datadictionary.*;
import org.dataflowanalysis.dfd.dataflowdiagram.*;

/**
 * Converts Palladio models to the data flow diagram and dictionary representation. Inherits from {@link Converter} to
 * utilize shared conversion logic while providing specific functionality for handling Palladio models.
 */
public class PCMConverter extends Converter {

    private final Map<Entity, Node> dfdNodeMap = new HashMap<>();
    private final DataDictionary dataDictionary = datadictionaryFactory.eINSTANCE.createDataDictionary();
    private final DataFlowDiagram dataFlowDiagram = dataflowdiagramFactory.eINSTANCE.createDataFlowDiagram();

    /**
     * Converts a PCM model into a DataFlowDiagramAndDictionary object.
     * @param inputModel Name of the model folder.
     * @param inputFile Name of the model file.
     * @param modelLocation Location of the model folder.
     * @return DataFlowDiagramAndDictionary object representing the converted Palladio model.
     */
    public DataFlowDiagramAndDictionary pcmToDFD(String inputModel, String inputFile, String modelLocation) {
        final var usageModelPath = Paths.get("models", inputModel, inputFile + ".usagemodel").toString();
        final var allocationPath = Paths.get("models", inputModel, inputFile + ".allocation").toString();
        final var nodeCharPath = Paths.get("models", inputModel, inputFile + ".nodecharacteristics").toString();

        DataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone().modelProjectName(modelLocation)
                .usePluginActivator(Activator.class).useUsageModel(usageModelPath).useAllocationModel(allocationPath)
                .useNodeCharacteristicsModel(nodeCharPath).build();

        analysis.initializeAnalysis();
        var flowGraph = analysis.findFlowGraph();
        var propagationResult = analysis.evaluateFlowGraph(flowGraph);

        return processPalladio(propagationResult);
    }

    private DataFlowDiagramAndDictionary processPalladio(FlowGraph flowGraph) {
        for (AbstractPartialFlowGraph aPFG : flowGraph.getPartialFlowGraphs()) {
            Node previousNode = null;
            for (AbstractVertex<?> abstractVertex : aPFG.getVertices()) {
                if (abstractVertex instanceof AbstractPCMVertex) {
                    previousNode = processAbstractPCMVertex((AbstractPCMVertex<?>) abstractVertex, previousNode);
                }
            }
        }
        return new DataFlowDiagramAndDictionary(dataFlowDiagram, dataDictionary);
    }

    private Node processAbstractPCMVertex(AbstractPCMVertex<? extends Entity> pcmVertex, Node previousDFDNode) {
        Node dfdNode = getDFDNode(pcmVertex);

        createFlowBetweenPreviousAndCurrentNode(previousDFDNode, dfdNode, pcmVertex);

        return dfdNode;
    }

    private void createFlowBetweenPreviousAndCurrentNode(Node source, Node dest, AbstractPCMVertex<? extends Entity> pcmVertex) {
        if (source == null || dest == null) {
            return;
        }
        List<DataFlowVariable> flowVariables = pcmVertex.getAllDataFlowVariables();
        for (DataFlowVariable flowVariable : flowVariables) {
            String flowName = flowVariable.variableName();

            dataFlowDiagram.getFlows().stream().filter(f -> f.getSourceNode().equals(source)).filter(f -> f.getDestinationNode().equals(dest))
                    .filter(f -> f.getEntityName().equals(flowName)).findFirst().orElse(createFlow(source, dest, flowName));
        }
    }

    private Flow createFlow(Node source, Node dest, String flowName) {
        Flow newFlow = dataflowdiagramFactory.eINSTANCE.createFlow();
        newFlow.setSourceNode(source);
        newFlow.setDestinationNode(dest);
        newFlow.setEntityName(flowName);
        Pin sourceOutPin = findOutputPin(source, flowName);
        Pin destInPin = findInputPin(dest, flowName);
        newFlow.setSourcePin(sourceOutPin);
        newFlow.setDestinationPin(destInPin);

        ForwardingAssignment forwarding = datadictionaryFactory.eINSTANCE.createForwardingAssignment();
        forwarding.setOutputPin(sourceOutPin);
        source.getBehaviour().getAssignment().add(forwarding);

        this.dataFlowDiagram.getFlows().add(newFlow);
        return newFlow;
    }

    // A pin is equivalent if the same parameters are passed
    private Pin findOutputPin(Node source, String parameters) {
        return source.getBehaviour().getOutPin().stream().filter(p -> p.getEntityName().equals(parameters)).findAny()
                .orElse(createPin(source, parameters, false));
    }

    // A pin is equivalent if the same parameters are passed
    private Pin findInputPin(Node dest, String parameters) {
        return dest.getBehaviour().getInPin().stream().filter(p -> p.getEntityName().equals(parameters)).findAny()
                .orElse(createPin(dest, parameters, true));
    }

    private Pin createPin(Node node, String parameters, boolean isInPin) {
        Pin pin = datadictionaryFactory.eINSTANCE.createPin();
        pin.setEntityName(parameters);
        if (isInPin) {
            node.getBehaviour().getInPin().add(pin);
        } else {
            node.getBehaviour().getOutPin().add(pin);
        }
        return pin;
    }

    private Node getDFDNode(AbstractPCMVertex<? extends Entity> pcmVertex) {
        Node dfdNode = dfdNodeMap.get(pcmVertex.getReferencedElement());

        if (dfdNode == null) {
            dfdNode = createDFDNode(pcmVertex);
        }

        addNodeCharacteristicsToNode(dfdNode, pcmVertex.getAllNodeCharacteristics());

        return dfdNode;
    }

    private Node createDFDNode(AbstractPCMVertex<? extends Entity> pcmVertex) {
        Node dfdNode = createCorrespondingDFDNode(pcmVertex);
        dfdNodeMap.put(pcmVertex.getReferencedElement(), dfdNode);
        return dfdNode;
    }

    private Node createCorrespondingDFDNode(AbstractPCMVertex<? extends Entity> pcmVertex) {
        Node node;

        if (pcmVertex instanceof UserPCMVertex<?>) {
            node = dataflowdiagramFactory.eINSTANCE.createExternal();
        } else if (pcmVertex instanceof SEFFPCMVertex<?>) {
            node = dataflowdiagramFactory.eINSTANCE.createProcess();
        } else {
            logger.error("Unregcognized palladio element");
            return null;
        }

        Behaviour behaviour = datadictionaryFactory.eINSTANCE.createBehaviour();
        node.setEntityName(pcmVertex.getReferencedElement().getEntityName());
        node.setId(pcmVertex.getReferencedElement().getId());
        node.setBehaviour(behaviour);
        dataDictionary.getBehaviour().add(behaviour);
        dataFlowDiagram.getNodes().add(node);
        return node;
    }

    private void addNodeCharacteristicsToNode(Node node, List<CharacteristicValue> charValues) {
        for (CharacteristicValue charValue : charValues) {
            Label label = getOrCreateDFDLabel(charValue);
            if (!node.getProperties().contains(label)) {
                node.getProperties().add(label);
            }
        }
    }

    private Label getOrCreateDFDLabel(CharacteristicValue charValue) {
        LabelType type = dataDictionary.getLabelTypes().stream().filter(f -> f.getEntityName().equals(charValue.getTypeName())).findFirst()
                .orElse(createLabelType(charValue));

        Label label = type.getLabel().stream().filter(f -> f.getEntityName().equals(charValue.getValueName())).findFirst()
                .orElse(createLabel(charValue, type));

        return label;
    }

    private Label createLabel(CharacteristicValue charValue, LabelType type) {
        Label label = datadictionaryFactory.eINSTANCE.createLabel();
        label.setEntityName(charValue.getValueName());
        type.getLabel().add(label);
        return label;
    }

    private LabelType createLabelType(CharacteristicValue charValue) {
        LabelType type = datadictionaryFactory.eINSTANCE.createLabelType();
        type.setEntityName(charValue.getTypeName());
        this.dataDictionary.getLabelTypes().add(type);
        return type;
    }

}
