package io.pleo.antaeus.core.tasks

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.messaging.InvoiceMessageProducer
import io.pleo.antaeus.core.messaging.model.InvoiceMessage
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class InvoiceChargingTaskTest {
    private val invoiceId: Int = 1
    private val invoice: Invoice = Invoice(
            id = invoiceId,
            amount = Money(BigDecimal.valueOf(10), Currency.EUR),
            customerId = 1,
            status = InvoiceStatus.PENDING
    )
    private val invoiceService: InvoiceService = mockk(relaxed = true)
    private val invoiceMessageProducer: InvoiceMessageProducer = mockk(relaxed = true)
    private val invoiceChargingTask = InvoiceChargingTask(
            invoiceService = invoiceService,
            invoiceMessageProducer = invoiceMessageProducer,
            maxNumberOfInvoices = 2
    )

    @Test
    fun `will paginate throw pending invoices`() {
        // Setup
        every { invoiceService.searchInvoicesPaginated(any()) } returns
                InvoiceListResult(result = listOf(invoice, invoice), hasMore = true) andThen
                InvoiceListResult(result = listOf(invoice, invoice), hasMore = true) andThen
                InvoiceListResult(result = listOf(invoice), hasMore = false)

        // Act
        invoiceChargingTask.execute()

        // Verify
        verify(exactly = 3) { invoiceService.searchInvoicesPaginated(any()) }
        verify(exactly = 5) { invoiceMessageProducer.publishMessage(invoiceMessage = InvoiceMessage(id = invoiceId)) }
    }
}
