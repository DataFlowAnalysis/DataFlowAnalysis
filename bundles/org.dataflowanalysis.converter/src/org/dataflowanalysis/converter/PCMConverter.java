package org.dataflowanalysis.converter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.seff.CallingSEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.seff.SEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.CallingUserPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.UserPCMVertex;
import org.dataflowanalysis.analysis.pcm.utils.PCMQueryUtils;
import org.dataflowanalysis.dfd.datadictionary.Behaviour;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.ForwardingAssignment;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.dataflowanalysis.dfd.dataflowdiagram.dataflowdiagramFactory;
import org.eclipse.core.runtime.Plugin;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.seff.StopAction;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.Stop;
/**
 * Converts Palladio models to the data flow diagram and dictionary representation. Inherits from {@link Converter} to
 * utilize shared conversion logic while providing specific functionality for handling Palladio models.
 */
public class PCMConverter extends Converter{

    private final Map<Entity, Node> dfdNodeMap = new HashMap<>();
    private final DataDictionary dataDictionary = datadictionaryFactory.eINSTANCE.createDataDictionary();
    private final DataFlowDiagram dataFlowDiagram = dataflowdiagramFactory.eINSTANCE.createDataFlowDiagram();

    /**
     * Converts a PCM model into a DataFlowDiagramAndDictionary object.
     * @param modelLocation Location of the model folder.
     * @param usageModelPath Location of the usage model.
     * @param allocationPath Location of the allocation.
     * @param nodeCharPath Location of the node characteristics.
     * @param activator Activator class of the plugin where the model resides.
     * @return DataFlowDiagramAndDictionary object representing the converted Palladio model.
     */
    public DataFlowDiagramAndDictionary pcmToDFD(String modelLocation, String usageModelPath, String allocationPath, String nodeCharPath,
            Class<? extends Plugin> activator) {
        DataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone()
                .modelProjectName(modelLocation)
                .usePluginActivator(activator)
                .useUsageModel(usageModelPath)
                .useAllocationModel(allocationPath)
                .useNodeCharacteristicsModel(nodeCharPath)
                .build();

        analysis.initializeAnalysis();
        var flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();

        return processPalladio(flowGraph);
    }

    /**
     * Converts a PCM model into a DataFlowDiagramAndDictionary object.
     * @param modelLocation Location of the model folder.
     * @param usageModelPath Location of the usage model.
     * @param allocationPath Location of the allocation.
     * @param nodeCharPath Location of the node characteristics.
     * @return DataFlowDiagramAndDictionary object representing the converted Palladio model.
     */
    public DataFlowDiagramAndDictionary pcmToDFD(String modelLocation, String usageModelPath, String allocationPath, String nodeCharPath) {
        DataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone()
                .modelProjectName(modelLocation)
                .useUsageModel(usageModelPath)
                .useAllocationModel(allocationPath)
                .useNodeCharacteristicsModel(nodeCharPath)
                .build();

        analysis.initializeAnalysis();
        var flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();

        return processPalladio(flowGraph);
    }

    /**
     * This method compute the complete name of a PCM vertex depending on its type
     * @param vertex
     * @return String containing the complete name
     */
    public static String computeCompleteName(AbstractPCMVertex<?> vertex) {
        if (vertex instanceof SEFFPCMVertex<?> cast) {
            String elementName = cast.getReferencedElement()
                    .getEntityName();
            if (cast.getReferencedElement() instanceof StartAction) {
                Optional<ResourceDemandingSEFF> seff = PCMQueryUtils.findParentOfType(cast.getReferencedElement(), ResourceDemandingSEFF.class,
                        false);
                if (seff.isPresent()) {
                    elementName = "Beginning " + seff.get()
                            .getDescribedService__SEFF()
                            .getEntityName();
                }
                if (cast.isBranching() && seff.isPresent()) {
                    BranchAction branchAction = PCMQueryUtils.findParentOfType(cast.getReferencedElement(), BranchAction.class, false)
                            .orElseThrow(() -> new IllegalStateException("Cannot find branch action"));
                    AbstractBranchTransition branchTransition = PCMQueryUtils
                            .findParentOfType(cast.getReferencedElement(), AbstractBranchTransition.class, false)
                            .orElseThrow(() -> new IllegalStateException("Cannot find branch transition"));
                    elementName = "Branching " + seff.get()
                            .getDescribedService__SEFF()
                            .getEntityName() + "." + branchAction.getEntityName() + "." + branchTransition.getEntityName();
                }
            }
            if (cast.getReferencedElement() instanceof StopAction) {
                Optional<ResourceDemandingSEFF> seff = PCMQueryUtils.findParentOfType(cast.getReferencedElement(), ResourceDemandingSEFF.class,
                        false);
                if (seff.isPresent()) {
                    elementName = "Ending " + seff.get()
                            .getDescribedService__SEFF()
                            .getEntityName();
                }
            }
            return elementName;
        }
        if (vertex instanceof UserPCMVertex<?> cast) {
            if (cast.getReferencedElement() instanceof Start || cast.getReferencedElement() instanceof Stop) {
                return cast.getEntityNameOfScenarioBehaviour();
            }
            return cast.getReferencedElement()
                    .getEntityName();
        }
        if (vertex instanceof CallingSEFFPCMVertex cast) {
            return cast.getReferencedElement()
                    .getEntityName();
        }
        if (vertex instanceof CallingUserPCMVertex cast) {
            return cast.getReferencedElement()
                    .getEntityName();
        }
        return vertex.getReferencedElement()
                .getEntityName();
    }

