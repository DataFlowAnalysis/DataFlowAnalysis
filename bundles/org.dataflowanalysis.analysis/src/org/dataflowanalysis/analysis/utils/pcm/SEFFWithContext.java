package org.dataflowanalysis.analysis.utils.pcm;

import java.util.Deque;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;

public record SEFFWithContext(ResourceDemandingSEFF seff, Deque<AssemblyContext> context) {

}
