package org.dataflowanalysis.converter.interactive;

import org.dataflowanalysis.converter.ModelType;

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
