package org.palladiosimulator.dataflow.confidentiality.analysis;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class PCMAnalysisUtils {

    public static final String EMF_PROFILE_PLUGIN = "org.palladiosimulator.dataflow.confidentiality.pcm.model.profile";
    public static final String EMF_PROFILE_NAME = "profile.emfprofile_diagram";

    public static final String PLUGIN_PATH = "org.palladiosimulator.dataflow.confidentiality.analysis";

    private static final ResourceSet resourceSet = new ResourceSetImpl();

    private PCMAnalysisUtils() {
        // Utility class
    }
    
    public static void addResource(Resource resource) {
    	resourceSet.getResources().add(resource);
    	EcoreUtil.resolveAll(resource);
    }

    // Partially based on Palladio's ResourceSetPartition
    public static EObject loadModelContent(URI modelURI) {
        final Resource resource = PCMAnalysisUtils.resourceSet.getResource(modelURI, true);

        if (resource == null) {
            throw new IllegalArgumentException(String.format("Model with URI %s could not be loaded.", modelURI));
        } else if (resource.getContents()
            .size() == 0) {
            throw new IllegalArgumentException(String.format("Model with URI %s is empty.", modelURI));
        }
        return resource.getContents()
            .get(0);
    }

    // Partially based on Palladio's ResourceSetPartition
    public static <T extends EObject> List<T> lookupElementOfType(final EClass targetType) {
        final ArrayList<T> result = new ArrayList<T>();
        for (final Resource r : PCMAnalysisUtils.resourceSet.getResources()) {
            if (PCMAnalysisUtils.isTargetInResource(targetType, r)) {
                result.addAll(EcoreUtil.<T> getObjectsByType(r.getContents(), targetType));
            }
        }

        return result;
    }

    // Partially based on Palladio's ResourceSetPartition
    public static boolean isTargetInResource(final EClass targetType, final Resource resource) {
        if (resource != null) {
            for (EObject c : resource.getContents()) {
                if (targetType.isSuperTypeOf(c.eClass())) {
                    return true;
                }
            }
        }
        return false;
    }

    // Partially based on Palladio's ResourceSetPartition
    public static void resolveAllProxies() {
        ArrayList<Resource> currentResources = null;

        do {
            currentResources = new ArrayList<Resource>(PCMAnalysisUtils.resourceSet.getResources());
            for (final Resource r : currentResources) {
                EcoreUtil.resolveAll(r);
            }
        } while (currentResources.size() != PCMAnalysisUtils.resourceSet.getResources()
            .size());
    }

}
