package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.node;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.PCMResourceLoader;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.PCMQueryUtils;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.EnumCharacteristic;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.repository.OperationalDataStoreComponent;
import org.palladiosimulator.dataflow.nodecharacteristics.nodecharacteristics.AbstractAssignee;
import org.palladiosimulator.dataflow.nodecharacteristics.nodecharacteristics.AssemblyAssignee;
import org.palladiosimulator.dataflow.nodecharacteristics.nodecharacteristics.Assignments;
import org.palladiosimulator.dataflow.nodecharacteristics.nodecharacteristics.NodeCharacteristicsFactory;
import org.palladiosimulator.dataflow.nodecharacteristics.nodecharacteristics.NodeCharacteristicsPackage;
import org.palladiosimulator.dataflow.nodecharacteristics.nodecharacteristics.RessourceAssignee;
import org.palladiosimulator.dataflow.nodecharacteristics.nodecharacteristics.UsageAsignee;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationPackage;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

public class PCMNodeCharacteristicsCalculatorImpl implements NodeCharacteristicsCalculator {
	private final Logger logger = Logger.getLogger(PCMNodeCharacteristicsCalculatorImpl.class);
    private final EObject node;
    private final PCMResourceLoader resourceLoader;
    
    /**
     * Creates a new node characteristic calculator with the given node
     * @param node Node of which the characteristics should be calculated. Should either be a User or SEFF Action.
     */
    public PCMNodeCharacteristicsCalculatorImpl(Entity node, PCMResourceLoader resourceLoader) {
    	this.node = node;
    	this.resourceLoader = resourceLoader;
    }

	@Override
	public List<CharacteristicValue> getNodeCharacteristics(Optional<Deque<AssemblyContext>> context) {
		Assignments assignments = this.resolveAssignments();
		List<AbstractAssignee> assignees;
		if (this.node instanceof AbstractUserAction) {
			assignees = this.getUsage(assignments);
		} else if (this.node instanceof AbstractAction || this.node instanceof OperationalDataStoreComponent) {
			assignees = this.getSEFF(assignments, context.get());
		} else {
			throw new IllegalArgumentException("Unkown assignee:" + this.node.toString());
		}
		List<EnumCharacteristic> enumCharacteristics = assignees.stream()
			.flatMap(it -> it.getCharacteristics().stream())
			.collect(Collectors.toList());
		return enumCharacteristics.stream()
				.flatMap(it -> it.getValues().stream().map(val -> new CharacteristicValue(it.getType(), val)))
				.collect(Collectors.toList());
	}
	
	private List<AbstractAssignee> getUsage(Assignments assignments) {
		UsageScenario usageScenario = PCMQueryUtils.findParentOfType(node, UsageScenario.class, false).get();
		return assignments.getAssignee().stream()
			.filter(UsageAsignee.class::isInstance)
			.map(UsageAsignee.class::cast)
			.filter(it -> it.getUsagescenario().equals(usageScenario))
			.collect(Collectors.toList());
	}
	
	private List<AbstractAssignee> getSEFF(Assignments assignments, Deque<AssemblyContext> context) {
		List<AbstractAssignee> resolvedAssignees = new ArrayList<>();
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
    	allocationContexts.forEach(it -> resolvedAssignees.addAll(getAllocation(assignments, it.getAssemblyContext_AllocationContext())));
    	allocationContexts.forEach(it -> resolvedAssignees.addAll(getResource(assignments, it.getResourceContainer_AllocationContext())));
    	return resolvedAssignees;
	}
	
	private List<AbstractAssignee> getAllocation(Assignments assignments, AssemblyContext assemblyContext) {
		return assignments.getAssignee().stream()
				.filter(AssemblyAssignee.class::isInstance)
				.map(AssemblyAssignee.class::cast)
				.filter(it -> it.getAssemblycontext().equals(assemblyContext))
				.collect(Collectors.toList());
	}
	
	private List<AbstractAssignee> getResource(Assignments assignments, ResourceContainer resourceContainer) {
		return assignments.getAssignee().stream()
				.filter(RessourceAssignee.class::isInstance)
				.map(RessourceAssignee.class::cast)
				.filter(it -> it.getResourcecontainer().equals(resourceContainer))
				.collect(Collectors.toList());
	}

	public Assignments resolveAssignments() {
		return this.resourceLoader.lookupElementOfType(NodeCharacteristicsPackage.eINSTANCE.getAssignments())
	            .stream()
	            .filter(Assignments.class::isInstance)
	            .map(Assignments.class::cast)
	            .findFirst().orElse(NodeCharacteristicsFactory.eINSTANCE.createAssignments());
	}
}
