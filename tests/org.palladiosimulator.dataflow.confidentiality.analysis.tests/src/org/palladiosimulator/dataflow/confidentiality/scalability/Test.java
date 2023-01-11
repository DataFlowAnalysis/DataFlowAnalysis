package org.palladiosimulator.dataflow.confidentiality.scalability;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.ConfidentialityFactory;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.PCMModelFactory;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.PCMSEFFFactory;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.PCMUsageFactory;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.seff.AbstractAction;

public class Test {
	public static void main(String[] args) {
		PCMModelFactory model = new PCMModelFactory("file://test");
		BasicComponent component = model.addBasicComponent("Scalibility Component");
		OperationInterface operationInterface = model.addInterface("Scalibility Interface");
		OperationSignature method = model.addOperationSigniture("scaliblityTest", null);
		model.addInterfaceSigniture(operationInterface, method);
		ResourceContainer resourceContainer = model.addResourceContainer("Scaliblity Resource Container");
		AssemblyContext assemblyContext = model.addAssemblyContext(component);
		OperationProvidedRole role =  model.addInterfaceOperationProvidedRole(operationInterface, component);
		model.addAllocationContext(assemblyContext, resourceContainer);
		model.addSystemOperationProvidedRole(operationInterface, assemblyContext, component);
		
		PCMSEFFFactory seff = model.getSEFF();
		seff.setComponent(component);
		seff.setSigniture(method);
		AbstractAction action = seff.addSetVariableAction("Scalibility SEFF");
		seff.addAction(action);
		model.addSEFF(component, seff.getSEFF());
		
		PCMUsageFactory usage = model.getUsageScenario();
		usage.addCall(role);
		model.addUsageScenario(usage.getUsageScenario());
		
		System.out.println("Finished model generation");
		try {
			model.saveModel();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Saved model");
	}
}
