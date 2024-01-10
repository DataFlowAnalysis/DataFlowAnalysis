package org.dataflowanalysis.analysis.utils;

import java.nio.file.Paths;

import org.eclipse.emf.common.util.URI;

public class ResourceUtils {
    private ResourceUtils() {
        throw new IllegalStateException("Utility classes should not be instanciated");
    }
    
    /**
     * Creates a relative plugin uri from the given relative path
     * @param relativePath Given relative path
     * @return Returns plugin path with the given project name and provided relative path
     */
    public static URI createRelativePluginURI(String relativePath, String modelProjectName) {
        String path = Paths.get(modelProjectName, relativePath)
            .toString();
        return URI.createPlatformPluginURI(path, false);
    }
    

    
}
