package com.onai.expressionevaluator.util;


import com.onai.expressionevaluator.model.LoanRateHistory;
import com.onai.expressionevaluator.model.Prepayment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

public class Utility {

    public static Prepayment createdummyData(){
        List<BigDecimal> actualInterestPaid = new ArrayList<>();
        actualInterestPaid.add(new BigDecimal(200.0));
        actualInterestPaid.add(new BigDecimal(300.0));
        actualInterestPaid.add(new BigDecimal(400.0));
        actualInterestPaid.add(new BigDecimal(500.0));

        Prepayment prepayment = new Prepayment();
        String paymentType = "Prepayment";
        prepayment.setPaymentType(paymentType);

        if(prepayment.getPaymentType().equals("TerminationActualLossOfInterest"))
            prepayment.setObjectName("LoanRateHistoryList");
        else
            prepayment.setObjectName("Prepayment");

        prepayment.setRegularExpression(fetchDataFromDB(prepayment.getPaymentType()));
        if(prepayment.getPaymentType().equals(paymentType))
        prepayment.setDaysToTerminate(34.0);
        prepayment.setPeriodStartDate(LocalDate.of(2018,12,30));
        prepayment.setPeriodEndDate(LocalDate.of(2019,01,30));
        prepayment.setAmount(new BigDecimal(50000.0));
        prepayment.setTotalInterest(new BigDecimal(0.5));
        prepayment.setFlatInterest(new BigDecimal(200000.0));

        List<LoanRateHistory> loanRateHistoryList = new ArrayList<>();
        LoanRateHistory loanRateHistory = new LoanRateHistory();
        loanRateHistory.setTotalAmountPaid(new BigDecimal(50000.0));
        loanRateHistory.setRateOfInterest(new BigDecimal(5.0));
        loanRateHistory.setTotalDays(new BigDecimal(30.0));
        loanRateHistory.setStartDate(LocalDate.of(2018,12,30));
        loanRateHistory.setEndDate(LocalDate.of(2019,02,04));
        loanRateHistoryList.add(loanRateHistory);

        loanRateHistory = new LoanRateHistory();
        loanRateHistory.setTotalAmountPaid(new BigDecimal(60000.0));
        loanRateHistory.setRateOfInterest(new BigDecimal(4.0));
        loanRateHistory.setStartDate(LocalDate.of(2018,12,30));
        loanRateHistory.setEndDate(LocalDate.of(2019,02,04));
        loanRateHistoryList.add(loanRateHistory);

        prepayment.setLoanRateHistoryList(loanRateHistoryList);
        prepayment.setActualInterestPaid(actualInterestPaid);
        return prepayment;
    }

    private static String fetchDataFromDB(String paymentType){
        Map<String,String> regExpressionMap = new WeakHashMap<>();
        regExpressionMap.put("Prepayment","( ( TotalInterest / 100.0 ) * Amount * NoOfDays ( PeriodEndDate , PeriodStartDate ) )");
        regExpressionMap.put("Termination","FlatInterest - Sum ( ActualInterestPaid )");
        regExpressionMap.put("TerminationActualLossOfInterest","Sum ( TotalAmountPaid * ( RateOfInterest / 100 ) * ( NoOfDays ( EndDate , StartDate ) / 360 ) )");

        return regExpressionMap.get(paymentType);
    }

    public static List<String> evaluateMetaDataFromExpression(String expression){
        char[] tokens = expression.toCharArray();
        List<String> dataList = new ArrayList<>();
        for (int i = 0; i < tokens.length; i++){
            if ((tokens[i] >= 'a' && tokens[i] <= 'z') ||  (tokens[i] >= 'A' && tokens[i] <= 'Z')){
                StringBuilder sbuf = new StringBuilder();
                // There may be more than one digits in number
                while (i < tokens.length && ((tokens[i] >= 'a' && tokens[i] <= 'z') || (tokens[i] >= 'A' && tokens[i] <= 'Z')))
                    sbuf.append(tokens[i++]);
                dataList.add(sbuf.toString());
            }
        }
        return dataList;
    }

    public static boolean checkNumericvalue(String operand){
        return Pattern.compile("[0-9].*").matcher(operand).find();
    }

    public static boolean checkOperators(String operand){
        return Pattern.compile("[-+*/]").matcher(operand).find();
    }


    public static int calculateNumberOfDays(LocalDate periodEndDate, LocalDate periodStartDate) {
        BigDecimal numberOfDaysInPeriod = new BigDecimal(((periodEndDate.getYear() - periodStartDate.getYear()) * 360)
                +
                ((periodEndDate.getMonthValue() - periodStartDate.getMonthValue()) * 30)
                +
                ((isEndOfMonth(periodEndDate) ? 30 : periodEndDate.getDayOfMonth()) -
                        (isEndOfMonth(periodStartDate) ? 30 : periodStartDate.getDayOfMonth())));

        return numberOfDaysInPeriod.intValue();
    }

    private static boolean isEndOfMonth(LocalDate date) {
        return date.equals(date.with(TemporalAdjusters.lastDayOfMonth()));
    }
}
