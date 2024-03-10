package org.dataflowanalysis.analysis.pcm.informationflow.core.seff;

import java.util.Deque;
import java.util.List;

import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.ExternalCallAction;

public class IFCallingSEFFPCMVertex extends AbstractIFCallingSEFFPCMVertex {

	public IFCallingSEFFPCMVertex(ExternalCallAction element, List<? extends AbstractPCMVertex<?>> previousElements,
			Deque<AssemblyContext> context, List<Parameter> parameter, ResourceProvider resourceProvider) {
		super(element, previousElements, context, parameter, true, resourceProvider);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected List<VariableCharacterisation> extractStandardVariableCharacterisations() {
		ExternalCallAction element = getReferencedElement();
		return element.getInputVariableUsages__CallAction().stream()
				.flatMap(it -> it.getVariableCharacterisation_VariableUsage().stream()).toList();
	}

}