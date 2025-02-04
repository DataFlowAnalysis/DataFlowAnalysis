package org.dataflowanalysis.analysis.dsl.context;

import org.dataflowanalysis.analysis.dsl.selectors.VertexType;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

/**
 * A {@link DSLContextProvider} is responsible for parsing a {@link VertexType} from a {@link StringView}.
 * As this behavior is analysis-specific, this functionality is provided as an interface
 */
public interface DSLContextProvider {
    ParseResult<VertexType> vertexTypeFromString(StringView string);
}
