package org.dataflowanalysis.analysis.pcm.core.seff;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.utils.PCMQueryUtils;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.SetVariableAction;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.seff.StopAction;

public class SEFFPCMVertex<T extends AbstractAction> extends AbstractPCMVertex<T> {
    private final Logger logger = Logger.getLogger(SEFFPCMVertex.class);

    private final List<Parameter> parameter;

    /**
     * Construct a new SEFF Action Sequence element with the given underlying Palladio Element, {@link AssemblyContext} and
     * a list of passed {@link Parameter}.
     * @param element Underlying Palladio SEFF Element
     * @param context Assembly context of the SEFF Element
     * @param parameter List of parameters, that were passed to the SEFF Element
     */
    public SEFFPCMVertex(T element, List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context, List<Parameter> parameter,
            ResourceProvider resourceProvider) {
        super(element, previousElements, context, resourceProvider);
        this.parameter = parameter;
    }

    @Override
    public void evaluateDataFlow() {
        List<DataCharacteristic> incomingDataCharacteristics = this.getIncomingDataCharacteristics();
        List<CharacteristicValue> nodeCharacteristics = this.getVertexCharacteristics();

        if (this.getReferencedElement() instanceof StartAction && !this.isBranching()) {
            List<String> variableNames = this.getParameter()
                    .stream()
                    .map(Parameter::getParameterName)
                    .toList();
            incomingDataCharacteristics = incomingDataCharacteristics.stream()
                    .filter(it -> variableNames.contains(it.variableName()))
                    .toList();
            this.setPropagationResult(incomingDataCharacteristics, incomingDataCharacteristics, nodeCharacteristics);
            return;
        } else if (this.getReferencedElement() instanceof StartAction) {
            this.setPropagationResult(incomingDataCharacteristics, incomingDataCharacteristics, nodeCharacteristics);
            return;
        } else if (this.getReferencedElement() instanceof StopAction && !this.isBranching()) {
            List<DataCharacteristic> outgoingDataCharacteristics = incomingDataCharacteristics.parallelStream()
                    .filter(it -> it.getVariableName()
                            .equals("RETURN"))
                    .collect(Collectors.toList());
            this.setPropagationResult(incomingDataCharacteristics, outgoingDataCharacteristics, nodeCharacteristics);
            return;
        } else if (this.getReferencedElement() instanceof StopAction) {
            this.setPropagationResult(incomingDataCharacteristics, incomingDataCharacteristics, nodeCharacteristics);
            return;
        } else if (!(this.getReferencedElement() instanceof SetVariableAction)) {
            logger.error("Found unexpected sequence element of unknown PCM type " + this.getReferencedElement()
                    .getClass()
                    .getName());
            throw new IllegalStateException("Unexpected action sequence element with unknown PCM type");
        }

        List<ConfidentialityVariableCharacterisation> variableCharacterisations = ((SetVariableAction) this.getReferencedElement())
                .getLocalVariableUsages_SetVariableAction()
                .stream()
                .flatMap(it -> it.getVariableCharacterisation_VariableUsage()
                        .stream())
                .filter(ConfidentialityVariableCharacterisation.class::isInstance)
                .map(ConfidentialityVariableCharacterisation.class::cast)
                .toList();

        List<DataCharacteristic> outgoingDataCharacteristics = this.getDataCharacteristics(nodeCharacteristics, variableCharacterisations,
                incomingDataCharacteristics);
        this.setPropagationResult(incomingDataCharacteristics, outgoingDataCharacteristics, nodeCharacteristics);
    }

    /**
     * Returns a list of parameters, that the SEFF was called with
     * @return List of parameters present for SEFF
     */
    public List<Parameter> getParameter() {
        return parameter;
    }

    /**
     * Returns whether a SEFF Action Sequence Element (i.e. Start Action) was created due to branching behavior
     * @return Returns true, if the SEFF Action was created, because branching behavior was defined. Otherwise, the method
     * returns false.
     */
    public boolean isBranching() {
        Optional<BranchAction> branchAction = PCMQueryUtils.findParentOfType(this.getReferencedElement(), BranchAction.class, false);
        return branchAction.isPresent();
    }

    @Override
    public String toString() {
        String elementName = this.getReferencedElement()
                .getEntityName();
        if (this.getReferencedElement() instanceof StartAction) {
            Optional<ResourceDemandingSEFF> seff = PCMQueryUtils.findParentOfType(this.getReferencedElement(), ResourceDemandingSEFF.class, false);
            if (seff.isPresent()) {
                elementName = "Beginning " + seff.get()
                        .getDescribedService__SEFF()
                        .getEntityName();
            }
            if (this.isBranching() && seff.isPresent()) {
                BranchAction branchAction = PCMQueryUtils.findParentOfType(this.getReferencedElement(), BranchAction.class, false)
                        .orElseThrow(() -> new IllegalStateException("Cannot find branch action"));
                AbstractBranchTransition branchTransition = PCMQueryUtils
                        .findParentOfType(this.getReferencedElement(), AbstractBranchTransition.class, false)
                        .orElseThrow(() -> new IllegalStateException("Cannot find branch transition"));
                elementName = "Branching " + seff.get()
                        .getDescribedService__SEFF()
                        .getEntityName() + "." + branchAction.getEntityName() + "." + branchTransition.getEntityName();
            }
        }
        if (this.getReferencedElement() instanceof StopAction) {
            Optional<ResourceDemandingSEFF> seff = PCMQueryUtils.findParentOfType(this.getReferencedElement(), ResourceDemandingSEFF.class, false);
            if (seff.isPresent()) {
                elementName = "Ending " + seff.get()
                        .getDescribedService__SEFF()
                        .getEntityName();
            }
        }
        return String.format("%s (%s, %s))", this.getClass()
                .getSimpleName(), elementName,
                this.getReferencedElement()
                        .getId());
    }

    @Override
    public AbstractPCMVertex<?> copy(Map<AbstractPCMVertex<?>, AbstractPCMVertex<?>> vertexMapping) {
        if (vertexMapping.get(this) != null) {
            return vertexMapping.get(this);
        }
        SEFFPCMVertex<?> copy = new SEFFPCMVertex<>(referencedElement, List.of(), new ArrayDeque<>(context), new ArrayList<>(this.getParameter()),
                resourceProvider);
        return super.updateCopy(copy, vertexMapping);
    }
}
