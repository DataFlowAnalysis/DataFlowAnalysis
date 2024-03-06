package org.dataflowanalysis.analysis.converter;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.impl.URIHandlerImpl;

/**
 * Custom URI handler that ignores the relative path which would break the loading logic with resolveAll(). 
 */
public class FileNameOnlyURIHandler extends URIHandlerImpl {
    @Override
    public URI deresolve(URI uri)
    {
      return URI.createFileURI(uri.lastSegment()).appendFragment(uri.fragment());
    }

}
