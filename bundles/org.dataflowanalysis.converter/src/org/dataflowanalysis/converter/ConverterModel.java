package org.dataflowanalysis.converter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.converter.util.FileNameOnlyURIHandler;
import org.dataflowanalysis.converter.util.PathUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;

/**
 * Abstract representation for an input to an {@link Converter}
 */
public abstract class ConverterModel {
    protected static final Logger logger = LoggerManager.getLogger(ConverterModel.class);

    private final ModelType modelType;

    /**
     * Creates a new converter model with the given model type that is encapsulates
     * @param modelType Model type of the encapsulated model
     */
    public ConverterModel(ModelType modelType) {
        this.modelType = modelType;
    }

    /**
     * Prompts the user for a file path on the given scanner. It must have the given file extension
     * @param scanner Given scanner which the prompt will read from
     * @param fileExtension File extension the user-provided file path must have
     * @return Returns a string with the given file path
     */
    protected String getFilePath(Scanner scanner, String fileExtension) {
        System.out.println("Please enter a path to a " + fileExtension + " file: ");
        System.out.print("> ");
        return PathUtils.normalizePathString(scanner.nextLine(), fileExtension);
    }

    /**
     * Transforms the converter model to a concrete instance given the class
     * @param converterModelClass Class of the concrete instance
     * @return Returns an optional containing the concrete converter model instance
     * @param <T> Type parameter for the concrete converter model instance
     */
    public <T> Optional<T> toType(Class<T> converterModelClass) {
        if (converterModelClass.isInstance(this)) {
            return Optional.of(converterModelClass.cast(this));
        } else {
            return Optional.empty();
        }
    }

    protected Resource createResource(String outputFile, String[] fileExtensions, ResourceSet resourceSet) {
        for (String fileExtension : fileExtensions) {
            resourceSet.getResourceFactoryRegistry()
                    .getExtensionToFactoryMap()
                    .put(fileExtension, new XMLResourceFactoryImpl());
        }
        URI uri = URI.createFileURI(outputFile);
        return resourceSet.createResource(uri);
    }

    protected void saveResource(Resource resource) {
        Map<Object, Object> saveOptions = ((XMLResource) resource).getDefaultSaveOptions();
        saveOptions.put(XMLResource.OPTION_URI_HANDLER, new FileNameOnlyURIHandler());
        try {
            resource.save(saveOptions);
        } catch (IOException e) {
            logger.error("Error saving converter model");
            throw new RuntimeException(e);
        }
    }

    public ModelType getModelType() {
        return modelType;
    }
}
