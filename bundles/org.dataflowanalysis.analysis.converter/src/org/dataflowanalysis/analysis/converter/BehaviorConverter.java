package org.dataflowanalysis.analysis.converter;

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

import org.dataflowanalysis.dfd.datadictionary.AND;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelReference;
import org.dataflowanalysis.dfd.datadictionary.NOT;
import org.dataflowanalysis.dfd.datadictionary.OR;
import org.dataflowanalysis.dfd.datadictionary.TRUE;
import org.dataflowanalysis.dfd.datadictionary.Term;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;

public class BehaviorConverter {
    private final datadictionaryFactory ddFactory;
    private DataDictionary dataDictionary;
    
    public BehaviorConverter() {
        ddFactory = datadictionaryFactory.eINSTANCE;
        dataDictionary = null;
    }
    
    public BehaviorConverter(DataDictionary dataDictionary) {
        this();
        this.dataDictionary = dataDictionary;
    }
    
    public Term stringToTerm(String expression) {
        // Tokenize the expression
        List<String> tokens = tokenize(expression);
        
        // Stack for operands
        Stack<Term> operands = new Stack<>();
        // Stack for operators
        Stack<String> operators = new Stack<>();

        for (String token : tokens) {
            if (token.equals("(")) {
                operators.push(token);
            } else if (token.equals(")")) {
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
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

    private boolean isOperator(String token) {
        return token.equals("&&") || token.equals("||") || token.equals("!");
    }

    private int precedence(String operator) {
        switch (operator) {
            case "||":
                return 1;
            case "&&":
                return 2;
            case "!":
                return 3;
            default:
                return -1;
        }
    }

    private void performOperation(Stack<Term> operands, String operator) {
        switch (operator) {
            case "&&":
            case "||":
                var right = operands.pop();
                var left = operands.pop();
                var operation = (operator.equals("&&")) ? ddFactory.createAND() : ddFactory.createOR();
                operation.getTerms().add(left);
                operation.getTerms().add(right);
                operands.push(operation);
                break;
            case "!":
                var negated = operands.pop();
                var notOperation = ddFactory.createNOT();
                notOperation.setNegatedTerm(negated);
                operands.push(notOperation);
                break;
        }
    }

    private Term createTerm(String token) {
        if (token.equals("TRUE")) {
            return ddFactory.createTRUE();
        } else if (token.equals("FALSE")) {
            var ddFalse = ddFactory.createNOT();
            ddFalse.setNegatedTerm(ddFactory.createTRUE());
            return ddFalse;
        } else {
            String typeName = token.split("\\.")[0];
            String valueName = token.split("\\.")[1];

            Label value = null;
            
            if(dataDictionary!=null) {
                value=dataDictionary.getLabelTypes().stream()
                        .filter(labelType -> labelType.getEntityName().equals(typeName))
                        .flatMap(labelType -> labelType.getLabel().stream())
                        .filter(label -> label.getEntityName().equals(valueName))
                        .findAny().orElse(null);
            }
 
            if(value==null) {
                value=ddFactory.createLabel();
                value.setEntityName(token);
            }
            
            var labelReference = ddFactory.createLabelReference();
            labelReference.setLabel(value);
            return labelReference;
        }
    }
    
    public String termToString(Term term) {
        return termToString(term, false);
    }

    private String termToString(Term term, boolean isNested) {
        if (term instanceof LabelReference) {
            return ((LabelReference) term).getLabel().getEntityName();
        } else if (term instanceof TRUE) {
            return "TRUE";
        } else if (term instanceof AND) {
            List<Term> operands = ((AND)term).getTerms();
            String result = termToString(operands.get(0), true) + " && " + termToString(operands.get(1), true);
            return isNested ? "(" + result + ")" : result;
        } else if (term instanceof OR) {
            List<Term> operands = ((OR)term).getTerms();
            String result = termToString(operands.get(0), true) + " || " + termToString(operands.get(1), true);
            return isNested ? "(" + result + ")" : result;
        } else if (term instanceof NOT) {
            return "!" + termToString(((NOT)term).getNegatedTerm(), false);
        } else {
            throw new IllegalArgumentException("Unknown term type");
        }
    }
    
    private List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder token = new StringBuilder();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isWhitespace(c)) {
                // Skip whitespace
                continue;
            }

            if (c == '(' || c == ')') {
                // Directly add parentheses as separate tokens
                if (token.length() > 0) {
                    tokens.add(token.toString());
                    token.setLength(0); // Reset the token builder
                }
                tokens.add(Character.toString(c));
            } else if (c == '&' || c == '|' || c == '!') {
                // Handle logical operators
                if (token.length() > 0) {
                    tokens.add(token.toString());
                    token.setLength(0);
                }
                token.append(c);

                // For && and ||, make sure to capture both characters
                if ((c == '&' || c == '|') && i + 1 < expression.length() && expression.charAt(i + 1) == c) {
                    i++; // Skip the next character since it's part of the operator
                    token.append(c);
                }

                tokens.add(token.toString());
                token.setLength(0);
            } else {
                // Build operand tokens
                token.append(c);
            }
        }

        if (token.length() > 0) {
            tokens.add(token.toString());
        }

        return tokens;
    }
}
