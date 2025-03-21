package org.dataflowanalysis.converter.interactive;

import org.dataflowanalysis.converter.ModelType;

/**
 * A Conversion key describes a conversion between two model types.
 * It may be used in the {@link ConversionTable} to find the
 * corresponding {@link org.dataflowanalysis.converter.Converter} for the conversion
 * @param origin The origin of the conversion
 * @param destination The destination of the conversion
 */
public record ConversionKey(ModelType origin, ModelType destination) {

    public static ConversionKey of(ModelType origin, ModelType destination) {
        return new ConversionKey(origin, destination);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        ConversionKey that = (ConversionKey) o;
        return origin == that.origin && destination == that.destination;
    }

}
