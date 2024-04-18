package org.dataflowanalysis.analysis.pcm.informationflow.core.extraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.pcm.informationflow.core.utils.IFConfidentialityVariableCharacterisationUtils;
import org.dataflowanalysis.analysis.pcm.informationflow.core.utils.IFPCMDataDictionaryUtils;
import org.dataflowanalysis.analysis.pcm.informationflow.core.utils.IFStoexUtils;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.CharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Enumeration;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;

import de.uka.ipd.sdq.stoex.AbstractNamedReference;
import de.uka.ipd.sdq.stoex.Expression;

/**
 * An IFPCMExtractionStrategy calculates
 * {@link ConfidentialityVariableCharacterisation}s for a lattice. The
 * calculated {@link ConfidentialityVariableCharacterisation} represent
 * effective constraints and are derived from the given parameters following a
 * concrete strategy.
 *
 */
public abstract class IFPCMExtractionStrategy {

	private ResourceProvider resourceProvider;
	private Enumeration lattice;
	private CharacteristicType latticeCharacteristicType;
	private boolean initialized;

	/**
	 * Creates an IFPCMExtractionStrategy for the given resourceProvider
	 * 
	 * @param resourceProvider the resourceProvider
	 */
	public IFPCMExtractionStrategy(ResourceProvider resourceProvider) {
		this.resourceProvider = resourceProvider;
		this.initialized = false;
	}

	/**
	 * Calculates effective {@link ConfidentialityVariableCharacterisation}s from
	 * defined characterizations.
	 * 
	 * @param allCharacterisations the defined characterizations
	 * @return the effective characterizations
	 */
	public List<ConfidentialityVariableCharacterisation> calculateEffectiveConfidentialityVariableCharacterisation(
			List<VariableCharacterisation> allCharacterisations) {

		return calculateEffectiveConfidentialityVariableCharacterisation(allCharacterisations, Optional.empty());
	}

	/**
	 * Calculates effective {@link ConfidentialityVariableCharacterisation}s from
	 * defined characterizations.
	 * 
	 * @param confidentialityCharacterisations the defined
	 *                                         {@link ConfidentialityVariableCharacterisation}s
	 * @param normalCharacterisations          the defined characterizations which
	 *                                         are not
	 *                                         {@link ConfidentialityVariableCharacterisation}s
	 * @return the effective characterizations
	 */
	public List<ConfidentialityVariableCharacterisation> calculateEffectiveConfidentialityVariableCharacterisation(
			List<ConfidentialityVariableCharacterisation> confidentialityCharacterisations,
			List<VariableCharacterisation> normalCharacterisations) {

		return calculateEffectiveConfidentialityVariableCharacterisation(confidentialityCharacterisations,
				normalCharacterisations, Optional.empty());
	}

	/**
	 * Calculates effective {@link ConfidentialityVariableCharacterisation}s from
	 * defined characterizations and a security context.
	 * 
	 * @param allCharacterisations the defined characterizations
	 * @param securityContext      the security context
	 * @return the effective characterizations
	 */
	public List<ConfidentialityVariableCharacterisation> calculateEffectiveConfidentialityVariableCharacterisation(
			List<VariableCharacterisation> allCharacterisations, DataFlowVariable securityContext) {
		return calculateEffectiveConfidentialityVariableCharacterisation(allCharacterisations,
				Optional.of(securityContext));
	}

	/**
	 * Calculates effective {@link ConfidentialityVariableCharacterisation}s from
	 * defined characterizations and a security context.
	 * 
	 * @param confidentialityCharacterisations the defined
	 *                                         {@link ConfidentialityVariableCharacterisation}s
	 * @param normalCharacterisations          the defined characterizations which
	 *                                         are not
	 *                                         {@link ConfidentialityVariableCharacterisation}s
	 * @param securityContext                  the security context
	 * @return the effective characterizations
	 */
	public List<ConfidentialityVariableCharacterisation> calculateEffectiveConfidentialityVariableCharacterisation(
			List<ConfidentialityVariableCharacterisation> confidentialityCharacterisations,
			List<VariableCharacterisation> normalCharacterisations, DataFlowVariable securityContext) {
		return calculateEffectiveConfidentialityVariableCharacterisation(confidentialityCharacterisations,
				normalCharacterisations, Optional.of(securityContext));
	}

