package org.palladiosimulator.dataflow.confidentiality.scalability.tests;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils;
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

public class VariableActionsTest implements ScalibilityTest {
	private final Logger logger = Logger.getLogger(VariableActionsTest.class);

	@Override
	public void run(ScalibilityParameter parameter) {
		parameter.startTiming();
		PCMModelFactory factory;
		try {
			factory = new PCMModelFactory("../org.palladiosimulator.dataflow.confidentiality.analysis.testmodels/VariableActions", true,
					Activator.class, AnalysisUtils.TEST_MODEL_PROJECT_NAME);
		} catch (IOException e) {
			logger.error("Unable to create model factory", e);
			return;
		}
		ResourceContainer resourceContainer = factory.addResourceContainer("VariableServer");
		DataType dataType = factory.addDataType("VariableDataType");
		InterfaceBuilder interfaceBuilder = InterfaceBuilder.builder(factory.getRepository())
				.setName("VariableInterface");
		OperationSignature operationSignature = interfaceBuilder
				.addOperation("call", Map.of("param", dataType));
		OperationInterface operationInterface = interfaceBuilder.build();
		BasicComponent component = (BasicComponent) ComponentBuilder.basicComponent(factory.getRepository())
				.setName("VariableComponent")
				.provideInterface(operationInterface, "CallProvider")
				.build();
		CharacteristicBuilder characteristicBuilder = CharacteristicBuilder.builder(factory.getDictionary())
				.setName("VariableEnum");
		characteristicBuilder = characteristicBuilder.addCharacteristicValue("Set");
		characteristicBuilder = characteristicBuilder.addCharacteristicValue("NotSet");
		EnumCharacteristicType characteristic = characteristicBuilder.build();
		AssemblyAllocationBuilder assemblyAllocation = 
				factory.addAssemblyContext("VariableAssembly", component)
				.addAllocation("VariableAllocation", resourceContainer);
		OperationProvidedRole providedRole = 
				assemblyAllocation.addSystemProvidedRole("VariableProvider", operationInterface);
		SEFFBuilder seffBuilder = SEFFBuilder.builder(component, operationSignature);
		for(int i = 0; i < parameter.getModelSize(); i++) {
			seffBuilder = seffBuilder.addVariableAction("SetVariableAction" + i);
		}
		seffBuilder.build();
		UsageBuilder builder = UsageBuilder.builder(factory.getUsageModel(), factory.getNodeCharacteristicBuilder());
		builder = builder.addCall("EntryLevelSystemCall")
				.setCallee(providedRole, operationSignature)
				.addInputCharacteristic("param" ,characteristic, Optional.of("Set"))
				.addDefaultReturn("param")
				.buildCall();
		builder.build();
		try {
			factory.saveModel();
		} catch (IOException e) {
			e.printStackTrace();
		}
		parameter.logAction("AnalysisExecution");
		StandalonePCMDataFlowConfidentialtyAnalysis analysis =
				new StandalonePCMDataFlowConfidentialtyAnalysis(AnalysisUtils.TEST_MODEL_PROJECT_NAME, 
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
		return "VariableActionsTest";
	}

}
