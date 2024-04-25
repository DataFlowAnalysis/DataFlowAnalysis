package org.dataflowanalysis.analysis.pcm.informationflow.tests.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.pcm.informationflow.core.utils.IFConfidentialityVariableCharacterisationUtils;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.DataDictionaryCharacterizedFactory;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Enumeration;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.ExpressionsFactory;
import org.junit.jupiter.api.Test;

import de.uka.ipd.sdq.stoex.AbstractNamedReference;
import de.uka.ipd.sdq.stoex.StoexFactory;
import de.uka.ipd.sdq.stoex.VariableReference;

class IFConfidentialityVariableCharacterisationUtilsTest {

//	private Logger logger = Logger.getLogger(IFConfidentialityVariableCharacterisationUtilsTest.class);

	private final DataDictionaryCharacterizedFactory ddcFactory = DataDictionaryCharacterizedFactory.eINSTANCE;
	private final StoexFactory stoexFactory = StoexFactory.eINSTANCE;;
	private final ExpressionsFactory expressionsFactory = ExpressionsFactory.eINSTANCE;

	@Test
	void testCreateMaximumJoinOneLatticeOneReference() {
		EnumCharacteristicType characteristicType = createLattice("One");
		Enumeration enumeration = characteristicType.getType();

		var characterisedVariable = createCharacterisedVariableX();
		var dependencies = createDependencies("a");

		var confidentialityCharacterisation = IFConfidentialityVariableCharacterisationUtils
				.createMaximumJoinCharacterisationsForLattice(characterisedVariable, dependencies, characteristicType,
						enumeration);

		assertEquals(1, confidentialityCharacterisation.size(),
				"For a one point lattice there should be 1 characterisation. One for each level.");

		Map<String, String> varToLevel = new HashMap<>();
		varToLevel.put("a", "One");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						confidentialityCharacterisation, enumeration, varToLevel, "One"),
				"Should forward 'One'");
	}

	@Test
	void testCreateMaximumJoinTwoLatticeOneReference() {
		EnumCharacteristicType characteristicType = createLattice("Low", "High");
		Enumeration enumeration = characteristicType.getType();

		var characterisedVariable = createCharacterisedVariableX();
		var dependencies = createDependencies("a");

		var confVarChars = IFConfidentialityVariableCharacterisationUtils.createMaximumJoinCharacterisationsForLattice(
				characterisedVariable, dependencies, characteristicType, enumeration);

		assertEquals(2, confVarChars.size(),
				"For a two point lattice there should be 2 characterisations. One for each level.");

		Map<String, Boolean> literalToBoolean = new HashMap<>();
		literalToBoolean.put("Low", true);
		literalToBoolean.put("High", false);
		Map<String, Map<String, Boolean>> variableToLiteralToBoolean = new HashMap<>();
		variableToLiteralToBoolean.put("a", literalToBoolean);

		assertTrue(ConfidentialityVariableCharacterisationTestUtils
				.evaluateConfidentialityCharacterisationForBooleanMapping(confVarChars.get(0),
						variableToLiteralToBoolean),
				"Should forward Low");
		assertFalse(ConfidentialityVariableCharacterisationTestUtils
				.evaluateConfidentialityCharacterisationForBooleanMapping(confVarChars.get(1),
						variableToLiteralToBoolean),
				"Should not set High, but forward Low");

		literalToBoolean.put("Low", false);
		literalToBoolean.put("High", true);
		variableToLiteralToBoolean.put("a", literalToBoolean);

		assertFalse(ConfidentialityVariableCharacterisationTestUtils
				.evaluateConfidentialityCharacterisationForBooleanMapping(confVarChars.get(0),
						variableToLiteralToBoolean),
				"Should not set Low, but forward High");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils
				.evaluateConfidentialityCharacterisationForBooleanMapping(confVarChars.get(1),
						variableToLiteralToBoolean),
				"Should forward High");
	}

	@Test
	void testCreateMaximumJoinThreeLatticeTwoReferences() {
		EnumCharacteristicType characteristicType = createLattice("Low", "Mid", "High");
		Enumeration enumeration = characteristicType.getType();

		var characterised = createCharacterisedVariableX();
		var references = createDependencies("a", "b");

		var confidentialityCharacterisations = IFConfidentialityVariableCharacterisationUtils
				.createMaximumJoinCharacterisationsForLattice(characterised, references, characteristicType,
						enumeration);

		assertEquals(3, confidentialityCharacterisations.size(),
				"For Low, Mid and High each a ConfidentialityVariableCharacterisation");

		Map<String, String> variableToLevel = new HashMap<>();
		variableToLevel.put("a", "Low");
		variableToLevel.put("b", "Low");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						confidentialityCharacterisations, enumeration, variableToLevel, "Low"),
				"Should forward 'Low' for a=Low, b=Low");

		variableToLevel.put("a", "Low");
		variableToLevel.put("b", "Mid");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						confidentialityCharacterisations, enumeration, variableToLevel, "Mid"),
				"Should forward 'Mid' for a=Low, b=Mid");

		variableToLevel.put("a", "Mid");
		variableToLevel.put("b", "Low");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						confidentialityCharacterisations, enumeration, variableToLevel, "Mid"),
				"Should forward 'Mid' for a=Mid, b=Low");

		variableToLevel.put("a", "Mid");
		variableToLevel.put("b", "Mid");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						confidentialityCharacterisations, enumeration, variableToLevel, "Mid"),
				"Should forward 'Mid' for a=Mid, b=Mid");

		variableToLevel.put("a", "Low");
		variableToLevel.put("b", "High");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						confidentialityCharacterisations, enumeration, variableToLevel, "High"),
				"Should forward 'High' for a=Low, b=High");

		variableToLevel.put("a", "Mid");
		variableToLevel.put("b", "High");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						confidentialityCharacterisations, enumeration, variableToLevel, "High"),
				"Should forward 'High' for a=Mid, b=High");

		variableToLevel.put("a", "High");
		variableToLevel.put("b", "Low");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						confidentialityCharacterisations, enumeration, variableToLevel, "High"),
				"Should forward 'High' for a=High, b=Low");

		variableToLevel.put("a", "High");
		variableToLevel.put("b", "Mid");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						confidentialityCharacterisations, enumeration, variableToLevel, "High"),
				"Should forward 'High' for a=High, b=Mid");

		variableToLevel.put("a", "High");
		variableToLevel.put("b", "High");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						confidentialityCharacterisations, enumeration, variableToLevel, "High"),
				"Should forward 'High' for a=High, b=High");
	}

	@Test
	void testModifyWithConstraintFromMaximumJoin() {
		EnumCharacteristicType characteristicType = createLattice("Low", "Mid", "High");
		Enumeration enumeration = characteristicType.getType();

		var characterisedVariable = createCharacterisedVariableX();
		var dependencies = createDependencies("a");

		// Assumes createMaximumJoin to work
		var initialConfidentialityCharacterisations = IFConfidentialityVariableCharacterisationUtils
				.createMaximumJoinCharacterisationsForLattice(characterisedVariable, dependencies, characteristicType,
						enumeration);

		var constraint = createDependencies("securityContext").get(0);
		var modifiedConfidentialityCharacterisations = IFConfidentialityVariableCharacterisationUtils
				.createModifiedCharacterisationsForAdditionalHigherEqualConstraint(
						initialConfidentialityCharacterisations, constraint, characteristicType, enumeration);

		assertEquals(3, modifiedConfidentialityCharacterisations.size(),
				"For Low, Mid and High each a ConfidentialityVariableCharacterisation");

		// TODO assertEquals initialConfChars with newlyGenerated initialConfChars?

		Map<String, String> variableToLevel = new HashMap<>();

		variableToLevel.put("a", "Low");
		variableToLevel.put("securityContext", "Low");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						modifiedConfidentialityCharacterisations, enumeration, variableToLevel, "Low"),
				"Should set 'Low' for a=Low, securityContext=Low");

		variableToLevel.put("a", "Mid");
		variableToLevel.put("securityContext", "Low");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						modifiedConfidentialityCharacterisations, enumeration, variableToLevel, "Mid"),
				"Should set 'Mid' for a=Mid, securityContext=Low");

		variableToLevel.put("a", "High");
		variableToLevel.put("securityContext", "Low");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						modifiedConfidentialityCharacterisations, enumeration, variableToLevel, "High"),
				"Should set 'High' for a=High, securityContext=Low");

		variableToLevel.put("a", "Low");
		variableToLevel.put("securityContext", "Mid");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						modifiedConfidentialityCharacterisations, enumeration, variableToLevel, "Mid"),
				"Should set 'Mid' for a=Low, securityContext=Mid");

		variableToLevel.put("a", "Mid");
		variableToLevel.put("securityContext", "Mid");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						modifiedConfidentialityCharacterisations, enumeration, variableToLevel, "Mid"),
				"Should set 'Mid' for a=Mid, securityContext=Mid");

		variableToLevel.put("a", "High");
		variableToLevel.put("securityContext", "Mid");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						modifiedConfidentialityCharacterisations, enumeration, variableToLevel, "High"),
				"Should set 'High' for a=High, securityContext=Mid");

		variableToLevel.put("a", "Low");
		variableToLevel.put("securityContext", "High");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						modifiedConfidentialityCharacterisations, enumeration, variableToLevel, "High"),
				"Should set 'High' for a=Low, securityContext=High");

		variableToLevel.put("a", "Mid");
		variableToLevel.put("securityContext", "High");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						modifiedConfidentialityCharacterisations, enumeration, variableToLevel, "High"),
				"Should set 'High' for a=Mid, securityContext=High");

		variableToLevel.put("a", "High");
		variableToLevel.put("securityContext", "High");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						modifiedConfidentialityCharacterisations, enumeration, variableToLevel, "High"),
				"Should set 'High' for a=High, securityContext=High");
	}

	@Test
	void testModifyWithConstraintWithOneLevelSpecifiedMidTrue() {
		EnumCharacteristicType characteristicType = createLattice("Low", "Mid", "High");
		Enumeration enumeration = characteristicType.getType();

		var characterisedVariable = createCharacterisedVariableX();
		var dependencies = createDependencies("a");

		// Assumes createMaximumJoin to work
		var initialConfidentialityCharacterisations = IFConfidentialityVariableCharacterisationUtils
				.createMaximumJoinCharacterisationsForLattice(characterisedVariable, dependencies, characteristicType,
						enumeration);
		var middleConfidentialityCharacterisation = initialConfidentialityCharacterisations.get(1);
		middleConfidentialityCharacterisation.setRhs(expressionsFactory.createTrue());
		initialConfidentialityCharacterisations = List.of(middleConfidentialityCharacterisation);

		var constraint = createDependencies("securityContext").get(0);
		var modifiedConfidentialityCharacterisations = IFConfidentialityVariableCharacterisationUtils
				.createModifiedCharacterisationsForAdditionalHigherEqualConstraint(
						initialConfidentialityCharacterisations, constraint, characteristicType, enumeration);

		assertEquals(3, modifiedConfidentialityCharacterisations.size(),
				"For Low, Mid and High each a ConfidentialityVariableCharacterisation");

		Map<String, String> variableToLevel = new HashMap<>();

		variableToLevel.put("securityContext", "Low");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						modifiedConfidentialityCharacterisations, enumeration, variableToLevel, "Mid"),
				"Should set 'Mid' for Litteral.Mid := true, securityContext=Low");

		variableToLevel.put("securityContext", "Mid");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						modifiedConfidentialityCharacterisations, enumeration, variableToLevel, "Mid"),
				"Should set 'Mid' for Litteral.Mid := true, securityContext=Mid");

		variableToLevel.put("securityContext", "High");
		assertTrue(
				ConfidentialityVariableCharacterisationTestUtils.evaluateConfidentialityCharacterisationLatticeMapping(
						modifiedConfidentialityCharacterisations, enumeration, variableToLevel, "High"),
				"Should set 'High' for Litteral.Mid := true, securityContext=High");
	}

	/*
	 * Help Methods
	 */

	private EnumCharacteristicType createLattice(String... names) {
		return createEnumCharacteristicType("Lattice", names);
	}

	private EnumCharacteristicType createEnumCharacteristicType(String ctName, String... latticeNames) {
		Enumeration lattice = ddcFactory.createEnumeration();
		lattice.setName(ctName);

		for (String latticeName : latticeNames) {
			addLiteral(latticeName, lattice);
		}

		EnumCharacteristicType latticeCharacteristicType = ddcFactory.createEnumCharacteristicType();
		latticeCharacteristicType.setType(lattice);
		latticeCharacteristicType.setName(ctName);
		return latticeCharacteristicType;
	}

	private Literal addLiteral(String name, Enumeration lattice) {
		Literal literal = ddcFactory.createLiteral();
		literal.setEnum(lattice);
		literal.setName(name);
		return literal;
	}

	private AbstractNamedReference createCharacterisedVariableX() {
		VariableReference x = stoexFactory.createVariableReference();
		x.setReferenceName("x");
		return x;
	}

	private List<AbstractNamedReference> createDependencies(String... names) {
		List<AbstractNamedReference> dependencies = new ArrayList<>(names.length);
		for (String name : names) {
			VariableReference reference = stoexFactory.createVariableReference();
			reference.setReferenceName(name);
			dependencies.add(reference);
		}
		return dependencies;
	}

}
