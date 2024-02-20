package org.dataflowanalysis.analysis.converter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.dataflowanalysis.analysis.core.*;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMActionSequenceElement;
import org.dataflowanalysis.analysis.pcm.core.seff.SEFFActionSequenceElement;
import org.dataflowanalysis.analysis.pcm.core.user.*;
import org.palladiosimulator.pcm.core.entity.Entity;

import org.dataflowanalysis.dfd.datadictionary.*;
import org.dataflowanalysis.dfd.dataflowdiagram.*;

public class PalladioProcesser {

    private final Map<Entity, Node> dfdNodeMap = new HashMap<>();
    private final DataDictionary dataDictionary = datadictionaryFactory.eINSTANCE.createDataDictionary();
    private final DataFlowDiagram dataFlowDiagram = dataflowdiagramFactory.eINSTANCE.createDataFlowDiagram();

    public DataFlowDiagramAndDictionary process(List<ActionSequence> ass) {
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
        Node dfdNode = getOrCreateDFDNode(pcmASE);

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

            //Palladio Assumption: Each flows between two nodes with the same parameters/that are called the same use the same pin
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
        Optional<Pin> optPin = source.getBehaviour().getOutPin().stream().filter(p -> p.getEntityName().equals(parameters)).findAny();
        if (optPin.isPresent()) {
            return optPin.get();
        }
        Pin pin = datadictionaryFactory.eINSTANCE.createPin();
        pin.setEntityName(parameters);
        source.getBehaviour().getOutPin().add(pin);
        return pin;
    }

    // I know it is basically a code clone from above but i do not care
    private Pin findOrCreateInputPin(Node dest, String parameters) {
        Optional<Pin> optPin = dest.getBehaviour().getInPin().stream().filter(p -> p.getEntityName().equals(parameters)).findAny();
        if (optPin.isPresent()) {
            return optPin.get();
        }
        Pin pin = datadictionaryFactory.eINSTANCE.createPin();
        pin.setEntityName(parameters);
        dest.getBehaviour().getInPin().add(pin);
        return pin;
    }

    private Node getOrCreateDFDNode(AbstractPCMActionSequenceElement<? extends Entity> pcmASE) {
        Node dfdNode = dfdNodeMap.get(pcmASE.getElement());
        // check if a corresponding node has already been created
        if (dfdNode == null) {
            // if not, create a node
            dfdNode = createCorrespondingDFDNode(pcmASE);
            dfdNodeMap.put(pcmASE.getElement(), dfdNode);
        }

        // add all node characteristics to the current node (if not already present)
        addNodeCharacteristicsToNode(dfdNode, pcmASE.getAllNodeCharacteristics());

        return dfdNode;
    }

    private Node createCorrespondingDFDNode(AbstractPCMActionSequenceElement<? extends Entity> pcmASE) {
        Node node;

        if (pcmASE instanceof UserActionSequenceElement<?>) {
            node = dataflowdiagramFactory.eINSTANCE.createExternal();
        } else if (pcmASE instanceof SEFFActionSequenceElement<?>) {
            node = dataflowdiagramFactory.eINSTANCE.createProcess();
        } else {
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
        LabelType type = dataDictionary.getLabelTypes().stream().filter(f -> f.getEntityName().equals(charValue.getTypeName())).findFirst().orElse(null);

        if (type == null) {
            type = datadictionaryFactory.eINSTANCE.createLabelType();
            type.setEntityName(charValue.getTypeName());
            this.dataDictionary.getLabelTypes().add(type);
        }

        Label label = type.getLabel().stream().filter(f -> f.getEntityName().equals(charValue.getValueName())).findFirst().orElse(null);

        if (label == null) {
            label = datadictionaryFactory.eINSTANCE.createLabel();
            label.setEntityName(charValue.getValueName());
            type.getLabel().add(label);
        }

        return label;
    }
}
