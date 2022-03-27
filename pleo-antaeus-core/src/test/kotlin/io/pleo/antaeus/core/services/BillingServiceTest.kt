package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InsufficientBalanceException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class BillingServiceTest {
    private val invoiceId: Int = 1
    private val invoice: Invoice = Invoice(
            id = invoiceId,
            amount = Money(BigDecimal.valueOf(10), Currency.EUR),
            customerId = 1,
            status = InvoiceStatus.PENDING
    )
    private val paidInvoice : Invoice = invoice.copy(status = InvoiceStatus.PAID)
    private val invoiceService: InvoiceService = mockk(relaxed = true)
    private val paymentProvider: PaymentProvider = mockk(relaxed = true)
    private val billingService = BillingService(invoiceService = invoiceService, paymentProvider = paymentProvider)

    @Nested
    inner class ChargeCustomer {

        @Test
        fun `will charge customer successfully after first network failure`() {
            // Setup
            every { invoiceService.fetch(id = invoiceId) } returns invoice
            every { paymentProvider.charge(invoice = invoice) } throws NetworkException() andThen true

            // Act
            billingService.chargeCustomer(invoiceId = invoiceId)

            // Verify
            verify { invoiceService.updateInvoice(updatedInvoice = paidInvoice) }
            verify(exactly = 2) { paymentProvider.charge(invoice = invoice) }
        }

        @Test
        fun `will throw network exception after all retry attempts`() {
            // Setup
            every { invoiceService.fetch(id = invoiceId) } returns invoice
            every { paymentProvider.charge(invoice = invoice) } throws NetworkException()

            // Act & Verify
            assertThrows<NetworkException> {
                billingService.chargeCustomer(invoiceId = invoiceId)
            }
            verify(exactly = 3) { paymentProvider.charge(invoice = invoice) }
            verify(exactly = 0) { invoiceService.updateInvoice(updatedInvoice = paidInvoice) }
        }

        @Test
        fun `will throw exception when payment provider call fails`() {
            // Setup
            every { invoiceService.fetch(id = invoiceId) } returns invoice
            every { paymentProvider.charge(invoice = invoice) } throws CustomerNotFoundException(invoice.customerId)

            // Act & Verify
            assertThrows<CustomerNotFoundException> {
                billingService.chargeCustomer(invoiceId = invoiceId)
            }
            verify { paymentProvider.charge(invoice = invoice) }
            verify(exactly = 0) { invoiceService.updateInvoice(updatedInvoice = paidInvoice) }
        }

        @Test
        fun `will do nothing when invoice is paid`() {
            // Setup
            every { invoiceService.fetch(id = invoiceId) } returns paidInvoice

            // Act
            billingService.chargeCustomer(invoiceId = invoiceId)

            // Verify
            verify(exactly = 0) { invoiceService.updateInvoice(updatedInvoice = any()) }
            verify(exactly = 0) { paymentProvider.charge(invoice = any()) }
        }

        @Test
        fun `will throw exception when customer has insufficient balance`() {
            // Setup
            every { invoiceService.fetch(id = invoiceId) } returns invoice
            every { paymentProvider.charge(invoice = invoice) } returns false

            // Act & Verify
            assertThrows<InsufficientBalanceException> {
                billingService.chargeCustomer(invoiceId = invoiceId)
            }
            verify(exactly = 0) { invoiceService.updateInvoice(updatedInvoice = any()) }
        }
    }
}
