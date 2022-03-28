package io.pleo.antaeus.core.tasks

import io.pleo.antaeus.core.messaging.InvoiceMessageProducer
import io.pleo.antaeus.core.messaging.model.InvoiceMessage
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.data.model.InvoiceSearchQuery
import io.pleo.antaeus.models.InvoiceListResult
import io.pleo.antaeus.models.InvoiceStatus

/*
  This task retrieves all pending invoices to be charged by the customer, and send it to the message producer.
 */
class InvoiceChargingTask(
        private val invoiceMessageProducer: InvoiceMessageProducer,
        private val invoiceService: InvoiceService,
        private val maxNumberOfInvoices: Int = 1000
) {

    fun execute() {
        val invoiceSearchQuery = InvoiceSearchQuery(status = InvoiceStatus.PENDING, limit = maxNumberOfInvoices)
        var invoiceListResult = InvoiceListResult(result = listOf(), hasMore = true)
        var offset = 0

        while (invoiceListResult.hasMore) {
            offset += invoiceListResult.result.size
            invoiceListResult = invoiceService.searchInvoicesPaginated(invoiceSearchQuery.copy(offset = offset))
            invoiceListResult.result.forEach {
                invoiceMessageProducer.publishMessage(InvoiceMessage(id = it.id))
            }
        }
    }
}
