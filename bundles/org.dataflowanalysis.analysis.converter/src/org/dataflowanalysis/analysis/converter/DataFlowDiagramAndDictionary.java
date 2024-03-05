package org.dataflowanalysis.analysis.converter;

import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;

/**
 * Combines a {@link DataFlowDiagram} and a {@link DataDictionary} for easier handling and access.
 */
public record DataFlowDiagramAndDictionary(DataFlowDiagram dataFlowDiagram, DataDictionary dataDictionary) {
}
