package org.dataflowanalysis.analysis.pcm.informationflow.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.CharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Enumeration;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.dataflowanalysis.pcm.extension.model.confidentiality.dictionary.DictionaryPackage;
import org.dataflowanalysis.pcm.extension.model.confidentiality.dictionary.PCMDataDictionary;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;

import de.uka.ipd.sdq.stoex.AbstractNamedReference;

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

	// TODO Eventually also needs to handle DataFlowVariables?

	// TODO What if there are VariableCharacterisations from different
	// VariableUsages with different Variables? Write assumption only one in
	// specification? Should be handled somewhere (here or in the Element classes).
	// Probably here.

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
	 * defined characterizations. Assumes all characterizations to be for the same
	 * variable.
	 * 
	 * @param allCharacterisations the defined characterizations
	 * @return the effective characterizations
	 */
	public List<ConfidentialityVariableCharacterisation> calculateEffectiveConfidentialityVariableCharacterisation(
			List<VariableCharacterisation> allCharacterisations) {

		List<ConfidentialityVariableCharacterisation> confChars = new ArrayList<ConfidentialityVariableCharacterisation>();
		List<VariableCharacterisation> normalChars = new ArrayList<VariableCharacterisation>();

		for (VariableCharacterisation characterisation : allCharacterisations) {
			if (characterisation instanceof ConfidentialityVariableCharacterisation) {
				confChars.add((ConfidentialityVariableCharacterisation) characterisation);
			} else {
				normalChars.add(characterisation);
			}
		}

		return calculateEffectiveConfidentialityVariableCharacterisation(confChars, normalChars);
	}

	/**
	 * Calculates effective {@link ConfidentialityVariableCharacterisation}s from
	 * defined characterizations. Assumes all characterizations to be for the same
	 * variable.
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

		List<ConfidentialityVariableCharacterisation> calculatedCharacterisations = new ArrayList<>();
		if (normalCharacterisations.size() > 0) {
			AbstractNamedReference characterisedVariable = normalCharacterisations.get(0)
					.getVariableUsage_VariableCharacterisation().getNamedReference__VariableUsage();
			var dependencies = extractVariableDependencies(normalCharacterisations);

			calculatedCharacterisations = calculateConfidentialityVariableCharacteristationsFromReferences(dependencies,
					characterisedVariable);
		}

		return calculateResultingConfidentialityVaraibleCharacterisations(calculatedCharacterisations,
				confidentialityCharacterisations);

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
			List<ConfidentialityVariableCharacterisation> definedCharacterisations);

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
