package org.dataflowanalysis.converter.micro2dfd;

import org.dataflowanalysis.converter.Converter;
import org.dataflowanalysis.converter.ConverterFactory;

public class Micro2DFDConverterFactory implements ConverterFactory {
    @Override
    public Converter createInstance() {
        return new Micro2DFDConverter();
    }
}
