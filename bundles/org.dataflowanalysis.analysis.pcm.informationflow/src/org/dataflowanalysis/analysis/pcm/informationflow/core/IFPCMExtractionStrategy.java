package org.dataflowanalysis.analysis.pcm.informationflow.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.CharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Enumeration;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.dataflowanalysis.pcm.extension.model.confidentiality.dictionary.DictionaryPackage;
import org.dataflowanalysis.pcm.extension.model.confidentiality.dictionary.PCMDataDictionary;
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

	private final static String LATTICE_CHARACTERISTIC_TYPE_NAME = "Lattice";
	private final static String LATTICE_NAME = LATTICE_CHARACTERISTIC_TYPE_NAME;

	private Logger logger = Logger.getLogger(IFPCMExtractionStrategy.class);
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
			String variable, Expression dependentExpression) {

		AbstractNamedReference variableRef = IFStoexUtils.createReferenceFromName(variable);
		var dependencies = IFStoexUtils.findVariablesInExpression(dependentExpression).stream()
				.map(it -> it.getId_Variable()).toList();
		return calculateConfidentialityVariableCharacteristationsFromReferences(dependencies, variableRef);
	}

	private List<ConfidentialityVariableCharacterisation> calculateEffectiveConfidentialityVariableCharacterisation(
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

	private List<ConfidentialityVariableCharacterisation> calculateEffectiveConfidentialityVariableCharacterisation(
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

	protected List<AbstractNamedReference> extractVariableDependencies(
			List<VariableCharacterisation> variableCharacterisations) {
		return variableCharacterisations.stream()
				.flatMap(varChar -> IFStoexUtils
						.findVariablesInExpression(varChar.getSpecification_VariableCharacterisation().getExpression())
						.stream().map(dependency -> dependency.getId_Variable()))
				.toList();
	}

	protected List<ConfidentialityVariableCharacterisation> calculateConfidentialityVariableCharacteristationsFromReferences(
			List<AbstractNamedReference> references, AbstractNamedReference characterisedVariable) {

		return IFConfidentialityVariableCharacterisationUtils.createMaximumJoinCharacterisationsForLattice(
				characterisedVariable, references, getLatticeCharacteristicType(), getLattice());
	}

	protected abstract List<ConfidentialityVariableCharacterisation> calculateResultingConfidentialityVaraibleCharacterisations(
			List<ConfidentialityVariableCharacterisation> calculatedCharacterisations,
			List<ConfidentialityVariableCharacterisation> definedCharacterisations,
			Optional<DataFlowVariable> optionalSecurityContext);

	protected Enumeration getLattice() {
		if (!initialized) {
			initializeResources();
		}
		return lattice;
	}

	protected CharacteristicType getLatticeCharacteristicType() {
		if (!initialized) {
			initializeResources();
		}
		return latticeCharacteristicType;
	}

	private void initializeResources() {
		var diccs = resourceProvider.lookupToplevelElement(DictionaryPackage.eINSTANCE.getPCMDataDictionary()).stream()
				.filter(PCMDataDictionary.class::isInstance).map(PCMDataDictionary.class::cast).toList();
		for (PCMDataDictionary dicc : diccs) {
			var lattice = dicc.getCharacteristicEnumerations().stream().filter(l -> l.getName().matches(LATTICE_NAME))
					.findFirst();
			var latticeCharacteristicType = dicc.getCharacteristicTypes().stream()
					.filter(charType -> charType.getName().matches(LATTICE_CHARACTERISTIC_TYPE_NAME)).findFirst();
			if (lattice.isPresent() && latticeCharacteristicType.isPresent()) {
				this.lattice = lattice.get();
				this.latticeCharacteristicType = latticeCharacteristicType.get();
				return;
			}
		}

		String errorMsg = "No pddc present with 'Lattice' as CharacteristicType and Enumeration";
		logger.error(errorMsg);
		throw new IllegalStateException(errorMsg);
	}
}
