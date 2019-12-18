package com.onai.expressionevaluator.util;

import com.udojava.evalex.AbstractFunction;
import com.udojava.evalex.Expression;
import org.springframework.expression.ExpressionException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Test1 {

    public static void main(String[] args) {


        List<Integer> list = new ArrayList<>();

//        LocalDate today = LocalDate.of(2018,12,30);
//        System.out.println("Today: ----------" + today);
//        long todayLong = today.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
//        System.out.println("todayLong: ---------- " + todayLong);
//        LocalDate todayBack = Instant.ofEpochSecond(todayLong).atZone(ZoneId.systemDefault()).toLocalDate();
//        System.out.println("todayBack: ---------- "+todayBack);





//        BigDecimal result = null;
//
//
//        Expression expression1 = new Expression("min(x,y,z) + noOfDays(a,b)");
//        noOfDays(expression1);
//        expression1.setVariable("x", new BigDecimal(2));
//        expression1.setVariable("y", new BigDecimal(3));
//        expression1.setVariable("z", new BigDecimal(4));
//        expression1.setVariable("a", new BigDecimal(6));
//        expression1.setVariable("b", new BigDecimal(4));
//        result = expression1.eval(); // 1.3
//
//        System.out.println(result);
    }

    public static void noOfDays(Expression e) {
        e.addFunction(new AbstractFunction("noOfDays", -1) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                if (parameters.size() == 0) {
                    throw new ExpressionException("sum requires at least one parameter");
                }
                for (BigDecimal parameter : parameters) {
                    System.out.println(parameter);
                }
                return BigDecimal.valueOf(Utility.calculateNumberOfDays(LocalDate.of(2019,1,30), LocalDate.of(2018,12,30)));
            }
        });
    }
    private static BigDecimal convertDateToBigdecimal(LocalDate date){
        StringBuilder sb = new StringBuilder();
        sb.append(date.getYear());
        sb.append(date.getMonthValue());
        sb.append(date.getDayOfMonth());
        return new BigDecimal(sb.toString());
    }
}
