package org.dataflowanalysis.analysis.converter;

import java.util.List;
import java.util.Stack;

import org.dataflowanalysis.dfd.datadictionary.AND;
import org.dataflowanalysis.dfd.datadictionary.LabelReference;
import org.dataflowanalysis.dfd.datadictionary.NOT;
import org.dataflowanalysis.dfd.datadictionary.OR;
import org.dataflowanalysis.dfd.datadictionary.TRUE;
import org.dataflowanalysis.dfd.datadictionary.Term;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;

public class BehaviorConverter {
    private final datadictionaryFactory ddFactory;
    
    public BehaviorConverter() {
        ddFactory = datadictionaryFactory.eINSTANCE; 
    }
    
    public Term stringToTerm(String expression) {
        // Tokenize the expression
        String[] tokens = expression.split(" ");
        
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
            var labelReference = ddFactory.createLabelReference();
            var label = ddFactory.createLabel();
            label.setEntityName(token);
            labelReference.setLabel(label);
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
            String result = termToString(operands.get(0), false) + " && " + termToString(operands.get(1), false);
            // Add parentheses only if this AND is nested inside another operation
            return isNested ? "(" + result + ")" : result;
        } else if (term instanceof OR) {
            List<Term> operands = ((OR)term).getTerms();
            String result = termToString(operands.get(0), false) + " || " + termToString(operands.get(1), false);
            // Add parentheses only if this OR is nested inside another operation
            return isNested ? "(" + result + ")" : result;
        } else if (term instanceof NOT) {
            Term negatedTerm = ((NOT)term).getNegatedTerm();
            // For NOT, parentheses are added around the negated term only if it's a complex term itself
            String negatedString = termToString(negatedTerm, false);
            if (negatedTerm instanceof AND || negatedTerm instanceof OR) {
                negatedString = "(" + negatedString + ")";
            }
            return "!" + negatedString;
        } else {
            throw new IllegalArgumentException("Unknown term type");
        }
    }
}
