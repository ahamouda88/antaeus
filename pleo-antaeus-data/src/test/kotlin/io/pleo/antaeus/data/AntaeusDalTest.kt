package io.pleo.antaeus.data

import io.pleo.antaeus.data.model.InvoiceSearchQuery
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AntaeusDalTest {
    private val database = Database.connect(url = "jdbc:h2:mem:temp-db;DB_CLOSE_DELAY=-1;IGNORECASE=true;", driver = "org.h2.Driver")
    private val tables: Array<Table> = arrayOf(InvoiceTable, CustomerTable)

    private val searchQuery: InvoiceSearchQuery = InvoiceSearchQuery(status = InvoiceStatus.PENDING, limit = 1)
    private val dal: AntaeusDal = AntaeusDal(db = database)

    @BeforeEach
    private fun setup() {
        transaction(database) {
            SchemaUtils.drop(*tables)
            SchemaUtils.create(*tables)
        }
    }

    @Nested
    inner class UpdateInvoice {

        @Test
        fun `will update invoice successfully`() {
            // Setup
            val customer = dal.createCustomer(currency = Currency.EUR)!!
            val invoice = dal.createInvoice(
                    amount = Money(value = BigDecimal(10.0), currency = Currency.EUR),
                    customer = customer,
                    status = InvoiceStatus.PENDING
            )!!
            assertEquals(invoice, dal.fetchInvoice(invoice.id))
            val updateInvoice = invoice.copy(status = InvoiceStatus.PAID, amount = Money(value = BigDecimal.valueOf(200, 2), currency = Currency.USD))

            // Act
            dal.updateInvoice(updatedInvoice = updateInvoice)

            // Verify
            assertEquals(updateInvoice, dal.fetchInvoice(invoice.id))
        }
    }

    @Nested
    inner class SearchInvoicesPaginated {

        @Test
        fun `will search invoices paginated`() {
            // Setup
            val customer = dal.createCustomer(currency = Currency.EUR)!!
            val invoiceOne = dal.createInvoice(
                    amount = Money(value = BigDecimal(10.0), currency = Currency.EUR),
                    customer = customer,
                    status = InvoiceStatus.PENDING
            )!!
            val invoiceTwo = dal.createInvoice(
                    amount = Money(value = BigDecimal(30.0), currency = Currency.USD),
                    customer = customer,
                    status = InvoiceStatus.PENDING
            )!!
            val invoiceThree = dal.createInvoice(
                    amount = Money(value = BigDecimal(60.0), currency = Currency.USD),
                    customer = customer,
                    status = InvoiceStatus.PENDING
            )!!
            dal.createInvoice(
                    amount = Money(value = BigDecimal(90.0), currency = Currency.USD),
                    customer = customer,
                    status = InvoiceStatus.PAID
            )!!

            assertEquals(4, dal.fetchInvoices().size)

            // Act & Verify
            val firstPageInvoices = dal.searchInvoicesPaginated(searchQuery)

            assertEquals(listOf(invoiceOne), firstPageInvoices.result)
            assertTrue(firstPageInvoices.hasMore)

            val secondPageInvoices = dal.searchInvoicesPaginated(searchQuery.copy(offset = 1))

            assertEquals(listOf(invoiceTwo), secondPageInvoices.result)
            assertTrue(secondPageInvoices.hasMore)

            val thirdPageInvoices = dal.searchInvoicesPaginated(searchQuery.copy(offset = 2))

            assertEquals(listOf(invoiceThree), thirdPageInvoices.result)
            assertFalse(thirdPageInvoices.hasMore)
        }
    }

    @AfterAll
    private fun shutdown() = TransactionManager.closeAndUnregister(database)
}