package org.dataflowanalysis.converter.dfd2web;

import org.dataflowanalysis.converter.Converter;
import org.dataflowanalysis.converter.ConverterFactory;

public class DataFlowDiagramConverterFactory implements ConverterFactory {
    @Override
    public Converter createInstance() {
        return new DataFlowDiagramConverter();
    }
}
