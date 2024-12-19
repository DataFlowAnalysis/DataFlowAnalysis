package org.dataflowanalysis.analysis.dsl.selectors;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.context.DSLContextKey;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

import java.util.List;

public record CharacteristicsSelectorData(ConstraintVariableReference characteristicType, ConstraintVariableReference characteristicValue) {
    private static final String DSL_SEPARATOR = ".";
    private static final Logger logger = Logger.getLogger(CharacteristicsSelectorData.class);

    /**
     * Determines whether a characteristic matches the saved reference
     * @param context DSL Matching context
     * @param characteristic Provided characteristic that should be matched
     * @return Returns true of the provided characteristic matches the saved reference.
     * Otherwise, the method returns false.
     */
    public boolean matchesCharacteristic(DSLContext context, AbstractVertex<?> vertex, CharacteristicValue characteristic, String variableName, List<String> characteristicTypes, List<String> characteristicValues) {
        var characteristicTypeVariable = context.getMapping(DSLContextKey.of(variableName, vertex), this.characteristicType());
        var characteristicValueVariable = context.getMapping(DSLContextKey.of(variableName, vertex), this.characteristicValue());

        if (characteristicTypeVariable.hasValues() && !characteristicTypeVariable.getPossibleValues().get().contains(characteristic.getTypeName())) {
            return false;
        }
        if (characteristicValueVariable.hasValues() && !characteristicValueVariable.getPossibleValues().get().contains(characteristic.getValueName())) {
            return false;
        }

        if (!characteristicTypeVariable.isConstant() && !characteristicTypes.contains(characteristic.getTypeName())) {
            characteristicTypes.add(characteristic.getTypeName());
        }
        if(!characteristicValueVariable.isConstant() && !characteristicValues.contains(characteristic.getValueName())) {
            characteristicValues.add(characteristic.getValueName());
        }
        return true;
    }

    public void applyResults(DSLContext context, AbstractVertex<?> vertex, String variableName,  List<String> characteristicTypes, List<String> characteristicValues) {
        var characteristicTypeVariable = context.getMapping(DSLContextKey.of(variableName, vertex), this.characteristicType());
        var characteristicValueVariable = context.getMapping(DSLContextKey.of(variableName, vertex), this.characteristicValue());

        if (!characteristicTypeVariable.isConstant()) {
            characteristicTypeVariable.addPossibleValues(characteristicTypes);
        }

        if (!characteristicValueVariable.isConstant()) {
            characteristicValueVariable.addPossibleValues(characteristicValues);
        }
    }

    @Override
    public String toString() {
        return this.characteristicType.toString() + DSL_SEPARATOR + this.characteristicValue.toString();
    }

    public static ParseResult<CharacteristicsSelectorData> fromString(StringView string) {
        logger.info("Parsing: " + string.getString());
        ParseResult<ConstraintVariableReference> characteristicType = ConstraintVariableReference.fromString(string);
        if (characteristicType.failed()) {
            return ParseResult.error(characteristicType.getError());
        }
        if (!string.startsWith(DSL_SEPARATOR)) {
            string.retreat(characteristicType.getResult().toString().length());
            return string.expect(DSL_SEPARATOR);
        }
        string.advance(DSL_SEPARATOR.length());
        ParseResult<ConstraintVariableReference> characteristicValue = ConstraintVariableReference.fromString(string);
        if (characteristicValue.failed()) {
            string.retreat(DSL_SEPARATOR.length() + characteristicType.getResult().toString().length());
            return ParseResult.error(characteristicValue.getError());
        }
        return ParseResult.ok(new CharacteristicsSelectorData(characteristicType.getResult(), characteristicValue.getResult()));
    }
}
