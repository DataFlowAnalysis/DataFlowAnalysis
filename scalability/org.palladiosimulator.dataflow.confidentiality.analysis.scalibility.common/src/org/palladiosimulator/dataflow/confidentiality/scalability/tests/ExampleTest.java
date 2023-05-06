package org.palladiosimulator.dataflow.confidentiality.scalability.tests;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.scalability.AnalysisExecutor;
import org.palladiosimulator.dataflow.confidentiality.scalability.AnalysisUtils;
import org.palladiosimulator.dataflow.confidentiality.analysis.testmodels.Activator;
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
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;

public class ExampleTest extends ScalibilityTest {
	private final Logger logger = Logger.getLogger(ExampleTest.class);

	@Override
	public void run(ScalibilityParameter parameter, AnalysisExecutor analysisExecutor) {
		parameter.startTiming();
		PCMModelFactory model;
		try {
			model = new PCMModelFactory("example", false, Activator.class, AnalysisUtils.TEST_MODEL_PROJECT_NAME);
		} catch (IOException e) {
			logger.error("Could not create model factory", e);
			return;
		}
		InterfaceBuilder operationInterfaceBuilder = InterfaceBuilder
				.builder(model.getRepository())
				.setName("Scalibility Interface");
		OperationSignature signiture = operationInterfaceBuilder.addOperation("scalibilityTest");
		OperationInterface operationInterface = operationInterfaceBuilder.build();
		BasicComponent component = (BasicComponent) ComponentBuilder
				.basicComponent(model.getRepository())
				.setName("ScalibilityComponent")
				.provideInterface(operationInterface, "ScaliblityInterfaceProvider")
				.build();
		ResourceContainer resourceContainer = model.addResourceContainer("Scaliblity Resource Container");
		AssemblyAllocationBuilder assemblyAllocation = model.addAssemblyContext("ScalibiliyAssemblyContext", component)
			.addAllocation("Scalibiliy Allocation", resourceContainer);
		
		OperationProvidedRole providedRole =  assemblyAllocation.addSystemProvidedRole("ScalilibtyProvidedRole", operationInterface);
		SEFFBuilder.builder(component, signiture)
			.addVariableAction("Scalibility Action")
			.build();
		UsageBuilder.builder(model.getUsageModel(), model.getNodeCharacteristicBuilder())
			.addCall("EntryLevelSystemCall")
			.setCallee(providedRole, signiture)
			.buildCall()
			.build();
		try {
			model.saveModel();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		parameter.stopTiming();
	}

	@Override
	public int getModelSize(int currentIndex) {
		return (int) Math.pow(10, currentIndex);
	}

	@Override
	public String getTestName() {
		return "ExampleTest";
	}
}
