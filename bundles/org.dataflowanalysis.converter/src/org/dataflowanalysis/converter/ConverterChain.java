package org.dataflowanalysis.converter;

import java.util.List;

/**
 * This class represents an opaque {@link Converter} that preforms several encapsulated conversions.
 * <p/>
 * The conversion will run sequentially though the provided list of converters
 */
public class ConverterChain extends Converter {
    private final List<Converter> converters;

    /**
     * Create a new {@link ConverterChain} with the given list of converters
     * @param converters List of at least one converter that are sequentially run
     */
    public ConverterChain(List<Converter> converters) {
        this.converters = converters;
    }

    @Override
    public PersistableConverterModel convert(ConverterModel input) {
        if (converters.isEmpty()) {
            return (PersistableConverterModel) input;
        }
        PersistableConverterModel current = this.converters.get(0)
                .convert(input);
        for (int i = 1; i < this.converters.size(); i++) {
            current = this.converters.get(i)
                    .convert(current);
        }
        return current;
    }
}
