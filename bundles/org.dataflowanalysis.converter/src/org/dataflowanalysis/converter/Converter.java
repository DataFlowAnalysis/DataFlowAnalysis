package org.dataflowanalysis.converter;

import org.apache.log4j.Logger;

public abstract class Converter {
    protected final Logger logger = Logger.getLogger(Converter.class);

    // TODO: Missing conversion options (like custom TFG Finders, reading mode or annotations)
    public abstract ConverterModel convert(ConverterModel input);
