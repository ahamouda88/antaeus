package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InsufficientBalanceException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.utils.RetryUtils
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging

class BillingService(
        private val invoiceService: InvoiceService,
        private val paymentProvider: PaymentProvider
) {

    private val logger = KotlinLogging.logger {}
    private val retryUtils: RetryUtils = RetryUtils(name = "charge",
            retryExceptions = *arrayOf(NetworkException::class.java))

    /*
       Charges a customer a particular invoice given the id of the invoice. If the invoice is already paid nothing will happen,
       otherwise the invoice will be updated with the new status.

       Throws:
          `InsufficientBalanceException`: when customer has insufficient balance and failed to pay the invoice.
     */
    fun chargeCustomer(invoiceId: Int) {
        val invoice: Invoice = invoiceService.fetch(id = invoiceId)
        when (invoice.status) {
            InvoiceStatus.PAID -> {
                logger.warn { "Invoice '$invoiceId' is already paid!" }
            }
            InvoiceStatus.PENDING -> {
                // Will keep the Customer & Currency validation on the Payment Provider service.
                val chargeStatus: Boolean = retryUtils.retry { paymentProvider.charge(invoice) }
                if (!chargeStatus) {
                    throw InsufficientBalanceException(invoiceId = invoiceId, customerId = invoice.customerId)
                }
                invoiceService.updateInvoice(invoice.copy(status = InvoiceStatus.PAID))
            }
        }
    }
}
