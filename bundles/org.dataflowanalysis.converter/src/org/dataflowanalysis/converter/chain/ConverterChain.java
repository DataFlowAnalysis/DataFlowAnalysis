package org.dataflowanalysis.converter.chain;

import java.util.List;
import org.dataflowanalysis.converter.Converter;
import org.dataflowanalysis.converter.ConverterModel;

public class ConverterChain extends Converter {
    private final List<Converter> converters;

    public ConverterChain(List<Converter> converters) {
        this.converters = converters;
    }

    @Override
    public ConverterModel convert(ConverterModel input) {
        ConverterModel current = input;
        for (Converter converter : this.converters) {
            current = converter.convert(current);
        }
        return current;
    }
}
