package org.dataflowanalysis.analysis.converter;

import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;

public record CompleteDFD(DataFlowDiagram dataFlowDiagram, DataDictionary dataDictionary) {
}
