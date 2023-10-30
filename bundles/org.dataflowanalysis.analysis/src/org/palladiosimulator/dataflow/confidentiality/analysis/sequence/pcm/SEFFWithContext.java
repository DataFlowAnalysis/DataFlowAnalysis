package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm;

import java.util.Deque;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;

public record SEFFWithContext(ResourceDemandingSEFF seff, Deque<AssemblyContext> context) {

}
