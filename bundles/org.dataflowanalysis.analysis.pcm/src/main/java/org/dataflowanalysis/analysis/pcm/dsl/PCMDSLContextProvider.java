package org.dataflowanalysis.analysis.pcm.dsl;

import org.dataflowanalysis.analysis.dsl.context.DSLContextProvider;
import org.dataflowanalysis.analysis.dsl.selectors.VertexType;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public class PCMDSLContextProvider implements DSLContextProvider {
    @Override
    public ParseResult<VertexType> vertexTypeFromString(StringView string) {
        return PCMVertexType.fromString(string);
    }
}
