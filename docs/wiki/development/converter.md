# Converter
In Addition to the analysis, a Converter can be used to convert between several different existing models to models that can be used in our analysis.
See the table below for all currently supported conversion.
Keep in mind that these conversions can be chained, e.g. PCM->DFD->WebDFD.

| Model | Description | Possible Conversion Targets |
| --------------- | --------------- | --------------- |
| DFD | The [Data Flow Diagram Model](/wiki/dfd/) developed for the analysis | WebDFD |
| PCM | The [Palladio Component Model](/wiki/pcm/)| DFD |
| WebDFD | The representation of [Data Flow Diagrams](/wiki/dfd/) for the WebEditor | DFD |
| MicroSecEnd | A model developed by TU Hamburg | DFD |
| PlantUML | A representation of UMl | MicroSecEnd |

## Usage
### 1. Creating a Converter Model 
First, you will need to create a converter model for the origin model of your choice.
They follow the follwing naming scheme `<Model>ConverterModel` where `<Model>` denotes the name of the origin model. 
As some Converter Model can be created from already loaded objects or from file paths, refer to the different constructors of the origin model for more information. 

### 2a Determine the converter automatically
The `ConversionTable` class contains the different conversions that are supported.
First, create a new instance of the class, then use the `getConverter()` method.

As a Parameter you will need to provide a `ConversionKey` that denotes the desired origin and destination of your conversion.
A `ConversionKey` can be created with the static `of()` method with first the source `ModelType` and then the destination `ModelType`.

This will result in the following code, assuming `origin` holds the `ModelType` of the origin model and `destination` holds the `ModelType` of the destination model:
```java
ConversionTable conversionTable = new ConversionTable();
Converter converter = conversionTable.getConverter(ConversionKey.of(origin, destination);
```

### 2b Creating the converter directly
If you want to use the converters directly, you can also create a instance of the converter instead.
They follow this naming scheme `<Origin>2<Destination>Converter` while `<Origin>` and `<Destination>` are the abbriviated names of the desired models

### 3. Running the conversion
Each converter has a method `convert()` that takes a converter model and converts it. 
The resulting model is returned as another instance of an `ConverterModel`

### Optional: 4. Saving the result
If desired the resulting model can be saved using the `save()` method all (persistable) converter model have. 
For that the first parameter contains the relative or absolute path to the folder the model should be saved in. 
The file name is specified by the second parameter.

## Examples
### PCM2DFD
The following example converts a pcm model in the current working directory named `input` to a data flow diagram model with the name `output` and saves it in the current working directory.
```java
PCMConverterModel input = new PCMConverterModel("input.usagemodel", "input.allocation", "input.nodecharacteristics");
ConversionTable conversionTable = new ConversionTable();
Converter converter = converterTable.getConverter(ConversionKey.of(ModelType.PCM, ModelType.DFD));
PersistableConverterModel output = converter.convert(input);
output.save(".", "output");
```

### DFD2Web
The following example converts a dfd model in the current working directory named `input` to a WebDFD json file named `output`:
```java
DataFlowDiagramAndDictionary input = new DataFlowDiagramAndDictionary("input.dataflowdiagram", "input.datadictionary");
ConversionTable conversionTable = new ConversionTable();
Converter converter = converterTable.getConverter(ConversionKey.of(ModelType.DFD, ModelType.WebDFD));
PersistableConverterModel output = converter.convert(input);
output.save(".", "output");
```
