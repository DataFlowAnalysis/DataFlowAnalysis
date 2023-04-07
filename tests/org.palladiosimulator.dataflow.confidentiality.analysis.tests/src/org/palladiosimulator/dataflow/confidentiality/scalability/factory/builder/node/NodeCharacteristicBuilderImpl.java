package org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.node;

import java.io.IOException;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.EnumCharacteristic;
import org.palladiosimulator.dataflow.nodecharacteristics.nodecharacteristics.AssemblyAssignee;
import org.palladiosimulator.dataflow.nodecharacteristics.nodecharacteristics.Assignments;
import org.palladiosimulator.dataflow.nodecharacteristics.nodecharacteristics.NodeCharacteristicsFactory;
import org.palladiosimulator.dataflow.nodecharacteristics.nodecharacteristics.RessourceAssignee;
import org.palladiosimulator.dataflow.nodecharacteristics.nodecharacteristics.UsageAsignee;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

public class NodeCharacteristicBuilderImpl implements NodeCharacteristicBuilder {
	private Resource resource;
	private Assignments assignments;
	
	public NodeCharacteristicBuilderImpl(URI uri) {
		this.resource = new XMLResourceImpl(uri);
	}
	
	@Override
	public void setup() {
		this.assignments = NodeCharacteristicsFactory.eINSTANCE.createAssignments();
		this.resource.getContents().add(this.assignments);
	}
	
	@Override
	public void save() throws IOException {
		this.resource.save(Map.of());
	}

	@Override
	public void addCharacteristic(ResourceContainer container, EnumCharacteristic characteristic) {
		RessourceAssignee assignee = NodeCharacteristicsFactory.eINSTANCE.createRessourceAssignee();
		assignee.setResourcecontainer(container);
		assignee.getCharacteristics().add(characteristic);
		assignments.getAssignee().add(assignee);
	}

	@Override
	public void addCharacteristic(UsageScenario scenario, EnumCharacteristic characteristic) {
		UsageAsignee assignee = NodeCharacteristicsFactory.eINSTANCE.createUsageAsignee();
		assignee.setUsagescenario(scenario);
		assignee.getCharacteristics().add(characteristic);
		assignments.getAssignee().add(assignee);
	}

	@Override
	public void addCharacteristic(AssemblyContext assemblyContext, EnumCharacteristic characteristic) {
		AssemblyAssignee assignee = NodeCharacteristicsFactory.eINSTANCE.createAssemblyAssignee();
		assignee.setAssemblycontext(assemblyContext);
		assignee.getCharacteristics().add(characteristic);
		assignments.getAssignee().add(assignee);
	}
}
