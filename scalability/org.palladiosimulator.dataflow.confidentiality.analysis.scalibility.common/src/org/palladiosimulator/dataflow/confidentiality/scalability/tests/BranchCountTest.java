package org.palladiosimulator.dataflow.confidentiality.scalability.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.dataflow.SEFFBranchBuilder;
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

public class BranchCountTest extends ScalibilityTest {
	private Logger logger = Logger.getLogger(BranchCountTest.class);

	@Override
	public void run(ScalibilityParameter parameter, AnalysisExecutor analysisExecutor) {
		parameter.startTiming();
		PCMModelFactory factory;
		try {
			factory = new PCMModelFactory("./BranchCountTest", parameter.isLegacy(),
					Activator.class, AnalysisUtils.TEST_MODEL_PROJECT_NAME);
		} catch (IOException e) {
			logger.error("Unable to create model factory", e);
			return;
		}
		ResourceContainer resourceContainer = factory.addResourceContainer("BranchServer");
		DataType dataType = factory.addDataType("BranchDataType");
		InterfaceBuilder interfaceBuilder = InterfaceBuilder.builder(factory.getRepository())
				.setName("BranchInterface");
		Map<String, DataType> parameters = new HashMap<>(parameter.getModelSize());
		parameters.put("param", dataType);
		OperationSignature operationSignature = interfaceBuilder
				.addOperation("call", parameters);
		OperationInterface operationInterface = interfaceBuilder.build();
		BasicComponent component = (BasicComponent) ComponentBuilder.basicComponent(factory.getRepository())
				.setName("BranchComponent")
				.provideInterface(operationInterface, "CallProvider")
				.build();
		CharacteristicBuilder characteristicBuilder = CharacteristicBuilder.builder(factory.getDictionary())
				.setName("BranchEnum");
		characteristicBuilder = characteristicBuilder.addCharacteristicValue("Set");
		characteristicBuilder = characteristicBuilder.addCharacteristicValue("NotSet");
		EnumCharacteristicType characteristic = characteristicBuilder.build();
		AssemblyAllocationBuilder assemblyAllocation = 
				factory.addAssemblyContext("BranchAssembly", component)
				.addAllocation("BranchAllocation", resourceContainer);
		OperationProvidedRole providedRole = 
				assemblyAllocation.addSystemProvidedRole("BranchProvider", operationInterface);
		SEFFBuilder seffBuilder = SEFFBuilder.builder(component, operationSignature);
		List<SEFFBuilder> currentLayer = new ArrayList<>();
		currentLayer.add(seffBuilder);
		for(int i = 0; i < parameter.getModelSize(); i++) {
			List<SEFFBuilder> newLayer = new ArrayList<>();
			for (SEFFBuilder builder : currentLayer) {
				SEFFBranchBuilder branchBuilder = builder.addBranch("BranchLayer" + i);
				newLayer.add(branchBuilder.left());
				newLayer.add(branchBuilder.right());
				builder.build();
			}
			currentLayer = newLayer;
		}
		currentLayer.forEach(it -> it.build());
		UsageBuilder builder = UsageBuilder.builder(factory.getUsageModel(), factory.getNodeCharacteristicBuilder());
		builder.addCharacteristic(characteristic, "Set");
		UsageCallBuilder callBuilder = builder.addCall("EntryLevelSystemCall")
				.setCallee(providedRole, operationSignature);
		callBuilder = callBuilder.addInputCharacteristic("param", characteristic, Optional.of("Set"));
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
		return "BranchCountTest";
	}

}
