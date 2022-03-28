package io.pleo.antaeus.core.exceptions

class InsufficientBalanceException(invoiceId: Int, customerId: Int) :
        Exception("Insufficient balance for customer: '$customerId' to pay invoice: '$invoiceId'")
