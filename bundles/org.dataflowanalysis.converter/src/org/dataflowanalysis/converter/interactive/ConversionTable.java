package org.dataflowanalysis.converter.interactive;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.dataflowanalysis.converter.Converter;
import org.dataflowanalysis.converter.ModelType;
import org.dataflowanalysis.converter.ConverterChain;
import org.dataflowanalysis.converter.dfd2web.DFD2WebConverter;
import org.dataflowanalysis.converter.micro2dfd.Micro2DFDConverter;
import org.dataflowanalysis.converter.pcm2dfd.PCM2DFDConverter;
import org.dataflowanalysis.converter.plant2micro.Plant2MicroConverter;
import org.dataflowanalysis.converter.web2dfd.Web2DFDConverter;

/**
 * This class contains all possible conversions currently supported by the converter
 */
public class ConversionTable {
    private final Map<ConversionKey, Supplier<Converter>> conversionTable = Map.of(
            ConversionKey.of(ModelType.PCM, ModelType.DFD), PCM2DFDConverter::new,
            ConversionKey.of(ModelType.DFD, ModelType.WEB_DFD), DFD2WebConverter::new,
            ConversionKey.of(ModelType.PLANT, ModelType.MICRO), Plant2MicroConverter::new,
            ConversionKey.of(ModelType.MICRO, ModelType.DFD), Micro2DFDConverter::new,
            ConversionKey.of(ModelType.WEB_DFD, ModelType.DFD), Web2DFDConverter::new
    );

    /**
     * TODO: This method is ugly. Furthermore this is just BFS
     * Determines the converter required to convert according to the given conversion key.
     * If no direct conversion between the elements exists, it creates the shortest {@link ConverterChain}
     * @param conversionKey Given conversion key that the converter should fulfil
     * @return Returns the converter that achieves the desired conversion
     * @throws IllegalArgumentException A valid converter cannot be found
     */
    public Converter getConverter(ConversionKey conversionKey) {
        if (conversionTable.containsKey(conversionKey)) {
            return conversionTable.get(conversionKey).get();
        }
        List<ModelType> visited = new ArrayList<>();
        Deque<ModelType> current = new ArrayDeque<>();
        Map<ModelType, ModelType> parent = new HashMap<>();
        visited.add(conversionKey.origin());
        current.add(conversionKey.origin());
        while (!current.isEmpty()) {
            ModelType modelType = current.pop();
            if (modelType.equals(conversionKey.destination())) {
                // Backtrack
                List<Converter> converters = new ArrayList<>();
                while (modelType != conversionKey.origin()) {
                    ModelType parentModelType = parent.get(modelType);
                    converters.add(conversionTable.get(ConversionKey.of(parentModelType, modelType)).get());
                    modelType = parentModelType;
                }
                Collections.reverse(converters);
                return new ConverterChain(converters);
            }
            visited.add(modelType);
            for (var directDestination : getDirectDestinations(modelType)) {
                if (!visited.contains(directDestination)) {
                    parent.put(directDestination, modelType);
                    current.addLast(directDestination);
                }
            }
        }
        throw new IllegalArgumentException("No path");
    }

    /**
     * Determines all possible origins for conversions
     * @return Returns a collection containing all possible origins for conversions
     */
    public Collection<ModelType> getPossibleOrigins() {
        Set<ModelType> result = new HashSet<>();
        for (var entry : conversionTable.entrySet()) {
            result.add(entry.getKey()
                    .origin());
        }
        return result;
    }

    /**
     * Determines all possible destinations for a model type.
     * Destinations via other model types are included in the result
     * @param origin Model type of the originating model
     * @return Returns a collection containing all possible (transitive) conversion destinations for the given origin
     */
    public Collection<ModelType> getPossibleDestinations(ModelType origin) {
        Set<ModelType> result = new HashSet<>();
        boolean changed = result.addAll(getDirectDestinations(origin));
        while (changed) {
            changed = false;
            for (ModelType modelType : result) {
                changed = result.addAll(getDirectDestinations(modelType));
            }
        }
        return result;
    }

    /**
     * Returns the direct conversion destinations for a model type
     * Does not include destinations via other model
     * @param origin Originating model type
     * @return Returns a collection containing all direct destinations for a model type
     */
    private Collection<ModelType> getDirectDestinations(ModelType origin) {
        Set<ModelType> result = new HashSet<>();
        for (var entry : conversionTable.entrySet()) {
            if (entry.getKey()
                    .origin()
                    .equals(origin)) {
                result.add(entry.getKey()
                        .destination());
            }
        }
        return result;
    }
}
