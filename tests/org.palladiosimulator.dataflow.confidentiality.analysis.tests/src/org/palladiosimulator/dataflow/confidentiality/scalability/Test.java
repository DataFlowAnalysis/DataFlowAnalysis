package org.palladiosimulator.dataflow.confidentiality.scalability;

import java.io.IOException;

import org.palladiosimulator.dataflow.confidentiality.scalability.factory.PCMModelFactory;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.ComponentBuilder;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.InterfaceBuilder;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;

public class Test {
	public static void main(String[] args) {
		PCMModelFactory model = new PCMModelFactory("file://test");
		BasicComponent component = (BasicComponent) ComponentBuilder
				.basicComponent(model.getRepository())
				.setName("ScalibilityComponent")
				.build();
		OperationInterface operationInterface = InterfaceBuilder
				.builder(model.getRepository())
				.setName("Scalibility Interface")
				.addOperation("scalibilityTest")
				.build();
		ResourceContainer resourceContainer = model.addResourceContainer("Scaliblity Resource Container");
		model.addAssemblyContext("ScalibiliyAssemblyContext", component)
			.addAllocation("Scalibiliy Allocation", resourceContainer)
			.addSystemProvidedRole("ScalilibtyProvidedRole", operationInterface);
		System.out.println("Finished model generation");
		try {
			model.saveModel();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Saved model");
	}
}
