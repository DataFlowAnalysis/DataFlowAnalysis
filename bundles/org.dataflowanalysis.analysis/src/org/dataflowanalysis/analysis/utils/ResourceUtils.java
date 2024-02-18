package org.dataflowanalysis.analysis.utils;

import java.nio.file.Paths;
import org.eclipse.emf.common.util.URI;

/**
 * This utility class is used for working with eclipse resources
 */
public class ResourceUtils {
    private ResourceUtils() {
        throw new IllegalStateException("Utility classes should not be instanciated");
    }

    /**
     * Creates a relative plug-in URI from the given relative path
     * @param relativePath Given relative path inside the modeling project
     * @param modelProjectName Name of the modeling project
     * @return Returns plug-in path with the given project name and provided relative path
     */
    public static URI createRelativePluginURI(String relativePath, String modelProjectName) {
        String path = Paths.get(modelProjectName, relativePath).toString();
        return URI.createPlatformPluginURI(path, false);
    }
}
