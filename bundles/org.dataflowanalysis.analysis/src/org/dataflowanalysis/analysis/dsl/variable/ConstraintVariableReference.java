package org.dataflowanalysis.analysis.dsl.variable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.dsl.AbstractParseable;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

/**
 * Represents a reference to a constraint variable with the given name and values
 */
public final class ConstraintVariableReference extends AbstractParseable {
    private static final String DSL_VARIABLE_SIGN = "$";
    private static final Logger logger = LoggerManager.getLogger(ConstraintVariableReference.class);
    private final String name;
    private final Optional<List<String>> values;

    /**
     * @param name Given name of the constraint variable reference
     * @param values Given possible values of the constraint variable reference
     */
    public ConstraintVariableReference(String name, Optional<List<String>> values) {
        this.name = name;
        this.values = values;
    }

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
     * @return Returns true, if the constraint variable is a constant. Otherwise, the method returns false
     */
    public boolean isConstant() {
        return this.name.equals(ConstraintVariable.CONSTANT_NAME);
    }

    @Override
    public String toString() {
        if (this.isConstant() && this.values.isPresent()) {
            return this.values.get()
                    .get(0);
        } else {
            return DSL_VARIABLE_SIGN + this.name;
        }
    }

    public static ParseResult<ConstraintVariableReference> fromString(StringView stringView) {
        if (stringView.invalid() || stringView.empty()) {
            return ParseResult.error("Cannot create variable: Expected any string!");
        }
        String[] split = stringView.getString()
                .split("[ .,()]");
        if (split.length == 0) {
            return ParseResult.error("Invalid variable: Expected any string!");
        }
        String string = split[0];
        logger.info("Parsing: " + string);
        stringView.advance(string.length());
        if (string.startsWith(DSL_VARIABLE_SIGN)) {
            if (string.substring(1)
                    .isEmpty()) {
                return ParseResult.error("Empty variable name!");
            }
            return ParseResult.ok(ConstraintVariableReference.of(string.substring(1)));
        } else {
            if (string.isEmpty()) {
                return ParseResult.error("Constant must be not be empty!");
            }
            if (string.contains(DSL_INVERTED_SYMBOL)) {
                return ParseResult.error("Constants must not contain \"" + DSL_INVERTED_SYMBOL + "\" in their name!");
            }
            return ParseResult.ok(ConstraintVariableReference.ofConstant(List.of(string)));
        }
    }

    public String name() {
        return name;
    }

    public Optional<List<String>> values() {
        return values;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        var that = (ConstraintVariableReference) obj;
        return Objects.equals(this.name, that.name) && Objects.equals(this.values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, values);
    }

}
