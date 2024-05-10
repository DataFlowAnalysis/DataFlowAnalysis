package org.dataflowanalysis.analysis.pcm.informationflow.core.utils;

import java.util.List;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Enumeration;
import org.dataflowanalysis.pcm.extension.model.confidentiality.dictionary.DictionaryPackage;
import org.dataflowanalysis.pcm.extension.model.confidentiality.dictionary.PCMDataDictionary;

/**
 * A Utils class for searching elements of specified {@link PCMDataDictionary}s. A major focus is on relevant elements
 * for the specified lattice. Most methods of this class assume a lattice to be specified. Note, a lattice can be
 * specified by creating an {@link EnumCharacteristicType} named 'Lattice'.
 */
public class IFPCMDataDictionaryUtils {

    private final static Logger logger = Logger.getLogger(IFPCMDataDictionaryUtils.class);

    private IFPCMDataDictionaryUtils() {
    }

    /**
     * Returns all specified {@link PCMDataDictionary}s.
     * @param resourceProvider the ResourceProvider used for searching specifications
     * @return all specified PCMDataDictionaries
     */
    public static List<PCMDataDictionary> getAllPCMDataDictionaries(ResourceProvider resourceProvider) {
        return resourceProvider.lookupToplevelElement(DictionaryPackage.eINSTANCE.getPCMDataDictionary())
                .stream()
                .filter(PCMDataDictionary.class::isInstance)
                .map(PCMDataDictionary.class::cast)
                .toList();
    }

    /**
     * Returns all specified {@link EnumCharacteristicType}s.
     * @param resourceProvider the ResourceProvider used for searching specifications
     * @return all specified EnumCharacteristicTypes
     */
    public static List<EnumCharacteristicType> getAllEnumCharacteristicTypes(ResourceProvider resourceProvider) {
        List<PCMDataDictionary> dictionaries = getAllPCMDataDictionaries(resourceProvider);
        return dictionaries.stream()
                .flatMap(dic -> dic.getCharacteristicTypes()
                        .stream())
                .filter(EnumCharacteristicType.class::isInstance)
                .map(EnumCharacteristicType.class::cast)
                .toList();
    }

    /**
     * Returns all specified {@link EnumCharacteristicType}s except the used lattice.
     * @param resourceProvider the ResourceProvider used for searching specifications
     * @return all specified EnumCharacteristicTypes except the used lattice
     */
    public static List<EnumCharacteristicType> getAllEnumCharacteristicTypesExceptLattice(ResourceProvider resourceProvider, String latticeName) {
        return getAllEnumCharacteristicTypes(resourceProvider).stream()
                .filter(it -> !it.equals(getLatticeCharacteristicType(resourceProvider, latticeName)))
                .toList();
    }

    /**
     * Returns the {@link EnumCharacteristicType} which specifies the lattice.
     * @param resourceProvider the ResourceProvider used for searching specifications
     * @return the EnumCharacteristicType which specifies the lattice
     */
    public static EnumCharacteristicType getLatticeCharacteristicType(ResourceProvider resourceProvider, String latticeName) {
        var latticeCharacteristicType = getAllEnumCharacteristicTypes(resourceProvider).stream()
                .filter(it -> it.getName()
                        .equals(latticeName))
                .findFirst();
        if (latticeCharacteristicType.isEmpty()) {
            String errorMessage = "Could not find an EnumCharacteristicType named '" + latticeName + "' in the resources.";
            logger.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        return latticeCharacteristicType.get();
    }

    /**
     * Returns the {@link Enumeration} of the specified lattice.
     * @param resourceProvider the ResourceProvider used for searching specifications
     * @return the Enumeration of the specified lattice
     */
    public static Enumeration getLatticeEnumeration(ResourceProvider resourceProvider, String latticeName) {
        return getLatticeCharacteristicType(resourceProvider, latticeName).getType();
    }

}
