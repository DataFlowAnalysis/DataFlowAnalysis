package org.dataflowanalysis.dfd.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;
import tools.mdsd.modelingfoundations.identifier.Entity;

class MinimalTest {
    private static final String PLUGIN_PATH = "org.dataflowanalysis.dfd.tests";
    private static final String DFD_MODEL_PATH = "minimal.dataflowdiagram";
    private static final String DD_MODEL_PATH = "minimal.datadictionary";

    private ResourceSet resources = new ResourceSetImpl();

    @BeforeAll
    static void setUpBeforeAll() throws Exception {
        initStandalone();
    }

    @BeforeEach
    void setUpBeforeEach() throws Exception {
    }

    @Test
    void loadAndCheckSupertype() {
        System.out.println("Start loading dfd");
        DataFlowDiagram dfd = (DataFlowDiagram) loadResource(createPluginURI(DFD_MODEL_PATH));
        System.out.println("Start loading dd");
        DataDictionary dd = (DataDictionary) loadResource(createPluginURI(DD_MODEL_PATH));

        System.out.println("Checking models");
        checkEntitySupertype(dfd, dd);
    }

    private void checkEntitySupertype(DataFlowDiagram dfd, DataDictionary dd) {
        assertTrue(dfd instanceof Entity);
        dfd.getNodes()
                .forEach(n -> {
                    assertTrue(n instanceof Entity);
                });
        System.out.println("Nodes of type Entity");
        dfd.getFlows()
                .forEach(f -> {
                    assertTrue(f instanceof Entity);
                });
        System.out.println("Flows of type Entity");

        dd.getBehavior()
                .forEach(b -> {
                    assertTrue(b instanceof Entity);
                    b.getInPin()
                            .forEach(p -> {
                                assertTrue(p instanceof Entity);
                            });
                    b.getOutPin()
                            .forEach(p -> {
                                assertTrue(p instanceof Entity);
                            });
                });
        System.out.println("Behaviour and contained classes of type Entity");

        dd.getLabelTypes()
                .forEach(t -> {
                    assertTrue(t instanceof Entity);
                    t.getLabel()
                            .forEach(l -> {
                                assertTrue(l instanceof Entity);
                            });
                });
        System.out.println("LabelTypes and Labels of type Entity");
    }

    // private DataFlowDiagram changeInstance(DataFlowDiagram dfd) {
    // dfd.getFlows().get(0).setEntityName("ExcitingFlow");
    // return dfd;
    // }

    // private void saveChangedInstance(DataFlowDiagram dfd) {
    // ResourceSet resourceSet = new ResourceSetImpl();
    // resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
    // .put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
    // resourceSet.getPackageRegistry().put(dataflowdiagramPackage.eNS_URI,
    // dataflowdiagramPackage.eINSTANCE);
    //
    // Resource resource = resourceSet.createResource(URI.createFileURI("output/changedModel.dataflowdiagrammodel"));
    //
    // resource.getContents().add(dfd);
    // assertDoesNotThrow(() -> resource.save(Collections.EMPTY_MAP));
    // }

    private EObject loadResource(URI modelURI) {
        Resource resource = this.resources.getResource(modelURI, true);
        if (resource == null) {
            throw new IllegalArgumentException(String.format("Model with URI %s could not be loaded", modelURI));
        } else if (resource.getContents()
                .isEmpty()) {
            throw new IllegalArgumentException(String.format("Model with URI %s is empty", modelURI));
        }
        return resource.getContents()
                .get(0);
    }

    private URI createPluginURI(String relativePath) {
        String path = Paths.get(PLUGIN_PATH, "models", relativePath)
                .toString();
        return URI.createPlatformPluginURI(path, false);
    }

    private static void initStandalone() {
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
                .put("dataflowdiagram", new XMIResourceFactoryImpl());
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
                .put("datadictionary", new XMIResourceFactoryImpl());

        try {
            StandaloneInitializerBuilder.builder()
                    .registerProjectURI(MinimalTest.class, PLUGIN_PATH)
                    .build()
                    .init();

        } catch (StandaloneInitializationException e) {
            e.printStackTrace();
        }
    }

}
