package org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder.dataflow;

import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.SeffFactory;

public class SEFFBranchBuilder {
	private SEFFBuilder seff;
	private AbstractBranchTransition left;
	private AbstractBranchTransition right;
	
	public SEFFBranchBuilder(SEFFBuilder seff, AbstractBranchTransition left, AbstractBranchTransition right) {
		this.seff = seff;
		this.left = left;
		this.right = right;
	}
	
	public SEFFBuilder left() {
		ResourceDemandingSEFF leftSEFF = SeffFactory.eINSTANCE.createResourceDemandingSEFF();
		left.setBranchBehaviour_BranchTransition(leftSEFF);
		return SEFFBuilder.nested(leftSEFF);
	}
	
	public SEFFBuilder right() {
		ResourceDemandingSEFF rightSEFF = SeffFactory.eINSTANCE.createResourceDemandingSEFF();
		right.setBranchBehaviour_BranchTransition(rightSEFF);
		return SEFFBuilder.nested(rightSEFF);
	}

	public SEFFBuilder build() {
		return seff;
	}
}
