package org.dataflowanalysis.analysis.pcm.informationflow.core;

import java.util.List;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Enumeration;
import org.dataflowanalysis.pcm.extension.model.confidentiality.dictionary.DictionaryPackage;
import org.dataflowanalysis.pcm.extension.model.confidentiality.dictionary.PCMDataDictionary;

public class IFPCMDataDictionaryUtils {

	private final static Logger logger = Logger.getLogger(IFPCMDataDictionaryUtils.class);

	private final static String LATTICE_CHARACTERISTIC_TYPE_NAME = "Lattice";

	private IFPCMDataDictionaryUtils() {
	}

	// TODO Copied from PCMDataCharacteristicsCalculator in part? Still differs
	// largely.

	// TODO assumes all CharacteristicType instances to be of the type
	// EnumCharacteristicType

	public static List<PCMDataDictionary> getAllPCMDataDictionaries(ResourceProvider resourceProvider) {
		return resourceProvider.lookupToplevelElement(DictionaryPackage.eINSTANCE.getPCMDataDictionary()).stream()
				.filter(PCMDataDictionary.class::isInstance).map(PCMDataDictionary.class::cast).toList();
	}

	public static List<EnumCharacteristicType> getAllEnumCharacteristicTypes(ResourceProvider resourceProvider) {
		List<PCMDataDictionary> dictionaries = getAllPCMDataDictionaries(resourceProvider);
		return dictionaries.stream().flatMap(dic -> dic.getCharacteristicTypes().stream())
				.filter(EnumCharacteristicType.class::isInstance).map(EnumCharacteristicType.class::cast).toList();

	}

	public static List<EnumCharacteristicType> getAllEnumCharacteristicTypesExceptLattice(
			ResourceProvider resourceProvider) {
		return getAllEnumCharacteristicTypes(resourceProvider).stream()
				.filter(it -> !it.equals(getLatticeCharacteristicType(resourceProvider))).toList();
	}

	public static EnumCharacteristicType getLatticeCharacteristicType(ResourceProvider resourceProvider) {
		var latticeCharacteristicType = getAllEnumCharacteristicTypes(resourceProvider).stream()
				.filter(it -> it.getName().equals(LATTICE_CHARACTERISTIC_TYPE_NAME)).findFirst();
		if (latticeCharacteristicType.isEmpty()) {
			String errorMsg = "Could not find an EnumCharacteristicType named '" + LATTICE_CHARACTERISTIC_TYPE_NAME
					+ "' in the resources.";
			logger.error(errorMsg);
			throw new IllegalStateException(errorMsg);
		}
		return latticeCharacteristicType.get();
	}

	public static Enumeration getLatticeEnumeration(ResourceProvider resourceProvider) {
		return getLatticeCharacteristicType(resourceProvider).getType();
	}

}
