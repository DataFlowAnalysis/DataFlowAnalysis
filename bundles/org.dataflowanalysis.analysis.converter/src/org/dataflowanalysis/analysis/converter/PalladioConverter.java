package org.dataflowanalysis.analysis.converter;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.core.*;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMActionSequenceElement;
import org.dataflowanalysis.analysis.pcm.core.seff.SEFFActionSequenceElement;
import org.dataflowanalysis.analysis.pcm.core.user.*;
import org.dataflowanalysis.analysis.testmodels.Activator;
import org.palladiosimulator.pcm.core.entity.Entity;

import org.dataflowanalysis.dfd.datadictionary.*;
import org.dataflowanalysis.dfd.dataflowdiagram.*;

public class PalladioConverter extends Converter {

    private final Map<Entity, Node> dfdNodeMap = new HashMap<>();
    private final DataDictionary dataDictionary = datadictionaryFactory.eINSTANCE.createDataDictionary();
    private final DataFlowDiagram dataFlowDiagram = dataflowdiagramFactory.eINSTANCE.createDataFlowDiagram();

    private DataFlowDiagramAndDictionary processPalladio(List<ActionSequence> ass) {
        for (ActionSequence actionSequence : ass) {
            Node previousNode = null;
            for (AbstractActionSequenceElement<?> ASE : actionSequence.getElements()) {
                if (ASE instanceof AbstractPCMActionSequenceElement) {
                    previousNode = processActionSequenceElement((AbstractPCMActionSequenceElement<?>) ASE, previousNode);
                }
            }
        }
        return new DataFlowDiagramAndDictionary(dataFlowDiagram, dataDictionary);
    }

    private Node processActionSequenceElement(AbstractPCMActionSequenceElement<? extends Entity> pcmASE, Node previousDFDNode) {
        Node dfdNode = getDFDNode(pcmASE);

        createFlowBetweenPreviousAndCurrentNode(previousDFDNode, dfdNode, pcmASE);

        return dfdNode;
    }

    private void createFlowBetweenPreviousAndCurrentNode(Node source, Node dest, AbstractPCMActionSequenceElement<? extends Entity> pcmASE) {
        if (source == null || dest == null) {
            return;
        }
        List<DataFlowVariable> FlowVariables = pcmASE.getAllDataFlowVariables();
        for (DataFlowVariable flowVariable : FlowVariables) {
            String flowName = flowVariable.variableName();

            Optional<Flow> optFlow = dataFlowDiagram.getFlows().stream().filter(f -> f.getSourceNode().equals(source))
                    .filter(f -> f.getDestinationNode().equals(dest)).filter(f -> f.getEntityName().equals(flowName)).findFirst();

            if (optFlow.isPresent()) {
                return;
            }

            Flow newFlow = dataflowdiagramFactory.eINSTANCE.createFlow();
            newFlow.setSourceNode(source);
            newFlow.setDestinationNode(dest);
            newFlow.setEntityName(flowName);

            // Palladio Assumption: Each flows between two nodes with the same parameters/that are called the same use the same pin
            Pin sourceOutPin = findOrCreateOutputPin(source, flowName);
            Pin destInPin = findOrCreateInputPin(dest, flowName);
            newFlow.setSourcePin(sourceOutPin);
            newFlow.setDestinationPin(destInPin);

            ForwardingAssignment forwarding = datadictionaryFactory.eINSTANCE.createForwardingAssignment();
            forwarding.setOutputPin(sourceOutPin);
            source.getBehaviour().getAssignment().add(forwarding);

            this.dataFlowDiagram.getFlows().add(newFlow);
        }
    }

    // A pin is equivalent if the same parameters are passed
    private Pin findOrCreateOutputPin(Node source, String parameters) {
        return source.getBehaviour().getOutPin().stream().filter(p -> p.getEntityName().equals(parameters)).findAny().orElse(createPin(source,parameters,false));
    }

    // A pin is equivalent if the same parameters are passed
    private Pin findOrCreateInputPin(Node dest, String parameters) {
        return dest.getBehaviour().getInPin().stream().filter(p -> p.getEntityName().equals(parameters)).findAny().orElse(createPin(dest, parameters, true));
    }

    private Pin createPin(Node node, String parameters, boolean isInPin) {
        Pin pin = datadictionaryFactory.eINSTANCE.createPin();
        pin.setEntityName(parameters);
        if(isInPin) {
            node.getBehaviour().getInPin().add(pin);
        }
        else {
            node.getBehaviour().getOutPin().add(pin);
        }
        return pin;
    }

    private Node getDFDNode(AbstractPCMActionSequenceElement<? extends Entity> pcmASE) {
        Node dfdNode = dfdNodeMap.get(pcmASE.getElement());

        if (dfdNode == null) {
            dfdNode = createDFDNode(pcmASE);
        }

        addNodeCharacteristicsToNode(dfdNode, pcmASE.getAllNodeCharacteristics());

        return dfdNode;
    }

    private Node createDFDNode(AbstractPCMActionSequenceElement<? extends Entity> pcmASE) {
        Node dfdNode = createCorrespondingDFDNode(pcmASE);
        dfdNodeMap.put(pcmASE.getElement(), dfdNode);
        return dfdNode;
    }

    private Node createCorrespondingDFDNode(AbstractPCMActionSequenceElement<? extends Entity> pcmASE) {
        Node node;

        if (pcmASE instanceof UserActionSequenceElement<?>) {
            node = dataflowdiagramFactory.eINSTANCE.createExternal();
        } else if (pcmASE instanceof SEFFActionSequenceElement<?>) {
            node = dataflowdiagramFactory.eINSTANCE.createProcess();
        } else {
            logger.error("Unregcognized palladio element");
            return null;
        }

        Behaviour behaviour = datadictionaryFactory.eINSTANCE.createBehaviour();
        node.setEntityName(pcmASE.getElement().getEntityName());
        node.setId(pcmASE.getElement().getId());
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

    public DataFlowDiagramAndDictionary assToDFD(String inputModel, String inputFile, String modelLocation) {
        final var usageModelPath = Paths.get("models", inputModel, inputFile + ".usagemodel").toString();
        final var allocationPath = Paths.get("models", inputModel, inputFile + ".allocation").toString();
        final var nodeCharPath = Paths.get("models", inputModel, inputFile + ".nodecharacteristics").toString();

        DataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone().modelProjectName(modelLocation)
                .usePluginActivator(Activator.class).useUsageModel(usageModelPath).useAllocationModel(allocationPath)
                .useNodeCharacteristicsModel(nodeCharPath).build();

        analysis.initializeAnalysis();
        analysis.findAllSequences();
        var sequences = analysis.findAllSequences();
        var propagationResult = analysis.evaluateDataFlows(sequences);

        return processPalladio(propagationResult);
    }
}
