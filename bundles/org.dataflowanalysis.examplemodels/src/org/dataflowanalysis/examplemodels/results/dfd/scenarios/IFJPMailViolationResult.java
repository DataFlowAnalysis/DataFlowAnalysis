package org.dataflowanalysis.examplemodels.results.dfd.scenarios;

import java.util.List;
import java.util.Map;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedCharacteristic;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;
import org.dataflowanalysis.examplemodels.results.dfd.DFDIdentifier;

public class IFJPMailViolationResult implements DFDExampleModelResult {
    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "IF-JPMail-violation";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
    	return List.of(new ConstraintDSL().ofData()
    			.withLabel("Level", "High")
    			.neverFlows()
    			.toVertex()
    			.withCharacteristic("Zone", "Attack")
    			.create());
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
        return List.of(
        		new ExpectedViolation(0, new DFDIdentifier("hkp4w"),
        				List.of(new ExpectedCharacteristic("Zone", "Attack")),
        				Map.of("fh2", List.of(new ExpectedCharacteristic("Level", "High")))), 
        		new ExpectedViolation(0, new DFDIdentifier("f63vu"), 
        				List.of(new ExpectedCharacteristic("Zone", "Attack")),
        				Map.of("pepa57", List.of(new ExpectedCharacteristic("Level", "High")))));
    }//Violating vertices: [(SMTP, hkp4w), (POP3, f63vu)]
    //fh2=[Level.High]
    //pepa57=[Level.High]

    @Override
    public String toString() {
        return this.getModelName();
    }
    
    @Override
    public String getFileName() {
    	return "diagram";
    }
}