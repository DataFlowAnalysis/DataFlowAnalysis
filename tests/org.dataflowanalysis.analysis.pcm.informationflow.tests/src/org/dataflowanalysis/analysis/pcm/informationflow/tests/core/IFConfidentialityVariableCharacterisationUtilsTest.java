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

	private final DataDictionaryCharacterizedFactory ddcFac = DataDictionaryCharacterizedFactory.eINSTANCE;
	private final StoexFactory stoexFac = StoexFactory.eINSTANCE;;
	private final ExpressionsFactory expsFac = ExpressionsFactory.eINSTANCE;

	@Test
	void testCreateMaximumJoinOneLatticeOneReference() {
		EnumCharacteristicType characteristicType = createLattice("One");
		Enumeration enumeration = characteristicType.getType();

		var characterisedVariable = createCharacterisedVariableX();
		var dependencies = createDependencies("a");

		var confVarChars = IFConfidentialityVariableCharacterisationUtils.createMaximumJoinCharacterisationsForLattice(
				characterisedVariable, dependencies, characteristicType, enumeration);

		assertEquals(1, confVarChars.size(),
				"For a one point lattice there should be 1 characterisation. One for each level.");

		Map<String, String> varToLevel = new HashMap<>();
		varToLevel.put("a", "One");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(confVarChars, enumeration, varToLevel, "One"),
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

		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcForBooleanMapping(confVarChars.get(0), variableToLiteralToBoolean),
				"Should forward Low");
		assertFalse(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcForBooleanMapping(confVarChars.get(1), variableToLiteralToBoolean),
				"Should not set High, but forward Low");

		literalToBoolean.put("Low", false);
		literalToBoolean.put("High", true);
		variableToLiteralToBoolean.put("a", literalToBoolean);

		assertFalse(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcForBooleanMapping(confVarChars.get(0), variableToLiteralToBoolean),
				"Should not set Low, but forward High");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcForBooleanMapping(confVarChars.get(1), variableToLiteralToBoolean),
				"Should forward High");
	}

	@Test
	void testCreateMaximumJoinThreeLatticeTwoReferences() {
		EnumCharacteristicType characteristicType = createLattice("Low", "Mid", "High");
		Enumeration enumeration = characteristicType.getType();

		var characterised = createCharacterisedVariableX();
		var refs = createDependencies("a", "b");

		var confVarChars = IFConfidentialityVariableCharacterisationUtils
				.createMaximumJoinCharacterisationsForLattice(characterised, refs, characteristicType, enumeration);

		assertEquals(3, confVarChars.size(), "For Low, Mid and High each a ConfidentialityVariableCharacterisation");

		Map<String, String> varToLevel = new HashMap<>();
		varToLevel.put("a", "Low");
		varToLevel.put("b", "Low");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(confVarChars, enumeration, varToLevel, "Low"),
				"Should forward 'Low' for a=Low, b=Low");

		varToLevel.put("a", "Low");
		varToLevel.put("b", "Mid");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(confVarChars, enumeration, varToLevel, "Mid"),
				"Should forward 'Mid' for a=Low, b=Mid");

		varToLevel.put("a", "Mid");
		varToLevel.put("b", "Low");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(confVarChars, enumeration, varToLevel, "Mid"),
				"Should forward 'Mid' for a=Mid, b=Low");

		varToLevel.put("a", "Mid");
		varToLevel.put("b", "Mid");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(confVarChars, enumeration, varToLevel, "Mid"),
				"Should forward 'Mid' for a=Mid, b=Mid");

		varToLevel.put("a", "Low");
		varToLevel.put("b", "High");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(confVarChars, enumeration, varToLevel, "High"),
				"Should forward 'High' for a=Low, b=High");

		varToLevel.put("a", "Mid");
		varToLevel.put("b", "High");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(confVarChars, enumeration, varToLevel, "High"),
				"Should forward 'High' for a=Mid, b=High");

		varToLevel.put("a", "High");
		varToLevel.put("b", "Low");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(confVarChars, enumeration, varToLevel, "High"),
				"Should forward 'High' for a=High, b=Low");

		varToLevel.put("a", "High");
		varToLevel.put("b", "Mid");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(confVarChars, enumeration, varToLevel, "High"),
				"Should forward 'High' for a=High, b=Mid");

		varToLevel.put("a", "High");
		varToLevel.put("b", "High");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(confVarChars, enumeration, varToLevel, "High"),
				"Should forward 'High' for a=High, b=High");
	}

	@Test
	void testModifyWithConstraintFromMaximumJoin() {
		EnumCharacteristicType characteristicType = createLattice("Low", "Mid", "High");
		Enumeration enumeration = characteristicType.getType();

		var characterisedVariable = createCharacterisedVariableX();
		var dependencies = createDependencies("a");

		// Assumes createMaximumJoin to work
		var initialConfChars = IFConfidentialityVariableCharacterisationUtils
				.createMaximumJoinCharacterisationsForLattice(characterisedVariable, dependencies, characteristicType,
						enumeration);

		var constraint = createDependencies("securityContext").get(0);
		var modifiedConfChars = IFConfidentialityVariableCharacterisationUtils
				.createModifiedCharacterisationsForAdditionalHigherEqualConstraint(initialConfChars, constraint,
						characteristicType, enumeration);

		assertEquals(3, modifiedConfChars.size(),
				"For Low, Mid and High each a ConfidentialityVariableCharacterisation");

		// TODO assertEquals initialConfChars with newlyGenerated initialConfChars?

		Map<String, String> varToLevel = new HashMap<>();

		varToLevel.put("a", "Low");
		varToLevel.put("securityContext", "Low");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(modifiedConfChars, enumeration, varToLevel, "Low"),
				"Should set 'Low' for a=Low, securityContext=Low");

		varToLevel.put("a", "Mid");
		varToLevel.put("securityContext", "Low");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(modifiedConfChars, enumeration, varToLevel, "Mid"),
				"Should set 'Mid' for a=Mid, securityContext=Low");

		varToLevel.put("a", "High");
		varToLevel.put("securityContext", "Low");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(modifiedConfChars, enumeration, varToLevel, "High"),
				"Should set 'High' for a=High, securityContext=Low");

		varToLevel.put("a", "Low");
		varToLevel.put("securityContext", "Mid");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(modifiedConfChars, enumeration, varToLevel, "Mid"),
				"Should set 'Mid' for a=Low, securityContext=Mid");

		varToLevel.put("a", "Mid");
		varToLevel.put("securityContext", "Mid");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(modifiedConfChars, enumeration, varToLevel, "Mid"),
				"Should set 'Mid' for a=Mid, securityContext=Mid");

		varToLevel.put("a", "High");
		varToLevel.put("securityContext", "Mid");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(modifiedConfChars, enumeration, varToLevel, "High"),
				"Should set 'High' for a=High, securityContext=Mid");

		varToLevel.put("a", "Low");
		varToLevel.put("securityContext", "High");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(modifiedConfChars, enumeration, varToLevel, "High"),
				"Should set 'High' for a=Low, securityContext=High");

		varToLevel.put("a", "Mid");
		varToLevel.put("securityContext", "High");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(modifiedConfChars, enumeration, varToLevel, "High"),
				"Should set 'High' for a=Mid, securityContext=High");

		varToLevel.put("a", "High");
		varToLevel.put("securityContext", "High");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(modifiedConfChars, enumeration, varToLevel, "High"),
				"Should set 'High' for a=High, securityContext=High");
	}

	@Test
	void testModifyWithConstraintWithOneLevelSpecifiedMidTrue() {
		EnumCharacteristicType characteristicType = createLattice("Low", "Mid", "High");
		Enumeration enumeration = characteristicType.getType();

		var characterisedVariable = createCharacterisedVariableX();
		var dependencies = createDependencies("a");

		// Assumes createMaximumJoin to work
		var initialConfChars = IFConfidentialityVariableCharacterisationUtils
				.createMaximumJoinCharacterisationsForLattice(characterisedVariable, dependencies, characteristicType,
						enumeration);
		var midConfChar = initialConfChars.get(1);
		midConfChar.setRhs(expsFac.createTrue());
		initialConfChars = List.of(midConfChar);

		var constraint = createDependencies("securityContext").get(0);
		var modifiedConfChars = IFConfidentialityVariableCharacterisationUtils
				.createModifiedCharacterisationsForAdditionalHigherEqualConstraint(initialConfChars, constraint,
						characteristicType, enumeration);

		assertEquals(3, modifiedConfChars.size(),
				"For Low, Mid and High each a ConfidentialityVariableCharacterisation");

		Map<String, String> varToLevel = new HashMap<>();

		varToLevel.put("securityContext", "Low");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(modifiedConfChars, enumeration, varToLevel, "Mid"),
				"Should set 'Mid' for Litteral.Mid := true, securityContext=Low");

		varToLevel.put("securityContext", "Mid");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(modifiedConfChars, enumeration, varToLevel, "Mid"),
				"Should set 'Mid' for Litteral.Mid := true, securityContext=Mid");

		varToLevel.put("securityContext", "High");
		assertTrue(ConfidentialityVariableCharacterisationTestUtils.evaluateCvcLatticeMapping(modifiedConfChars, enumeration, varToLevel, "High"),
				"Should set 'High' for Litteral.Mid := true, securityContext=High");
	}

	/*
	 * Help Methods
	 */

	private EnumCharacteristicType createLattice(String... names) {
		return createEnumCharacteristicType("Lattice", names);
	}

	private EnumCharacteristicType createEnumCharacteristicType(String ctName, String... latticeNames) {
		Enumeration lattice = ddcFac.createEnumeration();
		lattice.setName(ctName);

		for (String latticeName : latticeNames) {
			addLiteral(latticeName, lattice);
		}

		EnumCharacteristicType latticeCharacteristicType = ddcFac.createEnumCharacteristicType();
		latticeCharacteristicType.setType(lattice);
		latticeCharacteristicType.setName(ctName);
		return latticeCharacteristicType;
	}

	private Literal addLiteral(String name, Enumeration lattice) {
		Literal literal = ddcFac.createLiteral();
		literal.setEnum(lattice);
		literal.setName(name);
		return literal;
	}

	private AbstractNamedReference createCharacterisedVariableX() {
		VariableReference x = stoexFac.createVariableReference();
		x.setReferenceName("x");
		return x;
	}

	private List<AbstractNamedReference> createDependencies(String... names) {
		List<AbstractNamedReference> dependencies = new ArrayList<>(names.length);
		for (String name : names) {
			VariableReference ref = stoexFac.createVariableReference();
			ref.setReferenceName(name);
			dependencies.add(ref);
		}
		return dependencies;
	}

}
