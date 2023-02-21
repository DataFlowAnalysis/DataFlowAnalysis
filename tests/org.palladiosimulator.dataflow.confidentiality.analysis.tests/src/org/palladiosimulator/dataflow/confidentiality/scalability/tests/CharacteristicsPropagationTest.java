package org.palladiosimulator.dataflow.confidentiality.scalability.tests;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.StandalonePCMDataFlowConfidentialtyAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.PCMResourceListLoader;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.ActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.testmodels.Activator;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.PCMModelFactory;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.AssemblyAllocationBuilder;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.CharacteristicBuilder;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.ComponentBuilder;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.InterfaceBuilder;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.dataflow.SEFFBuilder;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.dataflow.UsageBuilder;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityParameter;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityTest;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;

public class CharacteristicsPropagationTest implements ScalibilityTest {
	private final Logger logger = Logger.getLogger(CharacteristicsPropagationTest.class);

	@Override
	public void run(ScalibilityParameter parameter) {
		parameter.startTiming();
		PCMModelFactory factory;
		try {
			factory = new PCMModelFactory("CharacteristicsPropagation");
		} catch (IOException e) {
			logger.error("Unable to create model factory", e);
			return;
		}
		ResourceContainer resourceContainer = factory.addResourceContainer("CharacteristicsServer");
		DataType dataType = factory.addDataType("CharacteristicsDataType");
		InterfaceBuilder interfaceBuilder = InterfaceBuilder.builder(factory.getRepository())
				.setName("CharacteristicsInterface");
		OperationSignature operationSignature = interfaceBuilder
				.addOperation("call", Map.of("param", dataType));
		OperationInterface operationInterface = interfaceBuilder.build();
		BasicComponent component = (BasicComponent) ComponentBuilder.basicComponent(factory.getRepository())
				.setName("CharacteristicsComponent")
				.provideInterface(operationInterface, "CallProvider")
				.build();
		AssemblyAllocationBuilder assemblyAllocation = 
				factory.addAssemblyContext("CharacteristicsAssembly", component)
				.addAllocation("CharacteristicsAllocation", resourceContainer);
		OperationProvidedRole providedRole = 
				assemblyAllocation.addSystemProvidedRole("CharacteristicsProvider", operationInterface);
		SEFFBuilder.builder(component, operationSignature)
			.build();
		EnumCharacteristicType characteristic = CharacteristicBuilder.builder(factory.getDictionary())
				.setName("CharacteristicsEnum")
		.addCharacteristicValue("Set")
		.addCharacteristicValue("NotSet")
		.build();
		UsageBuilder builder = UsageBuilder.builder(factory.getUsageModel());
		for(int i = 0; i < parameter.getModelSize(); i++) {
			builder = builder.addCall("EntryLevelSystemCall" + i)
				.setCallee(providedRole, operationSignature)
				.addInputCharacteristic("param" ,characteristic, Optional.of("Set"))
				.addDefaultReturn("param")
				.buildCall();
		}
		builder.build();
		try {
			factory.saveModel();
		} catch (IOException e) {
			e.printStackTrace();
		}
		parameter.logAction("AnalysisExecution");
		StandalonePCMDataFlowConfidentialtyAnalysis analysis =
				new StandalonePCMDataFlowConfidentialtyAnalysis("org.palladiosimulator.dataflow.confidentiality.analysis.testmodels", 
						Activator.class, new PCMResourceListLoader(factory.getResources()));
		analysis.initalizeAnalysis();
		parameter.logAction("InitializedAnalysis");
		List<ActionSequence> sequences = analysis.findAllSequences();
		parameter.logAction("Sequences");
		analysis.evaluateDataFlows(sequences);
		parameter.stopTiming();
	}

	@Override
	public int getModelSize(int currentIndex) {
		return (int) Math.pow(10, currentIndex);
	}

	@Override
	public String getTestName() {
		return "CharacteristicsPropagationTest";
	}

}
