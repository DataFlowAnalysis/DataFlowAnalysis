package org.palladiosimulator.dataflow.confidentiality.analysis;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;

public abstract class TestBase {
    @BeforeAll
    static void init() throws StandaloneInitializationException {
        TestInitializer.init();
    }

    private Resource loadResource(final ResourceSet resourceSet, final URI path) {
        return resourceSet.getResource(path, true);
    }

    protected abstract List<String> getModelsPath();

    protected abstract void assignValues(List<Resource> list);

    protected <T extends EObject> T getModel(final List<Resource> resources, final Class<T> classObject) {
        final var object = resources.stream()
            .filter(e -> classObject.isInstance(e.getContents()
                .get(0)))
            .findAny();
        if (object.isEmpty()) {
            fail("Class not found " + classObject);
        }
        return classObject.cast(object.get()
            .getContents()
            .get(0));
    }

    @BeforeEach
    protected void loadModels() throws IOException {
        final var listResources = new ArrayList<Resource>();
        final var resourceSet = new ResourceSetImpl();

        final var listModels = this.getModelsPath();
        for (final var model : listModels) {
            var ressource = this.loadResource(resourceSet, TestInitializer.getModelURI(model));
            listResources.add(ressource);
        }

        this.assignValues(listResources);

        EcoreUtil.resolveAll(resourceSet);
    }

}