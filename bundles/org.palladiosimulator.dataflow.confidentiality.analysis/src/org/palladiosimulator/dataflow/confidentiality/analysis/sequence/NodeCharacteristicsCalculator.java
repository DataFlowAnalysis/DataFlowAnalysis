package org.palladiosimulator.dataflow.confidentiality.analysis.sequence;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.dataflow.confidentiality.analysis.PCMAnalysisUtils;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.PCMQueryUtils;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.EnumCharacteristic;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.repository.OperationalDataStoreComponent;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.profile.ProfileConstants;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.palladiosimulator.mdsdprofiles.api.StereotypeAPI;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.allocation.AllocationPackage;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

public class NodeCharacteristicsCalculator {
	private Logger logger = Logger.getLogger(NodeCharacteristicsCalculator.class);
    private EObject node;
    
    public NodeCharacteristicsCalculator(EObject node) {
    	this.node = node;
    }
    
    public List<CharacteristicValue> getNodeCharacteristics(Optional<Deque<AssemblyContext>> context) {
    	if (this.node instanceof AbstractUserAction) {
    		return getUserNodeCharacteristics((AbstractUserAction) this.node);
    	} else if(this.node instanceof AbstractAction) {
    		return getSEFFNodeCharacteristics(context.get());
    	} else if (this.node instanceof OperationalDataStoreComponent) {
    		return getSEFFNodeCharacteristics(context.get());
    	}
    	logger.error("Trying to calculate node characteristics of unknown node type");
    	throw new IllegalArgumentException("Cannot calculate node characteristics of unknown type");
    }
    
    private List<CharacteristicValue> getUserNodeCharacteristics(AbstractUserAction node) {
    	var usageScenario = PCMQueryUtils.findParentOfType(node, UsageScenario.class, false).get();
    	return this.evaluateNodeCharacteristics(usageScenario);
    }
    
    private List<CharacteristicValue> getSEFFNodeCharacteristics(Deque<AssemblyContext> context) {
    	List<CharacteristicValue> nodeVariables = new ArrayList<>();
    	
    	var allocations = PCMAnalysisUtils.lookupElementOfType(AllocationPackage.eINSTANCE.getAllocation()).stream()
    			.filter(Allocation.class::isInstance)
    			.map(Allocation.class::cast)
    			.collect(Collectors.toList());
    	
    	var allocation = allocations.stream()
    			.filter(it -> it.getAllocationContexts_Allocation().stream()
    					.map(alloc -> alloc.getAssemblyContext_AllocationContext())
    					.anyMatch(context.getFirst()::equals)
    					)
    			.findFirst()
    			.orElseThrow();
    	
    	var allocationContexts = allocation.getAllocationContexts_Allocation();
    	    	
    	for (AllocationContext allocationContext : allocationContexts) {
    		if (context.contains(allocationContext.getAssemblyContext_AllocationContext())) {
        		nodeVariables.addAll(this.evaluateNodeCharacteristics(allocationContext.getResourceContainer_AllocationContext()));
    		}
    	}
    	return nodeVariables;
	}
    
    /**
     * Evaluates the node characteristics for an object that can be tagged with Characteristic Values
     * @param object Object that can be tagged with Characteristic Values
     * @return Returns a list of Characteristic Values that tag the provided object
     */
    private List<CharacteristicValue> evaluateNodeCharacteristics(EObject object) {
    	List<CharacteristicValue> nodeCharacteristics = new ArrayList<>();
    	var enumCharacteristics = StereotypeAPI.<List<EnumCharacteristic>>getTaggedValueSafe(object, ProfileConstants.characterisable.getValue(), ProfileConstants.characterisable.getStereotype());
    	if (enumCharacteristics.isPresent()) {
    		var nodeEnumCharacteristics = enumCharacteristics.get();
    		for (EnumCharacteristic nodeEnumCharacteristic : nodeEnumCharacteristics) {
        		for (Literal nodeLiteral : nodeEnumCharacteristic.getValues()) {
        			nodeCharacteristics.add(new CharacteristicValue(nodeEnumCharacteristic.getType(), nodeLiteral));
        		}
    		}
    	}
    	return nodeCharacteristics;
    }
}
