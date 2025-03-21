package org.dataflowanalysis.converter;

import org.apache.log4j.Logger;

/**
 * This class represents an interface all converters should implement
 */
public abstract class Converter {
    protected final Logger logger = Logger.getLogger(Converter.class);

    /**
     * Converts the given {@link ConverterModel} input model to the given {@link ConverterModel} output model according to the specified conversion
     * @param input {@link ConverterModel} that is used as an input model. It may not be persistable
     * @return {@link ConverterModel} that is returned from the conversion. It must be persistable
     */
    public abstract PersistableConverterModel convert(ConverterModel input);
}
