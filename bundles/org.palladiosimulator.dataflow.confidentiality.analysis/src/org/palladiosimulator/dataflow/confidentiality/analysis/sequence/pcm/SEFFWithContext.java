package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm;

import java.util.Deque;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;

public class SEFFWithContext {

    private final ResourceDemandingSEFF seff;
    private final Deque<AssemblyContext> context;

    public SEFFWithContext(ResourceDemandingSEFF seff, Deque<AssemblyContext> context) {
        this.seff = seff;
        this.context = context;
    }

    public ResourceDemandingSEFF getSeff() {
        return seff;
    }

    public Deque<AssemblyContext> getContext() {
        return context;
    }

}
