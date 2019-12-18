package com.onai.expressionevaluator.util;

import com.udojava.evalex.AbstractFunction;
import com.udojava.evalex.AbstractLazyFunction;
import com.udojava.evalex.Expression;
import org.springframework.expression.ExpressionException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        BigDecimal result = null;


        Expression expression1 = new Expression("min(x,y,z)");
        expression1.setVariable("x", new BigDecimal(2));
        expression1.setVariable("y", new BigDecimal(3));
        expression1.setVariable("z", new BigDecimal(4));
        result = expression1.eval(); // 1.3

        // Simple usage with an expression without variables.
        Expression expression = new Expression("(100.0 * 2.0) + 12.0");
        result = expression.eval(); // 1.333333
        // Lowering the precision.
        expression.setPrecision(3);
        result = expression.eval(); // 1.3
        System.out.println("result 1 is::"+result.doubleValue());

        // A more complex expression showing support for unary operators.
        result = new Expression("(3.4 + -4.1)/2").eval(); // -0.35
        System.out.println(result);

        // Using functions and variables.
        result = new Expression("SQRT(a^2 + b^2)")
                .with("a", "2.4")
                .and("b", "9.253")
                .eval(); // 9.5591845
        System.out.println(result);

        // Using pre-created BigDecimals for variables
        BigDecimal a = new BigDecimal("2.4");
        BigDecimal b = new BigDecimal("9.235");
        result = new Expression("SQRT(a^2 + b^2)")
                .with("a", a)
                .and("b", b)
                .eval(); // 9.5591845
        System.out.println("result is::"+result);

        // Increasing the precision and setting a different rounding mode.
        result = new Expression("2.4/PI")
                .setPrecision(128)
                .setRoundingMode(RoundingMode.UP)
                .eval(); // 0.763943726841...
        System.out.println(result);

        // Using a function to receive a random number and test it.
        result = new Expression("random() > 0.5").eval(); // 1
        System.out.println(result);

        // Using more functions and showing the boolean support.
        result = new Expression("not(x<7 || sqrt(max(x,9,3,min(4,3))) <= 3)")
                .with("x", "22.9")
                .eval(); // 1
        System.out.println(result);

        List<Double> dataList = new ArrayList<>();
        dataList.add(2.0);
        dataList.add(3.0);
        dataList.add(4.0);
        dataList.add(5.0);

        StringBuffer sbuf = new StringBuffer();
        for(Double data : dataList){
            sbuf.append(data);
            sbuf.append(",");
        }
        System.out.println("list::"+dataList+":: array::"+sbuf);
        Expression e = new Expression("sum("+sbuf+")");


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

        result = e.eval();
        System.out.println("sum result is::"+ result);


        Expression e6 = new Expression("STREQ(\"test\", \"test\")");
        e6.addLazyFunction(new AbstractLazyFunction("STREQ", 2) {
            private Expression.LazyNumber ZERO = new Expression.LazyNumber() {
                public BigDecimal eval() {
                    return BigDecimal.ZERO;
                }
                public String getString() {
                    return null;
                }
            };
            private Expression.LazyNumber ONE = new Expression.LazyNumber() {
                public BigDecimal eval() {
                    return BigDecimal.ONE;
                }
                public String getString() {
                    return null;
                }
            };
            @Override
            public Expression.LazyNumber lazyEval(List<Expression.LazyNumber> lazyParams) {
                if (lazyParams.get(0).getString().equals(lazyParams.get(1).getString())) {
                    return ZERO;
                }
                return ONE;
            }
        });

        System.out.println("result is ::"+e6.eval());
    }



}
