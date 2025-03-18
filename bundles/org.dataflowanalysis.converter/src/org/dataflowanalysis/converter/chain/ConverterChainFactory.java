package org.dataflowanalysis.converter.chain;

import java.util.List;
import org.dataflowanalysis.converter.Converter;
import org.dataflowanalysis.converter.ConverterFactory;

public class ConverterChainFactory implements ConverterFactory {
    private final List<ConverterFactory> converters;

    public ConverterChainFactory(List<ConverterFactory> converters) {
        this.converters = converters;
    }

    @Override
    public Converter createInstance() {
        return new ConverterChain(this.converters);
    }
}
