package org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.cdo.eresource.impl.CDOResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.plugin.RegistryReader;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.PlatformResourceURIHandlerImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
import org.modelversioning.emfprofile.Profile;
import org.palladiosimulator.dataflow.confidentiality.analysis.DataFlowConfidentialityAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.PCMAnalysisUtils;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.Characteristic;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.Characteristics;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.CharacteristicsFactory;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.EnumCharacteristic;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.profile.ProfileConstants;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.palladiosimulator.mdsdprofiles.api.ProfileAPI;
import org.palladiosimulator.mdsdprofiles.api.StereotypeAPI;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.protocol.util.ProtocolResourceFactoryImpl;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import com.google.inject.Injector;

import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;
import tools.mdsd.library.standalone.initialization.emfprofiles.EMFProfileInitializationTask;
import tools.mdsd.library.standalone.initialization.log4j.Log4jInitilizationTask;

public class LegacyCharacteristicBuilder implements NodeCharacteristicBuilder {
	private Class<?> activator;
	private String modelPath;
	
	private Characteristics usageCharacteristics;
	private Resource usageResource;
	private Characteristics resourceEnvironmentCharacteristics;
	private Resource resourceEnvironmentResource;
	private Characteristics systemCharacteristics;
	private Resource systemResource;
	
	private List<Resource> assignments = new ArrayList<>();
	
	public LegacyCharacteristicBuilder(Class<?> activator, String modelPath, 
			Resource usageResource, Resource resourceEnvirontmentResource, Resource systemResource) {
		this.activator = activator;
		this.modelPath = modelPath;
		this.usageResource = usageResource;
		this.resourceEnvironmentResource = resourceEnvirontmentResource;
		this.systemResource = systemResource;
	}
	
	@Override
	public void setup() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl() {
			public Resource createResource(URI uri) {
				return new XMIResourceImpl(uri);
			}
		});
		EcorePlugin.ExtensionProcessor.process(null);
		try {
			new Log4jInitilizationTask().initilizationWithoutPlatform();
			StandaloneInitializerBuilder.builder()
                .registerProjectURI(activator, modelPath)
                //.registerProjectURI(DataFlowConfidentialityAnalysis.class, PCMAnalysisUtils.PLUGIN_PATH)
                .build()
                .init();
			new EMFProfileInitializationTask(PCMAnalysisUtils.EMF_PROFILE_PLUGIN, PCMAnalysisUtils.EMF_PROFILE_NAME)
				.initilizationWithoutPlatform();
		} catch (StandaloneInitializationException e) {
			e.printStackTrace();
			return;
		}
		ResourceSet resourceSet = new ResourceSetImpl();
		URI profileURI = URI.createPlatformPluginURI(String.format("/%s/%s", PCMAnalysisUtils.EMF_PROFILE_PLUGIN, PCMAnalysisUtils.EMF_PROFILE_NAME), false);
		Profile profile = (Profile) resourceSet.getResource(profileURI, true).getContents().get(0);;
		ProfileAPI.applyProfile(usageResource, profile);
		ProfileAPI.applyProfile(resourceEnvironmentResource, profile);
		ProfileAPI.applyProfile(systemResource, profile);
		
		URI usageURI = usageResource.getURI().appendFileExtension("characteristics");
		Resource usageAssignmentResource = new XMLResourceImpl(usageURI);
		this.usageCharacteristics = CharacteristicsFactory.eINSTANCE.createCharacteristics();
		usageAssignmentResource.getContents().add(this.usageCharacteristics);
		this.assignments.add(usageAssignmentResource);
		
		URI resourceEnvironmentURI = resourceEnvironmentResource.getURI().appendFileExtension("characteristics");
		Resource resourceEnvironmentAssignmentResource = new XMLResourceImpl(resourceEnvironmentURI);
		this.resourceEnvironmentCharacteristics = CharacteristicsFactory.eINSTANCE.createCharacteristics();
		resourceEnvironmentAssignmentResource.getContents().add(this.resourceEnvironmentCharacteristics);
		this.assignments.add(resourceEnvironmentAssignmentResource);
		
		URI systemURI = systemResource.getURI().appendFileExtension("characteristics");
		Resource systemAssignmentResource = new XMLResourceImpl(systemURI);
		this.systemCharacteristics = CharacteristicsFactory.eINSTANCE.createCharacteristics();
		systemAssignmentResource.getContents().add(this.systemCharacteristics);
		this.assignments.add(systemAssignmentResource);
	}
	
	@Override
	public void save() throws IOException {
		for (Resource resource : this.assignments) {
			resource.save(Map.of());
		}
	}

	@Override
	public void addCharacteristic(ResourceContainer container, EnumCharacteristic characteristic) {
		if (!StereotypeAPI.isStereotypeApplied(container, ProfileConstants.characterisable.getStereotype())) {
			StereotypeAPI.applyStereotype(container, ProfileConstants.characterisable.getStereotype());
		}
		this.resourceEnvironmentCharacteristics.getCharacteristics().add(characteristic);
		StereotypeAPI.setTaggedValue(container, this.resourceEnvironmentCharacteristics.getCharacteristics(), 
				ProfileConstants.characterisable.getStereotype(), ProfileConstants.characterisable.getValue());
	}

	@Override
	public void addCharacteristic(UsageScenario scenario, EnumCharacteristic characteristic) {
		if (!StereotypeAPI.isStereotypeApplied(scenario, ProfileConstants.characterisable.getStereotype())) {
			StereotypeAPI.applyStereotype(scenario, ProfileConstants.characterisable.getStereotype());
		}
		this.usageCharacteristics.getCharacteristics().add(characteristic);
		StereotypeAPI.setTaggedValue(scenario, this.usageCharacteristics.getCharacteristics(), 
				ProfileConstants.characterisable.getStereotype(), ProfileConstants.characterisable.getValue());
	}

	@Override
	public void addCharacteristic(AssemblyContext assemblyContext, EnumCharacteristic characteristic) {
		if (!StereotypeAPI.isStereotypeApplied(assemblyContext, ProfileConstants.characterisable.getStereotype())) {
			StereotypeAPI.applyStereotype(assemblyContext, ProfileConstants.characterisable.getStereotype());
		}
		this.systemCharacteristics.getCharacteristics().add(characteristic);
		StereotypeAPI.setTaggedValue(assemblyContext, this.systemCharacteristics.getCharacteristics(), 
				ProfileConstants.characterisable.getStereotype(), ProfileConstants.characterisable.getValue());
	}
}
