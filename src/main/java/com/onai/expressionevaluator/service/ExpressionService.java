package com.onai.expressionevaluator.service;

import com.onai.expressionevaluator.config.CustomContext;
import com.onai.expressionevaluator.model.Prepayment;
import com.onai.expressionevaluator.util.EvaluateString;
import com.onai.expressionevaluator.util.Utility;
import com.udojava.evalex.AbstractFunction;
import com.udojava.evalex.Expression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpressionService {

    @Autowired
    CustomContext customContext;

    public void prepareData() throws Exception {

        feeCalculationExpressionEvaluator(Utility.createdummyData());
    }

    private void feeCalculationExpressionEvaluator(Prepayment prepayment) throws Exception{
        customContext.setAttribute("prepayment", prepayment);

        String regExpression = prepayment.getRegularExpression();

        List<String> metaData = Utility.evaluateMetaDataFromExpression(regExpression);

        Double finalFee = finalFeeCalculation(regExpression, metaData);

        System.out.println("final fee is::"+finalFee);
    }

    public String evaluateFunctionExpression(String functionName, String objectName, String subExpression) throws Exception{ // attributeName should be String of Array
        List<Double> dataList = new ArrayList<>();
        Prepayment obj =  (Prepayment)customContext.getAttribute("prepayment");
        List<Object> dataObjectList = null;
        Object dataObject = null;
        if(Utility.checkOperators(subExpression)){
            dataObject = obj.getClass().getMethod("get"+ objectName).invoke(obj);
            if(dataObject instanceof List){
                dataObjectList = (List<Object>)obj.getClass().getMethod("get"+ objectName).invoke(obj);
                for (Object object : dataObjectList)
                    dataList.add(expressionEvalaute(prepareExpression(Utility.evaluateMetaDataFromExpression(subExpression), new Expression(subExpression), object)));
            }else{
                dataList.add(expressionEvalaute(prepareExpression(Utility.evaluateMetaDataFromExpression(subExpression), new Expression(subExpression), dataObject)));
            }

        }else{
            for (String attr : Utility.evaluateMetaDataFromExpression(subExpression))
                dataList = ((List<Double>) obj.getClass().getMethod("get" + attr).invoke(obj));
        }

        return iterateDataAndCalculateSum(functionName, dataList);
    }

    public String iterateDataAndCalculateSum(String functionName, List<Double> dataList) throws Exception{ // attributeName should be String of Array

        switch (functionName){
            case "sum":
                return String.valueOf(calculateAttributesSum(dataList.stream().map(e -> e.toString()).collect(Collectors.joining(","))));
        }
        return null;
    }

    public String iterateDataAndCalculateSum(String functionName, String attributeName) throws Exception{ // attributeName should be String of Array
        Object obj =  customContext.getAttribute("prepayment");
        switch (functionName){
            case "sum":
                return String.valueOf(calculateAttributesSum(((List<Double>)obj.getClass().getMethod("get"+attributeName).invoke(obj)).stream().map(e -> e.toString()).collect(Collectors.joining(","))));
        }
        return null;
    }

    public double customExpressionEvaluation(char operator, String operand2, String operand1) throws Exception{
        Object obj =  customContext.getAttribute("prepayment");
        Expression expression = new Expression(operand1 + operator + operand2);

        if(!Utility.checkNumericvalue(operand1))
            setVariableForExpression(operand1, obj, expression);

        if(!Utility.checkNumericvalue(operand2))
            setVariableForExpression(operand1, obj, expression);

        return expressionEvalaute(expression);
    }

    private Double finalFeeCalculation(String regExpression, List<String> metaData) throws Exception {

        return (metaData.contains("sum") ? EvaluateString.evaluate(regExpression) : expressionEvalaute(prepareExpression(metaData, new Expression(regExpression), customContext.getAttribute("prepayment"))));
//        return (expressionEvalaute(prepareExpression(metaData, new Expression(regExpression), customContext.getAttribute("prepayment"))));
    }

    private Expression prepareExpression(List<String> metaData, Expression expression, Object obj) throws Exception{
        for (String attributeName : metaData)
            expression.setVariable(attributeName,new BigDecimal((Double)obj.getClass().getMethod("get"+attributeName).invoke(obj)));
        return expression;
    }

    private Double expressionEvalaute(Expression expression){
        sum(expression);
        noOfDays(expression);
        return expression.setPrecision(128).setRoundingMode(RoundingMode.UP).eval().doubleValue();
    }

    private Double calculateAttributesSum(String values){
        Expression e = new Expression("sum("+values+")");
        sum(e);
        Double result = e.eval().doubleValue();
        return result;
    }

    public void sum(Expression e) {
        e.addFunction(new AbstractFunction("sum", -1) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                if (parameters.size() == 0) {
                    throw new ExpressionException("sum requires at least one parameter");
                }
                BigDecimal sum = new BigDecimal(0);
                for (BigDecimal parameter : parameters) {
                    sum = sum.add(parameter);
                }
                return sum;
            }
        });
    }

    public void noOfDays(Expression e) {
        e.addFunction(new AbstractFunction("noOfDays", -1) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
//                if (parameters.size() == 0) {
//                    throw new ExpressionException("sum requires at least one parameter");
//                }
                Prepayment prepayment = (Prepayment)customContext.getAttribute("prepayment");
                return BigDecimal.valueOf(Utility.calculateNumberOfDays(prepayment.getPeriodEndDate(), prepayment.getPeriodStartDate()));
            }
        });
    }

    private Expression setVariableForExpression(String attributeName, Object obj, Expression expression) throws  Exception{
        expression.setVariable(attributeName, new BigDecimal(fetchAttributeValues(attributeName, obj)));
        return expression;
    }

    private Double fetchAttributeValues(String attributeName, Object obj) throws  Exception{
        return (Double)obj.getClass().getMethod("get"+attributeName).invoke(obj);
    }

}
