package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.data.model.InvoiceSearchQuery
import io.pleo.antaeus.models.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class InvoiceServiceTest {
    private val searchQuery: InvoiceSearchQuery = InvoiceSearchQuery(status = InvoiceStatus.PENDING)
    private val invoice: Invoice = Invoice(
            id = 1,
            amount = Money(BigDecimal.valueOf(10), Currency.EUR),
            customerId = 1,
            status = InvoiceStatus.PENDING
    )
    private val dal: AntaeusDal = mockk {
        every { fetchInvoice(404) } returns null
        every { updateInvoice(invoice) } returns invoice
    }

    private val invoiceService: InvoiceService = InvoiceService(dal = dal)

    @Test
    fun `will throw if invoice is not found`() {
        assertThrows<InvoiceNotFoundException> {
            invoiceService.fetch(404)
        }
    }

    @Test
    fun `will search invoices`() {
        val invoiceListResult = InvoiceListResult(result = listOf(invoice), hasMore = false)
        every { dal.searchInvoicesPaginated(searchQuery) } returns invoiceListResult

        val actualResult = invoiceService.searchInvoicesPaginated(invoiceSearchQuery = searchQuery)

        assertEquals(invoiceListResult, actualResult)
    }

    @Test
    fun `will update invoice`() {
        assertEquals(invoice, invoiceService.updateInvoice(invoice))
    }
}
