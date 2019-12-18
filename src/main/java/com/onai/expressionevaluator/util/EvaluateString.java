package com.onai.expressionevaluator.util;

import com.onai.expressionevaluator.service.ExpressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Component
public class EvaluateString {

	static ExpressionService expressionService;

	@Autowired
	public EvaluateString(ExpressionService expressionService){
		EvaluateString.expressionService = expressionService;
	}

	public static double evaluate(String expression) throws Exception
	{
		List<String> defaultFunctionList = new ArrayList<>();
		defaultFunctionList.add("sum");

		char[] tokens = expression.toCharArray();

		// Stack for numbers: 'values'
		Stack<String> values = new Stack<>();

		// Stack for Operators: 'ops'
		Stack<Character> ops = new Stack<>();

		Stack<String> functionStack = new Stack<>();

		// Stack for numbers: 'values'
		Stack<String> expressionStack = new Stack<>();

		for (int i = 0; i < tokens.length; i++)
		{
			// Current token is a whitespace, skip it
			if (tokens[i] == ' ')
				ops.push(tokens[i]);

			// Current token is a number, push it to stack for numbers
			if ((tokens[i] >= 'a' && tokens[i] <= 'z') ||  (tokens[i] >= 'A' && tokens[i] <= 'Z'))
			{
				StringBuilder dataBuilder = new StringBuilder();
				// There may be more than one alphabet
				while (i < tokens.length && ((tokens[i] >= 'a' && tokens[i] <= 'z') || (tokens[i] >= 'A' && tokens[i] <= 'Z')))
					dataBuilder.append(tokens[i++]);

				if(defaultFunctionList.contains(dataBuilder.toString())){

//					functionStack.push(sbuf.toString());

					StringBuilder sb = new StringBuilder();
					while (i < tokens.length )
						sb.append(tokens[i++]);

					values.push(expressionService.evaluateFunctionExpression(dataBuilder.toString(), "LoanRateHistoryList", sb.toString()));
				}else {
					values.push(dataBuilder.toString());
				}
			}

			// Current token is an opening brace, push it to 'ops'
			else if (tokens[i] == '(')
				ops.push(tokens[i]);

				// Closing brace encountered, solve entire brace
			else if (tokens[i] == ')')
			{
				while (ops.peek() == '(')
					if(functionStack.peek() != null) {
						values.push(expressionService.iterateDataAndCalculateSum(functionStack.pop(), values.pop())); // modify logic to pick all the values till end the function
						ops.pop();
					}else{
//						values.push(applyOp(ops.pop(), Integer.valueOf(values.pop()), Integer.valueOf(values.pop())));
						ops.pop();
					}
			}

			// Current token is an operator.
			else if (tokens[i] == '+' || tokens[i] == '-' ||
					tokens[i] == '*' || tokens[i] == '/')
			{

				// Push current token to 'ops'.
				ops.push(tokens[i]);
			}
		}

		// Entire expression has been parsed at this point, apply remaining
		// ops to remaining values
		while (!ops.empty())
			values.push(String.valueOf(expressionService.customExpressionEvaluation(ops.pop(), values.pop(), values.pop())));

		// Top of 'values' contains result, return it
		return Double.valueOf(values.isEmpty() ? "0" : (values.pop()) );
	}



	private void evaluateSubExpression(String expression, List<String> defaultFunctionList, Stack functionStack, Stack expressionStack){
		StringBuilder dataBuilder = new StringBuilder();
		StringBuilder functionBuilder = new StringBuilder();
		StringBuilder subExpressionBuilder = new StringBuilder();

		char[] tokens = expression.toCharArray();
		for (int i = 0; i < tokens.length; i++){

			if (tokens[i] == ' ')
				dataBuilder.append(tokens[i]);

			if ((tokens[i] >= 'a' && tokens[i] <= 'z') ||  (tokens[i] >= 'A' && tokens[i] <= 'Z')){

				while (i < tokens.length && ((tokens[i] >= 'a' && tokens[i] <= 'z') || (tokens[i] >= 'A' && tokens[i] <= 'Z')))
					functionBuilder.append(tokens[i++]);

				if(defaultFunctionList.contains(functionBuilder.toString())){
					functionStack.push(functionBuilder.toString());
					expressionStack.push(dataBuilder);
					while (i < tokens.length )
						subExpressionBuilder.append(tokens[i++]);

					evaluateSubExpression(subExpressionBuilder.toString(), defaultFunctionList, functionStack, expressionStack);
				}else{
					dataBuilder.append(functionBuilder.toString());
					functionBuilder = new StringBuilder();
				}
			}else{
					dataBuilder.append(tokens[i]);
			}
		}
	}
}
