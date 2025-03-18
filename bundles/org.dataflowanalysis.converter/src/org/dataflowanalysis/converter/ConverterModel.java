package org.dataflowanalysis.converter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import org.apache.log4j.Logger;
import org.dataflowanalysis.converter.interactive.ModelType;
import org.dataflowanalysis.converter.util.FileNameOnlyURIHandler;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;

public abstract class ConverterModel {
    protected static final Logger logger = Logger.getLogger(ConverterModel.class);

    private final ModelType modelType;

    public ConverterModel(ModelType modelType) {
        this.modelType = modelType;
    }

    // TODO: Should be ending agnostic (accept ending and not)
    public abstract void save(String filePath, String fileName);

    protected String promptInput(Scanner scanner, String fileExtension) {
        System.out.println("Please enter a path to a ." + fileExtension + " file: ");
        System.out.print("> ");
        String filePath = scanner.nextLine();
        if (!filePath.endsWith("." + fileExtension)) {
            System.out.println("Invalid path to file, the file must end with ." + fileExtension + "!");
            throw new IllegalArgumentException();
        }
        return filePath;
    }

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
