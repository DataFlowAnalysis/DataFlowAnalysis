package org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.node;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.ResourceLoader;
import org.palladiosimulator.dataflow.confidentiality.analysis.utils.pcm.PCMQueryUtils;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.EnumCharacteristic;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.repository.OperationalDataStoreComponent;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.profile.ProfileConstants;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.palladiosimulator.mdsdprofiles.api.StereotypeAPI;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.allocation.AllocationPackage;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.repository.CompositeComponent;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

public class LegacyPCMNodeCharacteristicsCalculator implements NodeCharacteristicsCalculator {
	private final Logger logger = Logger.getLogger(LegacyPCMNodeCharacteristicsCalculator.class);
	private ResourceLoader resourceLoader;
	
	public LegacyPCMNodeCharacteristicsCalculator(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
    
    /**
     * Returns the node characteristics that are present at the given node with the assembly context provided. For User Actions the assembly context should be empty
     * @param context SEFF assembly context provided to the method. Should be empty for User Sequence Elements
     * @return Returns a list of node characteristics that are present at the current node
     */
	@Override
    public List<CharacteristicValue> getNodeCharacteristics(Entity node, Deque<AssemblyContext> context) {
    	if (node instanceof AbstractUserAction) {
    		return getUserNodeCharacteristics((AbstractUserAction) node);
    	} else if(node instanceof AbstractAction) {
    		return getSEFFNodeCharacteristics(context);
    	} else if (node instanceof OperationalDataStoreComponent) {
    		return getSEFFNodeCharacteristics(context);
    	}
    	logger.error("Trying to calculate node characteristics of unknown node type");
    	throw new IllegalArgumentException("Cannot calculate node characteristics of unknown type");
    }
    
    /**
     * Returns the node characteristics present at a node in a usage scenario
     * @param node Node in the usage scenario
     * @return List of node variables present at the node
     */
    private List<CharacteristicValue> getUserNodeCharacteristics(AbstractUserAction node) {
    	var usageScenario = PCMQueryUtils.findParentOfType(node, UsageScenario.class, false).get();
    	return this.evaluateNodeCharacteristics(usageScenario);
    }
    
    /**
     * Returns the node characteristics present at a node in a SEFF context
     * @param context Context of the SEFF node
     * @return List of node characteristics present at the given node in the context provided
     */
    private List<CharacteristicValue> getSEFFNodeCharacteristics(Deque<AssemblyContext> context) {
    	Set<CharacteristicValue> nodeVariables = new HashSet<>();
    	
    	var allocations = this.resourceLoader.lookupElementOfType(AllocationPackage.eINSTANCE.getAllocation()).stream()
    			.filter(Allocation.class::isInstance)
    			.map(Allocation.class::cast)
    			.collect(Collectors.toList());
    	
    	Optional<Allocation> allocation = allocations.stream()
    			.filter(it -> it.getAllocationContexts_Allocation().stream()
    							.map(alloc -> alloc.getAssemblyContext_AllocationContext())
    							.anyMatch(context.getFirst()::equals))
    			.findFirst();
    	
    	if (allocation.isEmpty()) {
    		logger.error("Could not find fitting allocation for assembly context of SEFF Node");
    		throw new IllegalStateException();
    	}
    	
    	var allocationContexts = allocation.get().getAllocationContexts_Allocation();
	    for (AllocationContext allocationContext : allocationContexts) {
	    	if (context.contains(allocationContext.getAssemblyContext_AllocationContext())) {
	        	nodeVariables.addAll(this.evaluateNodeCharacteristics(allocationContext.getResourceContainer_AllocationContext()));
	        	nodeVariables.addAll(this.evaluateNodeCharacteristics(allocationContext.getAssemblyContext_AllocationContext()));
	        	if (allocationContext.getAssemblyContext_AllocationContext().getEncapsulatedComponent__AssemblyContext() instanceof CompositeComponent) {
	        		nodeVariables.addAll(this.getCompositeCharacteristics(allocationContext, context));
	        	}
	    	}
	    }
    	return new ArrayList<>(nodeVariables);
	}
    
    private List<CharacteristicValue> getCompositeCharacteristics(AllocationContext allocationContext, Deque<AssemblyContext> context) {
    	List<CharacteristicValue> nodeVariables = new ArrayList<>();
    	CompositeComponent compositeComponent = (CompositeComponent) allocationContext.getAssemblyContext_AllocationContext().getEncapsulatedComponent__AssemblyContext();
		List<AssemblyContext> assemblyContexts = compositeComponent.getAssemblyContexts__ComposedStructure();
		for (AssemblyContext assemblyContext : assemblyContexts) {
			if (context.contains(assemblyContext)) {
				nodeVariables.addAll(this.evaluateNodeCharacteristics(assemblyContext));
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