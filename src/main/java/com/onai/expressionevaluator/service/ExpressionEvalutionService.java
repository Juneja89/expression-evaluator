package com.onai.expressionevaluator.service;

import com.onai.expressionevaluator.config.CustomContext;
import com.onai.expressionevaluator.model.Prepayment;
import com.onai.expressionevaluator.util.EvaluateSubExpression;
import com.onai.expressionevaluator.util.Utility;
import com.udojava.evalex.AbstractFunction;
import com.udojava.evalex.Expression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionException;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExpressionEvalutionService {

    @Autowired
    CustomContext customContext;

    @Autowired
    EvaluateSubExpression evaluateSubexpression;

    public void prepareData() throws Exception {

        feeCalculationExpressionEvaluator(Utility.createdummyData());
    }

    public Prepayment feeCalculationExpressionEvaluator(Prepayment prepayment) throws Exception{
        customContext.setAttribute("prepayment", prepayment);

        String regExpression = prepayment.getRegularExpression();
        List<String> defaultFunctionList = getCustomFunctions();
        System.out.println("Payment Type is::"+ prepayment.getPaymentType());
        System.out.println("Expression is::"+ regExpression);
        BigDecimal finalFee = evaluateSubexpression.evaluateSubexpression(regExpression, customFunctionAcceptListOfObjects());
        System.out.println("final fee is::"+finalFee);
        prepayment.setTotalFee(finalFee);
        return prepayment;
    }

    public BigDecimal subExpressionEvaluator(String subExpression, String functionName)throws Exception{
        if(customFunctionAcceptListOfObjects().contains(functionName)){
            System.out.println("case::list of objects::SubExpression is::"+subExpression + ":: function is::"+functionName);
            return prepareExpressionForListOfObjects(subExpression, functionName);
        }else{
            System.out.println("case::single object::subExpression is::"+ subExpression);
            return prepareExpressionForOtherObjects(subExpression);
        }
    }

    private BigDecimal prepareExpressionForListOfObjects(String subExpression, String functionName)throws Exception {
        List<BigDecimal> dataList = new ArrayList<>();
        Object obj =  customContext.getAttribute("prepayment");
        String objectName = (String)obj.getClass().getMethod("getObjectName").invoke(obj);
        if(Utility.checkOperators(subExpression)){
                List<Object> dataObjectList = (List<Object>) obj.getClass().getMethod("get" + objectName).invoke(obj);
                for (Object object : dataObjectList)
                    dataList.add(expressionEvalaute(addAttributeToExpression(subExpression, Utility.evaluateMetaDataFromExpression(subExpression), getCustomFunctions(), object)));
        }else{
            for (String attr : Utility.evaluateMetaDataFromExpression(subExpression)){
                if(objectName.equalsIgnoreCase("Prepayment"))
                    dataList = ((List<BigDecimal>) obj.getClass().getMethod("get" + attr).invoke(obj));
                else {
                    Object otherObject = obj.getClass().getMethod("get" + objectName).invoke(obj);
                    dataList = ((List<BigDecimal>) otherObject.getClass().getMethod("get" + attr).invoke(otherObject));
                }
            }
        }

        String values = (dataList.stream().map(e -> e.toString()).collect(Collectors.joining(",")));
        Expression expression = new Expression(functionName + "(" + values + ")");
        return expressionEvalaute(expression);
    }

    private BigDecimal prepareExpressionForOtherObjects(String regExpression) throws Exception{

        Object object = customContext.getAttribute("prepayment");
        List<String> metaData = Utility.evaluateMetaDataFromExpression(regExpression);

        Expression expression = addAttributeToExpression(regExpression, metaData, getCustomFunctions(), object);
        return  expressionEvalaute(expression);
    }

    private BigDecimal expressionEvalaute(Expression expression){
        sum(expression);
        noOfDays(expression);
        return expression.setPrecision(128).eval().setScale(2, BigDecimal.ROUND_HALF_EVEN);
    }

    private Expression addAttributeToExpression(String regExpression, List<String> metaData, List<String> defaultFunctionList, Object object) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Expression expression = new Expression(regExpression);
        for (String attributeName : metaData) {
            if(!defaultFunctionList.contains(attributeName)) {
                if (object.getClass().getMethod("get" + attributeName).invoke(object) instanceof LocalDate) {
                      expression.setVariable(attributeName, new BigDecimal(((LocalDate)object.getClass().getMethod("get" + attributeName).invoke(object)).atStartOfDay(ZoneId.systemDefault()).toEpochSecond()));
                } else {
                    expression.setVariable(attributeName, (BigDecimal) object.getClass().getMethod("get" + attributeName).invoke(object));
                }
            }
        }
        return expression;
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
                if (parameters.size() == 0) {
                    throw new ExpressionException("sum requires at least one parameter");
                }
                LocalDate endDate = Instant.ofEpochSecond(parameters.get(0).longValue()).atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate startDate = Instant.ofEpochSecond(parameters.get(1).longValue()).atZone(ZoneId.systemDefault()).toLocalDate();
                return BigDecimal.valueOf(Utility.calculateNumberOfDays(endDate, startDate));
            }
        });
    }

    private List<String> getCustomFunctions() {
        List<String> defaultFunctionList = new ArrayList<>();
        defaultFunctionList.add("Sum");
        defaultFunctionList.add("NoOfDays");
        return defaultFunctionList;
    }

    private List<String> customFunctionAcceptListOfObjects() {
        List<String> defaultFunctionList = new ArrayList<>();
        defaultFunctionList.add("Sum");
        return defaultFunctionList;
    }
}
