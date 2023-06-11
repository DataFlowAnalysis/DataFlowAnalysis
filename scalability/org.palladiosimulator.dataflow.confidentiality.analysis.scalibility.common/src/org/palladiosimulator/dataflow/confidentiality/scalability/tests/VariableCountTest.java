package org.palladiosimulator.dataflow.confidentiality.scalability.tests;

import java.io.IOException;
import java.util.HashMap;
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
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.dataflow.UsageCallBuilder;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityParameter;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityTest;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;

public class VariableCountTest extends ScalibilityTest {
	private Logger logger = Logger.getLogger(VariableCountTest.class);

	@Override
	public void run(ScalibilityParameter parameter, AnalysisExecutor analysisExecutor) {
		parameter.startTiming();
		PCMModelFactory factory;
		try {
			factory = new PCMModelFactory("./VariableCountTest", parameter.isLegacy(),
					Activator.class, AnalysisUtils.TEST_MODEL_PROJECT_NAME);
		} catch (IOException e) {
			logger.error("Unable to create model factory", e);
			return;
		}
		ResourceContainer resourceContainer = factory.addResourceContainer("VariableServer");
		DataType dataType = factory.addDataType("VariableDataType");
		InterfaceBuilder interfaceBuilder = InterfaceBuilder.builder(factory.getRepository())
				.setName("VariableInterface");
		Map<String, DataType> parameters = new HashMap<>(parameter.getModelSize());
		for (int i = 0; i < parameter.getModelSize(); i++) {
			parameters.put("param" + i, dataType);
		}
		OperationSignature operationSignature = interfaceBuilder
				.addOperation("call", parameters);
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
		SEFFBuilder.builder(component, operationSignature)
				.build();
		UsageBuilder builder = UsageBuilder.builder(factory.getUsageModel(), factory.getNodeCharacteristicBuilder());
		builder.addCharacteristic(characteristic, "Set");
		UsageCallBuilder callBuilder = builder.addCall("EntryLevelSystemCall")
				.setCallee(providedRole, operationSignature);
		for(int i = 0; i < parameter.getModelSize(); i++) {
			callBuilder = callBuilder.addInputCharacteristic("param" + i, characteristic, Optional.of("Set"));
		}	
		builder = callBuilder.addDefaultReturn("param")
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
		return "VariableCountTest";
	}

}
