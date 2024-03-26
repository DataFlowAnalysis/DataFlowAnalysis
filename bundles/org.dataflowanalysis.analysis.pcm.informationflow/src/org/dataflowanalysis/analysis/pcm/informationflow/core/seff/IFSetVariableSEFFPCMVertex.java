package org.dataflowanalysis.analysis.pcm.informationflow.core.seff;

import java.util.Deque;
import java.util.List;

import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.SetVariableAction;

public class IFSetVariableSEFFPCMVertex extends AbstractIFSEFFPCMVertex<SetVariableAction> {

	public IFSetVariableSEFFPCMVertex(SetVariableAction element, List<? extends AbstractPCMVertex<?>> previousElements,
			Deque<AssemblyContext> context, List<Parameter> parameter, ResourceProvider resourceProvider) {
		super(element, previousElements, context, parameter, resourceProvider);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected List<VariableCharacterisation> extractStandardVariableCharacterisations() {
		SetVariableAction element = getReferencedElement();
		return element.getLocalVariableUsages_SetVariableAction().stream()
				.flatMap(it -> it.getVariableCharacterisation_VariableUsage().stream()).toList();
	}

	@Override
	protected AbstractIFSEFFPCMVertex<SetVariableAction> createIFSEFFVertex(SetVariableAction element,
			List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
			List<Parameter> parameter, ResourceProvider resourceProvider) {
		return new IFSetVariableSEFFPCMVertex(element, previousElements, context, parameter, resourceProvider);
	}

}
