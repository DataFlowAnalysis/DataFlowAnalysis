package org.dataflowanalysis.converter.plant2micro;

import org.dataflowanalysis.converter.Converter;
import org.dataflowanalysis.converter.ConverterFactory;

public class Plant2MicroConverterFactory implements ConverterFactory {
    @Override
    public Converter createInstance() {
        return new Plant2MicroConverter();
    }
}
