package org.dataflowanalysis.converter.pcm2dfd;

import org.dataflowanalysis.converter.Converter;
import org.dataflowanalysis.converter.ConverterFactory;

public class PCMConverterFactory implements ConverterFactory {
    @Override
    public Converter createInstance() {
        return new PCMConverter();
    }
}
