package org.dataflowanalysis.converter.chain;

import java.util.List;
import org.dataflowanalysis.converter.Converter;
import org.dataflowanalysis.converter.ConverterFactory;
import org.dataflowanalysis.converter.ConverterModel;

public class ConverterChain extends Converter {
    private final List<ConverterFactory> converters;

    public ConverterChain(List<ConverterFactory> converters) {
        this.converters = converters;
    }

    @Override
    public ConverterModel convert(ConverterModel input) {
        ConverterModel current = input;
        for (ConverterFactory converterFactory : this.converters) {
            Converter converter = converterFactory.createInstance();
            current = converter.convert(current);
        }
        return current;
    }
}
