package org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.node;

import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.EnumCharacteristic;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

public class NodeCharacteristicBuilderImpl implements NodeCharacteristicBuilder {
	
	@Override
	public void setup() {
		
	}
	
	@Override
	public void save() {
		
	}

	@Override
	public void addCharacteristic(ResourceContainer container, EnumCharacteristic characteristic) {
		/*
		ResourceAssignee assignee = NodeCharacteristicsFactory.eINSTANCE.createResourceAssignee();
		assignee.setResourceContainer(container);
		assignee.getCharacteristics().add(characteristic);
		assignments.getAssignees().add(assignee);
		 */
	}

	@Override
	public void addCharacteristic(UsageScenario scenario, EnumCharacteristic characteristic) {
		/*
		UsageAssignee assignee = NodeCharacteristicsFactory.eINSTANCE.createUsageAssignee();
		assignee.setUsageScenario(scenario);
		assignee.getCharacteristics().add(characteristic);
		assignments.getAssignees().add(assignee);
		 */
	}

	@Override
	public void addCharacteristic(AssemblyContext assemblyContext, EnumCharacteristic characteristic) {
		/*
		AssemblyAssignee assignee = NodeCharacteristicsFactory.eINSTANCE.createAssemblyAssignee();
		assignee.setAssemblyContext(assemblyContext);
		assignee.getCharacteristics().add(characteristic);
		assignments.getAssignees().add(assignee);
		 */
	}

}
