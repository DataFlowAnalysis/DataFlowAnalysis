package org.dataflowanalysis.analysis.tests.dfd.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.dataflowanalysis.analysis.tests.AnalysisUtils.TEST_MODEL_PROJECT_NAME;

import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.core.DFDFlowGraphCollection;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.analysis.dfd.simple.DFDSimpleTransposeFlowGraphFinder;
import org.dataflowanalysis.examplemodels.Activator;

public class DFDSimpleTest {    
        private DFDConfidentialityAnalysis analysis;        

        @Test
        public void testForUnusedInPinDetection() {
        	String minimalDataFlowDiagramPath = Paths.get("models", "DFDSimpleModels", "UnusedInput.dataflowdiagram").toString();
        	String minimalDataDictionaryPath = Paths.get("models", "DFDSimpleModels", "UnusedInput.datadictionary").toString();
        	this.analysis = new DFDDataFlowAnalysisBuilder().standalone()
                    .modelProjectName(TEST_MODEL_PROJECT_NAME)
                    .usePluginActivator(Activator.class)
                    .useDataFlowDiagram(minimalDataFlowDiagramPath.toString())
                    .useDataDictionary(minimalDataDictionaryPath.toString())
                    .useTransposeFlowGraphFinder(DFDSimpleTransposeFlowGraphFinder.class)
                    .build();
        	
            this.analysis.initializeAnalysis();
            var exception = assertThrows(IllegalArgumentException.class, () -> analysis.findFlowGraphs());
            System.out.println(exception.getMessage());
            assertEquals("DFD not simple: outPin not requiring all InPins", exception.getMessage());
        } 
        
        @Test
        public void testForBranchingFlows() {
        	String minimalDataFlowDiagramPath = Paths.get("models", "DFDTestModels", "BranchingTest.dataflowdiagram").toString();
        	String minimalDataDictionaryPath = Paths.get("models", "DFDTestModels", "BranchingTest.datadictionary").toString();
        	this.analysis = new DFDDataFlowAnalysisBuilder().standalone()
                    .modelProjectName(TEST_MODEL_PROJECT_NAME)
                    .usePluginActivator(Activator.class)
                    .useDataFlowDiagram(minimalDataFlowDiagramPath.toString())
                    .useDataDictionary(minimalDataDictionaryPath.toString())
                    .useTransposeFlowGraphFinder(DFDSimpleTransposeFlowGraphFinder.class)
                    .build();
        	
            this.analysis.initializeAnalysis();
            var exception = assertThrows(IllegalArgumentException.class, () -> analysis.findFlowGraphs());
            System.out.println(exception.getMessage());
            assertEquals("DFD not simple: Number of flows to inpin not 1", exception.getMessage());
        } 
        
        @Test
        public void testForDeadOutPin() {
        	String minimalDataFlowDiagramPath = Paths.get("models", "DFDSimpleModels", "DeadOutPin.dataflowdiagram").toString();
        	String minimalDataDictionaryPath = Paths.get("models", "DFDSimpleModels", "DeadOutPin.datadictionary").toString();
        	this.analysis = new DFDDataFlowAnalysisBuilder().standalone()
                    .modelProjectName(TEST_MODEL_PROJECT_NAME)
                    .usePluginActivator(Activator.class)
                    .useDataFlowDiagram(minimalDataFlowDiagramPath.toString())
                    .useDataDictionary(minimalDataDictionaryPath.toString())
                    .useTransposeFlowGraphFinder(DFDSimpleTransposeFlowGraphFinder.class)
                    .build();
        	
            this.analysis.initializeAnalysis();
            var exception = assertThrows(IllegalArgumentException.class, () -> analysis.findFlowGraphs());
            System.out.println(exception.getMessage());
            assertEquals("DFD not simple: Dead output pin", exception.getMessage());
        } 
        
        @Test
        public void testForWrongFlowNames() {
        	String minimalDataFlowDiagramPath = Paths.get("models", "DFDSimpleModels", "WrongFlowName.dataflowdiagram").toString();
        	String minimalDataDictionaryPath = Paths.get("models", "DFDSimpleModels", "WrongFlowName.datadictionary").toString();
        	this.analysis = new DFDDataFlowAnalysisBuilder().standalone()
                    .modelProjectName(TEST_MODEL_PROJECT_NAME)
                    .usePluginActivator(Activator.class)
                    .useDataFlowDiagram(minimalDataFlowDiagramPath.toString())
                    .useDataDictionary(minimalDataDictionaryPath.toString())
                    .useTransposeFlowGraphFinder(DFDSimpleTransposeFlowGraphFinder.class)
                    .build();
        	
            this.analysis.initializeAnalysis();
            var exception = assertThrows(IllegalArgumentException.class, () -> analysis.findFlowGraphs());
            System.out.println(exception.getMessage());
            assertEquals("DFD not simple: All outgoing flows from one pin must have the same name", exception.getMessage());
        } 
}