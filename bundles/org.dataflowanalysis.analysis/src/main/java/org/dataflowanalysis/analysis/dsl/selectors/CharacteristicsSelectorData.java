package org.dataflowanalysis.analysis.dsl.selectors;

import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.dsl.AbstractParseable;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.context.DSLContextKey;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

/**
 * Represents a selected characteristic with a given characteristic type and characteristic value. Each may be a
 * {@link ConstraintVariableReference} that is not a constant
 */
public final class CharacteristicsSelectorData extends AbstractParseable {
    private static final Logger logger = LoggerManager.getLogger(CharacteristicsSelectorData.class);
    private final ConstraintVariableReference characteristicType;
    private final ConstraintVariableReference characteristicValue;

    /**
     * Creates a new characteristics data object with the given {@link ConstraintVariableReference} as characteristic type
     * and value
     * @param characteristicType {@link ConstraintVariableReference} used as characteristic type
     * @param characteristicValue {@link ConstraintVariableReference} used as characteristic value
     */
    public CharacteristicsSelectorData(ConstraintVariableReference characteristicType, ConstraintVariableReference characteristicValue) {
        this.characteristicType = characteristicType;
        this.characteristicValue = characteristicValue;
    }

    /**
     * Determines whether a characteristic matches the saved reference
     * @param context DSL Matching context
     * @param characteristic Provided characteristic that should be matched
     * @return Returns true of the provided characteristic matches the saved reference. Otherwise, the method returns false.
     */
    public boolean matchesCharacteristic(DSLContext context, AbstractVertex<?> vertex, CharacteristicValue characteristic, String variableName,
            List<String> characteristicTypes, List<String> characteristicValues) {
        var characteristicTypeVariable = context.getMapping(DSLContextKey.of(variableName, vertex), this.characteristicType());
        var characteristicValueVariable = context.getMapping(DSLContextKey.of(variableName, vertex), this.characteristicValue());

        if (characteristicTypeVariable.hasValues() && !characteristicTypeVariable.getPossibleValues()
                .get()
                .contains(characteristic.getTypeName())) {
            return false;
        }
        if (characteristicValueVariable.hasValues() && !characteristicValueVariable.getPossibleValues()
                .get()
                .contains(characteristic.getValueName())) {
            return false;
        }

        if (!characteristicTypeVariable.isConstant() && !characteristicTypes.contains(characteristic.getTypeName())) {
            characteristicTypes.add(characteristic.getTypeName());
        }
        if (!characteristicValueVariable.isConstant() && !characteristicValues.contains(characteristic.getValueName())) {
            characteristicValues.add(characteristic.getValueName());
        }
        return true;
    }

    public void applyResults(DSLContext context, AbstractVertex<?> vertex, String variableName, List<String> characteristicTypes,
            List<String> characteristicValues) {
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

    /**
     * Parses a {@link CharacteristicsSelectorData} object from the given view on a string.
     * <p/>
     * This method expects the following format: {@code <Type>.<Value>}
     * @param string String view on the string that is parsed
     * @return {@link ParseResult} containing the {@link CharacteristicsSelectorData} object
     */
    public static ParseResult<CharacteristicsSelectorData> fromString(StringView string) {
        string.skipWhitespace();
        if (string.invalid() || string.empty()) {
            return ParseResult.error("Cannot parse characteristic selector data from empty or invalid string!");
        }
        logger.debug("Parsing: " + string.getString());
        int position = string.getPosition();
        ParseResult<ConstraintVariableReference> characteristicType = ConstraintVariableReference.fromString(string);
        if (characteristicType.failed()) {
            string.setPosition(position);
            return ParseResult.error(characteristicType.getError());
        }
        if (string.invalid() || string.empty()) {
            string.setPosition(position);
            return ParseResult.error("Missing characteristic value from characteristics selector data!");
        }
        if (!string.startsWith(DSL_SEPARATOR)) {
            string.setPosition(position);
            return string.expect(DSL_SEPARATOR);
        }
        string.advance(DSL_SEPARATOR.length());
        ParseResult<ConstraintVariableReference> characteristicValue = ConstraintVariableReference.fromString(string);
        if (characteristicValue.failed()) {
            string.setPosition(position);
            return ParseResult.error(characteristicValue.getError());
        }
        return ParseResult.ok(new CharacteristicsSelectorData(characteristicType.getResult(), characteristicValue.getResult()));
    }

    /**
     * Returns the characteristic type of the characteristics selector data object
     * @return {@link ConstraintVariableReference} to the characteristic type
     */
    public ConstraintVariableReference characteristicType() {
        return characteristicType;
    }

    /**
     * type Returns the characteristic value of the characteristics selector data object
     * @return {@link ConstraintVariableReference} to the characteristic value
     */
    public ConstraintVariableReference characteristicValue() {
        return characteristicValue;
    }
}
