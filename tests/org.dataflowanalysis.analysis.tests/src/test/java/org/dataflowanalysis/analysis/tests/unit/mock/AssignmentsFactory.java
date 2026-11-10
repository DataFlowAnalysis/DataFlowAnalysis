package org.dataflowanalysis.analysis.tests.unit.mock;

import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.Assignments;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.NodeCharacteristicsFactory;

public class AssignmentsFactory {
    private final Assignments assignments;

    public AssignmentsFactory() {
        this.assignments = NodeCharacteristicsFactory.eINSTANCE.createAssignments();
    }

}
