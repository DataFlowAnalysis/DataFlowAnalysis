package org.dataflowanalysis.converter.web2dfd;

import org.dataflowanalysis.converter.Converter;
import org.dataflowanalysis.converter.ConverterFactory;

public class Web2DFDConverterFactory implements ConverterFactory {
    @Override
    public Converter createInstance() {
        return new Web2DFDConverter();
    }
}
