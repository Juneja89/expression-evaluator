package com.onai.expressionevaluator.controller;

import com.onai.expressionevaluator.model.Prepayment;
import com.onai.expressionevaluator.service.ExpressionEvalutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("expression")
public class ExpressionEvaluatorController {

    @Autowired
    ExpressionEvalutionService expressionEvalutionService;

    @RequestMapping(value = "/evaluation", method = RequestMethod.POST)
    public Prepayment expressionEvaluator(@RequestBody Prepayment prepayment) throws Exception{
        return expressionEvalutionService.feeCalculationExpressionEvaluator(prepayment);
    }
}
