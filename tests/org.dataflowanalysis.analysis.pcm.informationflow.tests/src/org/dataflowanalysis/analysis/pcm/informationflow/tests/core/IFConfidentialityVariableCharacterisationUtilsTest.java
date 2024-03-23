package org.dataflowanalysis.analysis.pcm.informationflow.tests.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.pcm.informationflow.core.IFConfidentialityVariableCharacterisationUtils;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.DataDictionaryCharacterizedFactory;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Enumeration;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.junit.jupiter.api.Test;

import de.uka.ipd.sdq.stoex.AbstractNamedReference;
import de.uka.ipd.sdq.stoex.StoexFactory;
import de.uka.ipd.sdq.stoex.VariableReference;

class IFConfidentialityVariableCharacterisationUtilsTest {

//	private Logger logger = Logger.getLogger(IFConfidentialityVariableCharacterisationUtilsTest.class);

	private final DataDictionaryCharacterizedFactory ddcFac = DataDictionaryCharacterizedFactory.eINSTANCE;
	private final StoexFactory stoexFac = StoexFactory.eINSTANCE;;

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
		assertTrue(CvcTestUtils.evaluateCvcLatticeMapping(confVarChars, enumeration, varToLevel, "One"),
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

		assertTrue(CvcTestUtils.evaluateCvcForBooleanMapping(confVarChars.get(0), variableToLiteralToBoolean),
				"Should forward Low");
		assertFalse(CvcTestUtils.evaluateCvcForBooleanMapping(confVarChars.get(1), variableToLiteralToBoolean),
				"Should not set High, but forward Low");

		literalToBoolean.put("Low", false);
		literalToBoolean.put("High", true);
		variableToLiteralToBoolean.put("a", literalToBoolean);

		assertFalse(CvcTestUtils.evaluateCvcForBooleanMapping(confVarChars.get(0), variableToLiteralToBoolean),
				"Should not set Low, but forward High");
		assertTrue(CvcTestUtils.evaluateCvcForBooleanMapping(confVarChars.get(1), variableToLiteralToBoolean),
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
		assertTrue(CvcTestUtils.evaluateCvcLatticeMapping(confVarChars, enumeration, varToLevel, "Low"),
				"Should forward 'Low' for a=Low, b=Low");

		varToLevel.put("a", "Low");
		varToLevel.put("b", "Mid");
		assertTrue(CvcTestUtils.evaluateCvcLatticeMapping(confVarChars, enumeration, varToLevel, "Mid"),
				"Should forward 'Mid' for a=Low, b=Mid");

		varToLevel.put("a", "Mid");
		varToLevel.put("b", "Low");
		assertTrue(CvcTestUtils.evaluateCvcLatticeMapping(confVarChars, enumeration, varToLevel, "Mid"),
				"Should forward 'Mid' for a=Mid, b=Low");

		varToLevel.put("a", "Mid");
		varToLevel.put("b", "Mid");
		assertTrue(CvcTestUtils.evaluateCvcLatticeMapping(confVarChars, enumeration, varToLevel, "Mid"),
				"Should forward 'Mid' for a=Mid, b=Mid");

		varToLevel.put("a", "Low");
		varToLevel.put("b", "High");
		assertTrue(CvcTestUtils.evaluateCvcLatticeMapping(confVarChars, enumeration, varToLevel, "High"),
				"Should forward 'High' for a=Low, b=High");

		varToLevel.put("a", "Mid");
		varToLevel.put("b", "High");
		assertTrue(CvcTestUtils.evaluateCvcLatticeMapping(confVarChars, enumeration, varToLevel, "High"),
				"Should forward 'High' for a=Mid, b=High");

		varToLevel.put("a", "High");
		varToLevel.put("b", "Low");
		assertTrue(CvcTestUtils.evaluateCvcLatticeMapping(confVarChars, enumeration, varToLevel, "High"),
				"Should forward 'High' for a=High, b=Low");

		varToLevel.put("a", "High");
		varToLevel.put("b", "Mid");
		assertTrue(CvcTestUtils.evaluateCvcLatticeMapping(confVarChars, enumeration, varToLevel, "High"),
				"Should forward 'High' for a=High, b=Mid");

		varToLevel.put("a", "High");
		varToLevel.put("b", "High");
		assertTrue(CvcTestUtils.evaluateCvcLatticeMapping(confVarChars, enumeration, varToLevel, "High"),
				"Should forward 'High' for a=High, b=High");
	}

	/*
	 * Help Methods
	 */

	private EnumCharacteristicType createLattice(String... names) {
		Enumeration lattice = ddcFac.createEnumeration();
		lattice.setName("Lattice");

		for (String name : names) {
			addLiteral(name, lattice);
		}

		EnumCharacteristicType latticeCharacteristicType = ddcFac.createEnumCharacteristicType();
		latticeCharacteristicType.setType(lattice);
		latticeCharacteristicType.setName("Lattice");
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
