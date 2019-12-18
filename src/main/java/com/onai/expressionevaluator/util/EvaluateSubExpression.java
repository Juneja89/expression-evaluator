package com.onai.expressionevaluator.util;

import com.onai.expressionevaluator.service.ExpressionEvalutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Stack;

@Component
public class EvaluateSubExpression {

    @Autowired
    ExpressionEvalutionService expressionEvalutionService;

    public BigDecimal evaluateSubexpression(String expression, List<String> customFunctionAcceptListOfObjects) throws Exception{

        // Stack for values: 'expressionEvaluationValues'
        Stack<BigDecimal> valueStack = new Stack<>();

        // Stack for functions: 'sum'
        Stack<String> functionStack = new Stack<>();

        // Stack for expression: '(a+b)'
        Stack<String> expressionStack = new Stack<>();

        evaluateString(expression, customFunctionAcceptListOfObjects, expressionStack, functionStack);

        // Entire expression has been parsed at this point, apply remaining
        while (!expressionStack.empty()) {
            if (functionStack.isEmpty() && valueStack.isEmpty()) {
                valueStack.push(expressionEvalutionService.subExpressionEvaluator(expressionStack.pop(), ""));
            } else if (!functionStack.isEmpty()) {
                valueStack.push(expressionEvalutionService.subExpressionEvaluator(expressionStack.pop(), functionStack.pop()));
            } else if (!valueStack.isEmpty()) {
                valueStack.push(expressionEvalutionService.subExpressionEvaluator(expressionStack.pop() + String.valueOf(valueStack.pop()), ""));
            }
        }
        // Top of 'values' contains result, return it
        return valueStack.pop();
    }

    private void evaluateString(String expression, List<String> defaultFunctionsList, Stack expressionStack, Stack functionStack ){
        char[] tokens = expression.toCharArray();

        StringBuilder expressionBuilder = new StringBuilder();

        for (int i = 0; i < tokens.length; i++) {
            // Current token is a whitespace, skip it
            if (tokens[i] == ' ')
                expressionBuilder.append(tokens[i]);

                // Current token is a character, push it to stack for characters
            else if ((tokens[i] >= 'a' && tokens[i] <= 'z') || (tokens[i] >= 'A' && tokens[i] <= 'Z')) {
                StringBuilder dataBuilder = new StringBuilder();
                // There may be more than one alphabet
                while (i < tokens.length && ((tokens[i] >= 'a' && tokens[i] <= 'z') || (tokens[i] >= 'A' && tokens[i] <= 'Z')))
                    dataBuilder.append(tokens[i++]);

                if (defaultFunctionsList.contains(dataBuilder.toString())) {
                    StringBuilder sb = new StringBuilder();
                    functionStack.push(dataBuilder.toString());
                    while (i < tokens.length)
                        sb.append(tokens[i++]);

                    if(!expressionBuilder.toString().isEmpty())
                        expressionStack.push(expressionBuilder.toString());

                    expressionBuilder = new StringBuilder();
                    evaluateString(sb.toString(), defaultFunctionsList, expressionStack, functionStack);
                } else {
                    expressionBuilder.append(dataBuilder);
                    expressionBuilder.append(" ");
                }

            } else {
                expressionBuilder.append(tokens[i]);
            }
        }

        if(!expressionBuilder.toString().isEmpty())
            expressionStack.push(expressionBuilder.toString());
    }
}
