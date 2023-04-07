package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.node;

import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.PCMResourceLoader;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.nodecharacteristics.nodecharacteristics.NodeCharacteristicsPackage;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.entity.Entity;

public class PCMNodeCharacteristicsCalculator {
	private final Logger logger = Logger.getLogger(PCMNodeCharacteristicsCalculator.class);
    private final EObject node;
    private final PCMResourceLoader resourceLoader;
    private final NodeCharacteristicsCalculator nodeCharacteristicsCalculator;
    /**
     * Creates a new node characteristic calculator with the given node
     * @param node Node of which the characteristics should be calculated. Should either be a User or SEFF Action.
     */
    public PCMNodeCharacteristicsCalculator(Entity node, PCMResourceLoader resourceLoader) {
    	this.node = node;
    	this.resourceLoader = resourceLoader;
    	if (this.resourceLoader.lookupElementOfType(NodeCharacteristicsPackage.eINSTANCE.getAssignments()).isEmpty()) {
    		this.nodeCharacteristicsCalculator = new LegacyPCMNodeCharacteristicsCalculator(node, resourceLoader);
    	} else {
    		this.nodeCharacteristicsCalculator = new PCMNodeCharacteristicsCalculatorImpl(node, resourceLoader);
    	}
    }
    
    public List<CharacteristicValue> getNodeCharacteristics(Optional<Deque<AssemblyContext>> context) {
    	return this.nodeCharacteristicsCalculator.getNodeCharacteristics(context);
    }
}
