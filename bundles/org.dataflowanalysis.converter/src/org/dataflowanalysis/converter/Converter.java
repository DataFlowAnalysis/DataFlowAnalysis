package org.dataflowanalysis.converter;

import org.apache.log4j.Logger;

public abstract class Converter {
    protected final Logger logger = Logger.getLogger(Converter.class);

    public abstract PersistableConverterModel convert(ConverterModel input);
}
