package org.dataflowanalysis.analysis.dsl.context;

import org.dataflowanalysis.analysis.dsl.selectors.VertexType;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public interface DSLContextProvider {
    ParseResult<VertexType> vertexTypeFromString(StringView string);
}
