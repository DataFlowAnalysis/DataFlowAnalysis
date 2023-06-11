package org.palladiosimulator.dataflow.confidentiality.scalability.tests;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.scalability.AnalysisExecutor;
import org.palladiosimulator.dataflow.confidentiality.scalability.AnalysisUtils;
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

public class NodeCharacteristicsTest extends ScalibilityTest {
	private final Logger logger = Logger.getLogger(NodeCharacteristicsTest.class);

	@Override
	public void run(ScalibilityParameter parameter, AnalysisExecutor analysisExecutor) {
		parameter.startTiming();
		PCMModelFactory factory;
		try {
			factory = new PCMModelFactory("./NodeCharacteristicsTest", parameter.isLegacy(),
					Activator.class, AnalysisUtils.TEST_MODEL_PROJECT_NAME);
		} catch (IOException e) {
			logger.error("Unable to create model factory", e);
			return;
		}
		ResourceContainer resourceContainer = factory.addResourceContainer("NodeServer");
		DataType dataType = factory.addDataType("NodeDataType");
		InterfaceBuilder interfaceBuilder = InterfaceBuilder.builder(factory.getRepository())
				.setName("NodeInterface");
		OperationSignature operationSignature = interfaceBuilder
				.addOperation("call", Map.of("param", dataType));
		OperationInterface operationInterface = interfaceBuilder.build();
		BasicComponent component = (BasicComponent) ComponentBuilder.basicComponent(factory.getRepository())
				.setName("NodeComponent")
				.provideInterface(operationInterface, "CallProvider")
				.build();
		CharacteristicBuilder characteristicBuilder = CharacteristicBuilder.builder(factory.getDictionary())
				.setName("NodeEnum");
		for(int i = 0; i < parameter.getModelSize(); i++) {
			characteristicBuilder = characteristicBuilder.addCharacteristicValue("Set" + i);
		}
		characteristicBuilder = characteristicBuilder.addCharacteristicValue("NotSet");
		EnumCharacteristicType characteristic = characteristicBuilder.build();
		AssemblyAllocationBuilder assemblyAllocation = 
				factory.addAssemblyContext("NodeAssembly", component)
				.addAllocation("NodeAllocation", resourceContainer);
		OperationProvidedRole providedRole = 
				assemblyAllocation.addSystemProvidedRole("NodeProvider", operationInterface);
		SEFFBuilder.builder(component, operationSignature)
				.build();
		UsageBuilder builder = UsageBuilder.builder(factory.getUsageModel(), factory.getNodeCharacteristicBuilder());
		for(int i = 0; i < parameter.getModelSize(); i++) {
			builder.addCharacteristic(characteristic, "Set" + i);
		}
		builder = builder.addCall("EntryLevelSystemCall")
				.setCallee(providedRole, operationSignature)
				.addInputCharacteristic("param" ,characteristic, Optional.of("Set0"))
				.addDefaultReturn("param")
				.buildCall();
		builder.build();
		try {
			factory.saveModel();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.runAnalysis(factory, parameter, analysisExecutor);
		parameter.stopTiming();
	}

	@Override
	public int getModelSize(int currentIndex) {
		return (int) Math.pow(10, currentIndex);
	}

	@Override
	public String getTestName() {
		return "NodeCharacteristicsTest";
	}

}
