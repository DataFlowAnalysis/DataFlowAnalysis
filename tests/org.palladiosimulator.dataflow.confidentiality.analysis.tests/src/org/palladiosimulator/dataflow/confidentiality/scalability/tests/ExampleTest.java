package org.palladiosimulator.dataflow.confidentiality.scalability.tests;

import java.io.IOException;

import org.palladiosimulator.dataflow.confidentiality.scalability.factory.PCMModelFactory;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.AssemblyAllocationBuilder;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.ComponentBuilder;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.InterfaceBuilder;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.dataflow.SEFFBuilder;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.dataflow.UsageBuilder;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityParameter;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityTest;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;

public class ExampleTest implements ScalibilityTest {

	@Override
	public void run(ScalibilityParameter parameter) {
		parameter.startTiming();
		PCMModelFactory model = new PCMModelFactory("file:/example");
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
		parameter.logAction("Model generation finished");
		try {
			model.saveModel();
		} catch (IOException e) {
			e.printStackTrace();
		}
		parameter.stopTiming();
	}

	@Override
	public int getModelSize(int currentIndex) {
		return (int) Math.pow(10, currentIndex);
	}

}
