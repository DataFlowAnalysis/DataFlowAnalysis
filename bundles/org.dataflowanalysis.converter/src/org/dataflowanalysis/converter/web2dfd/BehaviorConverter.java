package org.dataflowanalysis.converter.web2dfd;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import org.apache.log4j.Logger;
import org.dataflowanalysis.dfd.datadictionary.AND;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelReference;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.NOT;
import org.dataflowanalysis.dfd.datadictionary.OR;
import org.dataflowanalysis.dfd.datadictionary.TRUE;
import org.dataflowanalysis.dfd.datadictionary.Term;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;

/**
 * Converts string expressions to {@link Term} instances and vice versa, based on a given {@link DataDictionary}.
 * Supports logical operations AND, OR, and NOT.
 */
public class BehaviorConverter {
    private final datadictionaryFactory ddFactory = datadictionaryFactory.eINSTANCE;
    private final DataDictionary dataDictionary;

    private final Logger logger = Logger.getLogger(BehaviorConverter.class);

    private final String LOGICAL_AND = "&&";
    private final String LOGICAL_OR = "||";
    private final String LOGICAL_NOT = "!";

    public BehaviorConverter(DataDictionary dataDictionary) {
        this.dataDictionary = dataDictionary;
    }

    /**
     * Converts a string expression into a {@link Term} instance. The expression can include logical operators (&&, ||, !)
     * and operands represented by strings.
     * @param expression the logical expression to convert
     * @return the {@link Term} representation of the expression
     */
    public Term stringToTerm(String expression) {
        List<String> tokens = tokenize(expression);

        Stack<Term> operands = new Stack<>();

        Stack<String> operators = new Stack<>();

        for (String token : tokens) {
            if (token.equals("(")) {
                operators.push(token);
            } else if (token.equals(")")) {
                while (!operators.isEmpty() && !operators.peek()
                        .equals("(")) {
                    performOperation(operands, operators.pop());
                }
                operators.pop(); // Remove the '(' from the stack
            } else if (isOperator(token)) {
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(token)) {
                    performOperation(operands, operators.pop());
                }
                operators.push(token);
            } else {
                operands.push(createTerm(token));
            }
        }

        while (!operators.isEmpty()) {
            performOperation(operands, operators.pop());
        }

        return operands.pop();
    }

    /**
     * Converts a {@link Term} instance back into its string representation.
     * @param term the {@link Term} instance to convert
     * @return the string representation of the term
     */
    public String termToString(Term term) {
        return termToString(term, false);
    }

    private boolean isOperator(String token) {
        return token.equals(LOGICAL_AND) || token.equals(LOGICAL_OR) || token.equals(LOGICAL_NOT);
    }

    private int precedence(String operator) {
        return switch (operator) {
            case LOGICAL_OR -> 1;
            case LOGICAL_AND -> 2;
            case LOGICAL_NOT -> 3;
            default -> -1;
        };
    }

    private void performOperation(Stack<Term> operands, String operator) {
        switch (operator) {
            case LOGICAL_AND:
            case LOGICAL_OR:
                var right = operands.pop();
                var left = operands.pop();
                var operation = (operator.equals(LOGICAL_AND)) ? ddFactory.createAND() : ddFactory.createOR();
                operation.getTerms()
                        .add(left);
                operation.getTerms()
                        .add(right);
                operands.push(operation);
                break;
            case LOGICAL_NOT:
                var negated = operands.pop();
                var notOperation = ddFactory.createNOT();
                notOperation.setNegatedTerm(negated);
                operands.push(notOperation);
                break;
            default:
                logger.error("Unknow operator");
                throw new IllegalArgumentException(operator);
        }
    }

    private Term createTerm(String token) {
        if (token.equals("TRUE")) {
            return ddFactory.createTRUE();
        }

        if (token.equals("FALSE")) {
            var ddFalse = ddFactory.createNOT();
            ddFalse.setNegatedTerm(ddFactory.createTRUE());
            return ddFalse;
        }

        String typeName = token.split("\\.")[0];
        String valueName = token.split("\\.")[1];

        Optional<Label> optionalValue = Optional.ofNullable(dataDictionary)
                .flatMap(dd -> dd.getLabelTypes()
                        .stream()
                        .filter(labelType -> labelType.getEntityName()
                                .equals(typeName))
                        .flatMap(labelType -> labelType.getLabel()
                                .stream())
                        .filter(label -> label.getEntityName()
                                .equals(valueName))
                        .findAny());

        Label value = optionalValue.orElseGet(() -> {
            Label label = ddFactory.createLabel();
            label.setEntityName(token);
            return label;
        });

        var labelReference = ddFactory.createLabelReference();
        labelReference.setLabel(value);
        return labelReference;

    }

    private String termToString(Term term, boolean isNested) {
        if (term instanceof LabelReference labelReference) {
            Label label = labelReference.getLabel();
            return ((LabelType) label.eContainer()).getEntityName() + "." + label.getEntityName();
        } else if (term instanceof TRUE) {
            return "TRUE";
        } else if (term instanceof AND and) {
            List<Term> operands = and.getTerms();
            String result = termToString(operands.get(0), true) + " " + LOGICAL_AND + " " + termToString(operands.get(1), true);
            return isNested ? "(" + result + ")" : result;
        } else if (term instanceof OR or) {
            List<Term> operands = or.getTerms();
            String result = termToString(operands.get(0), true) + " " + LOGICAL_OR + " " + termToString(operands.get(1), true);
            return isNested ? "(" + result + ")" : result;
        } else if (term instanceof NOT not) {
            if (not.getNegatedTerm() instanceof TRUE) {
                return "FALSE";
            }
            if (not.getNegatedTerm() instanceof LabelReference) {
                return LOGICAL_NOT + termToString(not.getNegatedTerm(), false);
            }
            return LOGICAL_NOT + termToString(not.getNegatedTerm(), true);
        } else {
            throw new IllegalArgumentException("Unknown term type");
        }
    }

    private List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder token = new StringBuilder();

        for (int i = 0; i < expression.length(); i++) {
            char current = expression.charAt(i);

            if (Character.isWhitespace(current)) {
                // Skip whitespace
                continue;
            }

            if (current == '(' || current == ')') {
                // Directly add parentheses as separate tokens
                if (!token.isEmpty()) {
                    tokens.add(token.toString());
                    token.setLength(0); // Reset the token builder
                }
                tokens.add(Character.toString(current));
            } else if (current == '&' || current == '|' || current == '!') {
                // Handle logical operators
                if (!token.isEmpty()) {
                    tokens.add(token.toString());
                    token.setLength(0);
                }
                token.append(current);

                // For && and ||, make sure to capture both characters
                if ((current == '&' || current == '|') && i + 1 < expression.length() && expression.charAt(i + 1) == current) {
                    i++; // Skip the next character since it's part of the operator
                    token.append(current);
                }

                tokens.add(token.toString());
                token.setLength(0);
            } else {
                // Build operand tokens
                token.append(current);
            }
        }

        if (!token.isEmpty()) {
            tokens.add(token.toString());
        }

        return tokens;
    }
}