	/**
	 * Calculates {@link ConfidentialityVariableCharacterisation}s from an
	 * expression for a variable.
	 * 
	 * @param variable            the variable
	 * @param dependentExpression the expression
	 * @return the characterization
	 */
	public List<ConfidentialityVariableCharacterisation> calculateConfidentialityVariableCharacterisationForExpression(
			String variable, Expression dependentExpression, Optional<DataFlowVariable> optionalSecurityContext) {

		AbstractNamedReference variableRef = IFStoexUtils.createReferenceFromName(variable);
		var dependencies = IFStoexUtils.findVariablesInExpression(dependentExpression).stream()
				.map(it -> it.getId_Variable()).toList();
		if (optionalSecurityContext.isPresent()) {
			String securityContextName = optionalSecurityContext.get().getVariableName();
			dependencies.add(IFStoexUtils.createReferenceFromName(securityContextName));
		}
		return calculateConfidentialityVariableCharacteristationsFromReferences(dependencies, variableRef);
	}

	/**
	 * Calculates effective {@link ConfidentialityVariableCharacterisation}s from
	 * defined characterizations and if present a security context.
	 * 
	 * @param allCharacterisations    the defined characterizations
	 * @param optionalSecurityContext the security context
	 * @return the effective characterizations
	 */
	public List<ConfidentialityVariableCharacterisation> calculateEffectiveConfidentialityVariableCharacterisation(
			List<VariableCharacterisation> allCharacterisations, Optional<DataFlowVariable> optionalSecurityContext) {

		List<ConfidentialityVariableCharacterisation> confChars = new ArrayList<ConfidentialityVariableCharacterisation>();
		List<VariableCharacterisation> normalChars = new ArrayList<VariableCharacterisation>();

		for (VariableCharacterisation characterisation : allCharacterisations) {
			if (characterisation instanceof ConfidentialityVariableCharacterisation) {
				confChars.add((ConfidentialityVariableCharacterisation) characterisation);
			} else {
				normalChars.add(characterisation);
			}
		}

		return calculateEffectiveConfidentialityVariableCharacterisation(confChars, normalChars,
				optionalSecurityContext);
	}

	/**
	 * Calculates effective {@link ConfidentialityVariableCharacterisation}s from
	 * defined characterizations and if present a security context.
	 * 
	 * @param confidentialityCharacterisations the defined
	 *                                         {@link ConfidentialityVariableCharacterisation}s
	 * @param normalCharacterisations          the defined characterizations which
	 *                                         are not
	 *                                         {@link ConfidentialityVariableCharacterisation}s
	 * @param optionalSecurityContext          the security context
	 * @return the effective characterizations
	 */
	public List<ConfidentialityVariableCharacterisation> calculateEffectiveConfidentialityVariableCharacterisation(
			List<ConfidentialityVariableCharacterisation> confidentialityCharacterisations,
			List<VariableCharacterisation> normalCharacterisations,
			Optional<DataFlowVariable> optionalSecurityContext) {

		var variableNameToNormalChars = mapVariableNameToVarChar(normalCharacterisations);
		var variableNameToConfChars = mapVariableNameToVarChar(confidentialityCharacterisations);

		Set<String> characterisedVariableNames = new HashSet<>(variableNameToNormalChars.keySet());
		characterisedVariableNames.addAll(variableNameToConfChars.keySet());

		List<ConfidentialityVariableCharacterisation> allResultingCvcs = new ArrayList<>();
		for (String characterisedVariableName : characterisedVariableNames) {
			var normalCharsForVariable = variableNameToNormalChars.get(characterisedVariableName);
			if (normalCharsForVariable == null) {
				normalCharsForVariable = new ArrayList<>();
			}
			var confCharsForVariable = variableNameToConfChars.get(characterisedVariableName);
			if (confCharsForVariable == null) {
				confCharsForVariable = new ArrayList<>();
			}

			var resultingCvcs = calculateEffectiveCvcForVariable(confCharsForVariable, normalCharsForVariable,
					optionalSecurityContext);
			allResultingCvcs.addAll(resultingCvcs);
		}
		return allResultingCvcs;

	}

