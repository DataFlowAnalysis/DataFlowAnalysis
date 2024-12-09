package org.dataflowanalysis.analysis.dsl.variable;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

import java.util.List;
import java.util.Optional;

/**
 * Represents a reference to a constraint variable with the given name and values
 * @param name Given name of the constraint variable reference
 * @param values Given possible values of the constraint variable reference
 */
public record ConstraintVariableReference (String name, Optional<List<String>> values) {
    private static final String DSL_VARIABLE_SIGN = "$";
    private static final Logger logger = Logger.getLogger(ConstraintVariableReference.class);

    /**
     * Creates a constraint variable reference with the given name with no set values
     * @param name Name of the constraint variable
     * @return Returns a new reference to the variable with the given name
     */
    public static ConstraintVariableReference of(String name) {
        return new ConstraintVariableReference(name, Optional.empty());
    }

    /**
     * Creates a constraint variable reference with the given name with set values
     * @param name Name of the constraint variable
     * @param values Values of the constraint variable reference
     * @return Returns a new reference to the variable with the given name and values
     */
    public static ConstraintVariableReference of(String name, List<String> values) {
        return new ConstraintVariableReference(name, Optional.of(values));
    }

    /**
     * Creates a new constraint variable reference to a constant with the given values
     * @param values Values of the constraint
     * @return Returns a new reference to constant with the given values
     */
    public static ConstraintVariableReference ofConstant(List<String> values) {
        return new ConstraintVariableReference(ConstraintVariable.CONSTANT_NAME, Optional.of(values));
    }

    /**
     * Returns whether the constraint variable reference is a constant
     * @return  Returns true, if the constraint variable is a constant.
     *          Otherwise, the method returns false
     */
    public boolean isConstant() {
        return this.name.equals(ConstraintVariable.CONSTANT_NAME);
    }

    @Override
    public String toString() {
        if (this.isConstant() && this.values.isPresent()) {
            return this.values.get().get(0);
        } else {
            return DSL_VARIABLE_SIGN + this.name;
        }
    }

    public static ParseResult<ConstraintVariableReference> fromString(StringView stringView) {
        String string = stringView.getString().split("[ .,()]")[0];
        logger.info("Parsing: " + string);
        stringView.advance(string.length());
        if (string.startsWith(DSL_VARIABLE_SIGN)) {
            return ParseResult.ok(ConstraintVariableReference.of(string.substring(1)));
        } else {
            return ParseResult.ok(ConstraintVariableReference.ofConstant(List.of(string)));
        }
    }
}
