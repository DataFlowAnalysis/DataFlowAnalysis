package org.palladiosimulator.dataflow.confidentiality.scalability;

import java.io.IOException;

import org.palladiosimulator.dataflow.confidentiality.scalability.factory.PCMModelFactory;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.AssemblyAllocationBuilder;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.ComponentBuilder;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.InterfaceBuilder;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.dataflow.SEFFBuilder;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.dataflow.UsageBuilder;
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
		AssemblyAllocationBuilder assemblyAllocation = model.addAssemblyContext("ScalibiliyAssemblyContext", component)
			.addAllocation("Scalibiliy Allocation", resourceContainer);
		
		OperationProvidedRole providedRole =  assemblyAllocation.addSystemProvidedRole("ScalilibtyProvidedRole", operationInterface);
		SEFFBuilder.builder(component, operationInterface.getSignatures__OperationInterface().get(0))
			.addVariableAction("Scalibility Action")
			.build();
		UsageBuilder.builder(model.getUsageModel())
			.addCall("EntryLevelSystemCall")
			.setCallee(providedRole, operationInterface.getSignatures__OperationInterface().get(0))
			.buildCall()
			.build();
		System.out.println("Finished model generation");
		try {
			model.saveModel();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Saved model");
	}
}
