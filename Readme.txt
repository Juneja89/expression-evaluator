Steps to Execute the Expression Builder:

1) Start the server
2) Open the link defined below:
    http://localhost:8080/swagger-ui.html

Request Body for Prepayment:
{
    "amount": 50000.0,
    "objectName": "Prepayment",
    "periodStartDate": "2018-12-30",
    "periodEndDate": "2019-01-30",
    "paymentType": "Prepayment",
    "regularExpression": "( ( TotalInterest / 100.0 ) * Amount * NoOfDays ( PeriodEndDate , PeriodStartDate ) )",
    "totalInterest": 0.5
}

Request Body for Termination:
{
    "actualInterestPaid": [
        50000.0,
        20000.0,
        35000.0
    ],
    "objectName": "Prepayment",
    "paymentType": "Termination",
    "periodStartDate": "2018-12-30",
    "periodEndDate": "2019-01-30",
    "flatInterest": 200000.0,
    "regularExpression": "FlatInterest - Sum ( ActualInterestPaid )"
}

Request Body for Actual Loss of Interest:
{
        "amount": 50000.0,
        "objectName": "LoanRateHistoryList",
        "paymentType": "Termination Actual Loss Of Interest",
        "regularExpression": "Sum ( TotalAmountPaid * ( RateOfInterest / 100 ) * ( NoOfDays ( EndDate , StartDate ) / 360 ) )",
        "loanRateHistoryList": [
            {
                "id": 1,
                "startDate": "2018-12-30",
                "endDate": "2019-01-30",
                "rateOfInterest": 5.0,
                "totalAmountPaid": 50000.0
            },
            {
                "id": 2,
                "startDate": "2018-12-30",
                "endDate": "2019-01-30",
                "rateOfInterest": 10.0,
                "totalAmountPaid": 100000.0
            }
        ]
    }