	private List<ConfidentialityVariableCharacterisation> calculateEffectiveCvcForVariable(
			List<ConfidentialityVariableCharacterisation> confidentialityCharacterisations,
			List<VariableCharacterisation> normalCharacterisations,
			Optional<DataFlowVariable> optionalSecurityContext) {

		List<ConfidentialityVariableCharacterisation> calculatedCharacterisations = new ArrayList<>();
		if (normalCharacterisations.size() > 0) {
			AbstractNamedReference characterisedVariable = normalCharacterisations.get(0)
					.getVariableUsage_VariableCharacterisation().getNamedReference__VariableUsage();
			var dependencies = extractVariableDependencies(normalCharacterisations);
			if (optionalSecurityContext.isPresent()) {
				String securityContextName = optionalSecurityContext.get().getVariableName();
				dependencies.add(IFStoexUtils.createReferenceFromName(securityContextName));
			}

			calculatedCharacterisations = calculateConfidentialityVariableCharacteristationsFromReferences(dependencies,
					characterisedVariable);
		}

		return calculateResultingConfidentialityVaraibleCharacterisations(calculatedCharacterisations,
				confidentialityCharacterisations, optionalSecurityContext);
	}

	private <T extends VariableCharacterisation> Map<String, List<T>> mapVariableNameToVarChar(List<T> varChars) {
		Map<String, List<T>> variableNameToVarChars = new HashMap<>();
		for (var varChar : varChars) {
			String variableName = varChar.getVariableUsage_VariableCharacterisation().getNamedReference__VariableUsage()
					.getReferenceName();
			var mappedVarChars = variableNameToVarChars.get(variableName);

			if (mappedVarChars == null) {
				mappedVarChars = new ArrayList<>();
			}
			mappedVarChars.add(varChar);
			variableNameToVarChars.put(variableName, mappedVarChars);
		}
		return variableNameToVarChars;
	}

	private List<AbstractNamedReference> extractVariableDependencies(
			List<VariableCharacterisation> variableCharacterisations) {
		return variableCharacterisations.stream()
				.flatMap(varChar -> IFStoexUtils
						.findVariablesInExpression(varChar.getSpecification_VariableCharacterisation().getExpression())
						.stream().map(dependency -> dependency.getId_Variable()))
				.toList();
	}

	private List<ConfidentialityVariableCharacterisation> calculateConfidentialityVariableCharacteristationsFromReferences(
			List<AbstractNamedReference> references, AbstractNamedReference characterisedVariable) {

		// Set the lowest level if the VariableCharacterisation contains no variables
		if (references.size() <= 0) {
			return IFConfidentialityVariableCharacterisationUtils.createSetLowestLevelCharacterisationsForLattice(
					characterisedVariable, getLatticeCharacteristicType(), getLattice());
		}

		return IFConfidentialityVariableCharacterisationUtils.createMaximumJoinCharacterisationsForLattice(
				characterisedVariable, references, getLatticeCharacteristicType(), getLattice());
	}

	/**
	 * Determines the resulting effective
	 * {@link ConfidentialityVariableCharacterisation} from calculated
	 * characterizations, defined characterizations and optionally an security
	 * context.
	 * 
	 * @param calculatedCharacterisations the calculated characterizations
	 * @param definedCharacterisations    the defined characterizations
	 * @param optionalSecurityContext     the optional security context
	 * @return the resulting characterizations
	 */
	protected abstract List<ConfidentialityVariableCharacterisation> calculateResultingConfidentialityVaraibleCharacterisations(
			List<ConfidentialityVariableCharacterisation> calculatedCharacterisations,
			List<ConfidentialityVariableCharacterisation> definedCharacterisations,
			Optional<DataFlowVariable> optionalSecurityContext);

	/**
	 * Returns the lattice used for the extraction.
	 * 
	 * @return the lattice used for the extraction
	 */
	protected Enumeration getLattice() {
		if (!initialized) {
			initializeResources();
		}
		return lattice;
	}

	/**
	 * Returns the CharacteristicType used for the extraction.
	 * 
	 * @return the CharacteristicType used for the extraction
	 */
	protected CharacteristicType getLatticeCharacteristicType() {
		if (!initialized) {
			initializeResources();
		}
		return latticeCharacteristicType;
	}

	/**
	 * Returns the ResourceProvider used for the extraction.
	 * 
	 * @return the ResourceProvider used for the extraction
	 */
	protected ResourceProvider getResourceProvider() {
		return resourceProvider;
	}

	private void initializeResources() {
		latticeCharacteristicType = IFPCMDataDictionaryUtils.getLatticeCharacteristicType(resourceProvider);
		lattice = IFPCMDataDictionaryUtils.getLatticeEnumeration(resourceProvider);
	}
}