    private DataFlowDiagramAndDictionary processPalladio(FlowGraphCollection flowGraphCollection) {
        for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraphCollection.getTransposeFlowGraphs()) {
            Node previousNode = null;
            for (AbstractVertex<?> abstractVertex : transposeFlowGraph.getVertices()) {
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
        List<DataCharacteristic> dataCharacteristics = pcmVertex.getAllDataCharacteristics();
        for (DataCharacteristic dataCharacteristic : dataCharacteristics) {
            String flowName = dataCharacteristic.variableName();

            dataFlowDiagram.getFlows()
                    .stream()
                    .filter(f -> f.getSourceNode()
                            .equals(source))
                    .filter(f -> f.getDestinationNode()
                            .equals(dest))
                    .filter(f -> f.getEntityName()
                            .equals(flowName))
                    .findFirst()
                    .orElseGet(() -> createFlow(source, dest, flowName));
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
        source.getBehaviour()
                .getAssignment()
                .add(forwarding);

        this.dataFlowDiagram.getFlows()
                .add(newFlow);
        return newFlow;
    }

    // A pin is equivalent if the same parameters are passed
    private Pin findOutputPin(Node source, String parameters) {
        return source.getBehaviour()
                .getOutPin()
                .stream()
                .filter(p -> p.getEntityName()
                        .equals(parameters))
                .findAny()
                .orElseGet(() -> createPin(source, parameters, false));
    }

    // A pin is equivalent if the same parameters are passed
    private Pin findInputPin(Node dest, String parameters) {
        return dest.getBehaviour()
                .getInPin()
                .stream()
                .filter(p -> p.getEntityName()
                        .equals(parameters))
                .findAny()
                .orElseGet(() -> createPin(dest, parameters, true));
    }

    private Pin createPin(Node node, String parameters, boolean isInPin) {
        Pin pin = datadictionaryFactory.eINSTANCE.createPin();
        pin.setEntityName(parameters);
        if (isInPin) {
            node.getBehaviour()
                    .getInPin()
                    .add(pin);
        } else {
            node.getBehaviour()
                    .getOutPin()
                    .add(pin);
        }
        return pin;
    }

    private Node getDFDNode(AbstractPCMVertex<? extends Entity> pcmVertex) {
        Node dfdNode = dfdNodeMap.get(pcmVertex.getReferencedElement());

        if (dfdNode == null) {
            dfdNode = createDFDNode(pcmVertex);
        }

        addNodeCharacteristicsToNode(dfdNode, pcmVertex.getAllVertexCharacteristics());

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

        node.setEntityName(computeCompleteName(pcmVertex));
        node.setId(pcmVertex.getReferencedElement()
                .getId());

        node.setBehaviour(behaviour);
        dataDictionary.getBehaviour()
                .add(behaviour);
        dataFlowDiagram.getNodes()
                .add(node);
        return node;
    }

    private void addNodeCharacteristicsToNode(Node node, List<CharacteristicValue> charValues) {
        for (CharacteristicValue charValue : charValues) {
            Label label = getOrCreateDFDLabel(charValue);
            if (!node.getProperties()
                    .contains(label)) {
                node.getProperties()
                        .add(label);
            }
        }
    }

    private Label getOrCreateDFDLabel(CharacteristicValue charValue) {
        LabelType type = dataDictionary.getLabelTypes()
                .stream()
                .filter(f -> f.getEntityName()
                        .equals(charValue.getTypeName()))
                .findFirst()
                .orElseGet(() -> createLabelType(charValue));

        Label label = type.getLabel()
                .stream()
                .filter(f -> f.getEntityName()
                        .equals(charValue.getValueName()))
                .findFirst()
                .orElseGet(() -> createLabel(charValue, type));

        return label;
    }

    private Label createLabel(CharacteristicValue charValue, LabelType type) {
        Label label = datadictionaryFactory.eINSTANCE.createLabel();
        label.setEntityName(charValue.getValueName());
        type.getLabel()
                .add(label);
        return label;
    }

    private LabelType createLabelType(CharacteristicValue charValue) {
        LabelType type = datadictionaryFactory.eINSTANCE.createLabelType();
        type.setEntityName(charValue.getTypeName());
        this.dataDictionary.getLabelTypes()
                .add(type);
        return type;
    }

}
