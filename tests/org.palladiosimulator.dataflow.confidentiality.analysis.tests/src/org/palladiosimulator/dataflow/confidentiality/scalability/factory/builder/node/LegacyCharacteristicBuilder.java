package org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.node;

import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.EnumCharacteristic;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.profile.ProfileConstants;
import org.palladiosimulator.mdsdprofiles.api.StereotypeAPI;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

public class LegacyCharacteristicBuilder implements NodeCharacteristicBuilder {

	@Override
	public void addCharacteristic(ResourceContainer container, EnumCharacteristic characteristic) {
		if (!StereotypeAPI.isStereotypeApplied(container, ProfileConstants.characterisable.getStereotype())) {
			StereotypeAPI.applyStereotype(container, ProfileConstants.characterisable.getStereotype());
		}
		StereotypeAPI.setTaggedValue(container, List.of(characteristic),
				ProfileConstants.characterisable.getStereotype(), ProfileConstants.characterisable.getValue());
	}

	@Override
	public void addCharacteristic(UsageScenario scenario, EnumCharacteristic characteristic) {
		if (!StereotypeAPI.isStereotypeApplied(scenario, ProfileConstants.characterisable.getStereotype())) {
			StereotypeAPI.applyStereotype(scenario, ProfileConstants.characterisable.getStereotype());
		}
		StereotypeAPI.setTaggedValue(scenario, List.of(characteristic),
				ProfileConstants.characterisable.getStereotype(), ProfileConstants.characterisable.getValue());
	}

	@Override
	public void addCharacteristic(AssemblyContext assemblyContext, EnumCharacteristic characteristic) {
		if (!StereotypeAPI.isStereotypeApplied(assemblyContext, ProfileConstants.characterisable.getStereotype())) {
			StereotypeAPI.applyStereotype(assemblyContext, ProfileConstants.characterisable.getStereotype());
		}
		StereotypeAPI.setTaggedValue(assemblyContext, List.of(characteristic),
				ProfileConstants.characterisable.getStereotype(), ProfileConstants.characterisable.getValue());
	}
}
