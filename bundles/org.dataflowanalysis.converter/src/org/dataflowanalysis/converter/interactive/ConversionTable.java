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
import org.dataflowanalysis.converter.ConverterFactory;
import org.dataflowanalysis.converter.chain.ConverterChainFactory;
import org.dataflowanalysis.converter.dfd2web.DataFlowDiagramConverterFactory;
import org.dataflowanalysis.converter.micro2dfd.Micro2DFDConverterFactory;
import org.dataflowanalysis.converter.pcm2dfd.PCMConverterFactory;
import org.dataflowanalysis.converter.plant2micro.Plant2MicroConverterFactory;
import org.dataflowanalysis.converter.web2dfd.Web2DFDConverterFactory;

public class ConversionTable {
    private final Map<ConversionKey, ConverterFactory> conversionTable = Map.of(ConversionKey.of(ModelType.PCM, ModelType.DFD),
            new PCMConverterFactory(), ConversionKey.of(ModelType.DFD, ModelType.WEB_DFD), new DataFlowDiagramConverterFactory(),
            ConversionKey.of(ModelType.PLANT, ModelType.MICRO), new Plant2MicroConverterFactory(), ConversionKey.of(ModelType.MICRO, ModelType.DFD),
            new Micro2DFDConverterFactory(), ConversionKey.of(ModelType.WEB_DFD, ModelType.DFD), new Web2DFDConverterFactory());

    public ConverterFactory getConverter(ConversionKey conversionKey) {
        if (conversionTable.containsKey(conversionKey)) {
            return conversionTable.get(conversionKey);
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
                List<ConverterFactory> converters = new ArrayList<>();
                while (modelType != conversionKey.origin()) {
                    ModelType parentModelType = parent.get(modelType);
                    converters.add(conversionTable.get(ConversionKey.of(parentModelType, modelType)));
                    modelType = parentModelType;
                }
                Collections.reverse(converters);
                return new ConverterChainFactory(converters);
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

    public Collection<ModelType> getPossibleOrigins() {
        Set<ModelType> result = new HashSet<>();
        for (var entry : conversionTable.entrySet()) {
            result.add(entry.getKey()
                    .origin());
        }
        return result;
    }

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
