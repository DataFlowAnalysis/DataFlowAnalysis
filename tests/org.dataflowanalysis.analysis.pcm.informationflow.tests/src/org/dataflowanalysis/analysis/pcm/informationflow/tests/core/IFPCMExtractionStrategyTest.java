package org.dataflowanalysis.analysis.pcm.informationflow.tests.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.pcm.informationflow.core.IFPCMExtractionStrategy;
import org.dataflowanalysis.analysis.pcm.informationflow.core.IFPCMExtractionStrategyPrefer;
import org.dataflowanalysis.analysis.pcm.informationflow.tests.ModelCreationTestUtils;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.DataDictionaryCharacterizedFactory;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Enumeration;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.ExpressionsFactory;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.False;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityFactory;
import org.dataflowanalysis.pcm.extension.model.confidentiality.expression.ExpressionFactory;
import org.dataflowanalysis.pcm.extension.model.confidentiality.expression.LhsEnumCharacteristicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.parameter.ParameterFactory;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.parameter.VariableCharacterisationType;

import de.uka.ipd.sdq.stoex.StoexFactory;

class IFPCMExtractionStrategyTest {

	private IFPCMExtractionStrategy extractionStrategy;

	private final DataDictionaryCharacterizedFactory ddcFac = DataDictionaryCharacterizedFactory.eINSTANCE;
	private final StoexFactory stoexFac = StoexFactory.eINSTANCE;
	private final ExpressionsFactory expsFac = ExpressionsFactory.eINSTANCE;
	private final ConfidentialityFactory confFac = ConfidentialityFactory.eINSTANCE;
	private final ExpressionFactory expFac = ExpressionFactory.eINSTANCE;
	private final CoreFactory pcmCoreFac = CoreFactory.eINSTANCE;
	private final ParameterFactory pcmParameterFac = ParameterFactory.eINSTANCE;

	@BeforeEach
	void prepareStrategy() {
		var analysis = ModelCreationTestUtils.createSwappedCallsAnalysis();
		extractionStrategy = new IFPCMExtractionStrategyPrefer(analysis.getResourceProvider());
	}

	@Test
	void testCalculateEffectiveCvcWithoutAnyVcs() {
		var result = extractionStrategy.calculateEffectiveConfidentialityVariableCharacterisation(List.of());
		assertEquals(0, result.size(), "There should be no generation of Cvcs from nothing.");
	}

	@Test
	void testCalculateEffectiveCvcWithoutNormalVcs() {
		var term = expsFac.createFalse();
		Enumeration enumeration = ddcFac.createEnumeration();
		enumeration.setName("OtherEnumeration");
		Literal literal = ddcFac.createLiteral();
		literal.setEnum(enumeration);
		literal.setName("SomeLabel");
		EnumCharacteristicType latticeCharacteristicType = ddcFac.createEnumCharacteristicType();
		latticeCharacteristicType.setType(enumeration);
		latticeCharacteristicType.setName("OtherEnumertaionCT");
		var lhs = expFac.createLhsEnumCharacteristicReference();
		lhs.setLiteral(literal);
		var variableRef = stoexFac.createVariableReference();
		variableRef.setReferenceName("x");
		var variableUsage = pcmParameterFac.createVariableUsage();
		variableUsage.setNamedReference__VariableUsage(variableRef);
		var cvc = confFac.createConfidentialityVariableCharacterisation();
		cvc.setLhs(lhs);
		cvc.setRhs(term);
		cvc.setVariableUsage_VariableCharacterisation(variableUsage);

		List<VariableCharacterisation> cvcs = List.of(cvc);

		var result = extractionStrategy.calculateEffectiveConfidentialityVariableCharacterisation(cvcs);

		assertEquals(1, result.size(), "There should be the same number of CVCs when no normal VCs exist.");
		assertInstanceOf(False.class, result.get(0).getRhs(),
				"The call should result in the same CVC when no normal VCs exist.");
	}

	@Test
	void testCalculateEffectiveCvcWithoutCvcs() {
		// Create VC with "x.VALUE = a.VALUE * b.VALUE + c.VALUE"
		var variableRef = stoexFac.createVariableReference();
		variableRef.setReferenceName("x");
		var variableUsage = pcmParameterFac.createVariableUsage();
		variableUsage.setNamedReference__VariableUsage(variableRef);
		var randomVar = pcmCoreFac.createPCMRandomVariable();
		randomVar.setSpecification("a.VALUE * b.VALUE + c.VALUE");
		var varChar = pcmParameterFac.createVariableCharacterisation();
		varChar.setSpecification_VariableCharacterisation(randomVar);
		varChar.setType(VariableCharacterisationType.VALUE);
		varChar.setVariableUsage_VariableCharacterisation(variableUsage);
		var varChars = List.of(varChar);

		var result = extractionStrategy.calculateEffectiveConfidentialityVariableCharacterisation(varChars);

		assertEquals(2, result.size(), "Cvcs should be created for each level in the lattice.");

		var lhs = (LhsEnumCharacteristicReference) result.get(0).getLhs();
		Map<String, String> varToLevel = new HashMap<>();

		varToLevel.put("a", "Low");
		varToLevel.put("b", "Low");
		varToLevel.put("c", "Low");
		assertTrue(CvcTestUtils.evaluateCvcLatticeMapping(result, lhs.getLiteral().getEnum(), varToLevel, "Low"),
				"Should forward 'Low' for a=Low, b=Low, c=Low");

		varToLevel.put("a", "High");
		varToLevel.put("b", "Low");
		varToLevel.put("c", "Low");
		assertTrue(CvcTestUtils.evaluateCvcLatticeMapping(result, lhs.getLiteral().getEnum(), varToLevel, "High"),
				"Should forward 'Low' for a=High, b=Low, c=Low");

		varToLevel.put("a", "Low");
		varToLevel.put("b", "High");
		varToLevel.put("c", "Low");
		assertTrue(CvcTestUtils.evaluateCvcLatticeMapping(result, lhs.getLiteral().getEnum(), varToLevel, "High"),
				"Should forward 'Low' for a=Low, b=High, c=Low");

		varToLevel.put("a", "Low");
		varToLevel.put("b", "Low");
		varToLevel.put("c", "High");
		assertTrue(CvcTestUtils.evaluateCvcLatticeMapping(result, lhs.getLiteral().getEnum(), varToLevel, "High"),
				"Should forward 'Low' for a=Low, b=Low, c=High");
	}

}